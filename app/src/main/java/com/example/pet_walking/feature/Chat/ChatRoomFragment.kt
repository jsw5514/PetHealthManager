package com.example.pet_walking.feature.Chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pet_walking.R
import com.example.pet_walking.feature.Login.LoginSession
import com.example.pet_walking.feature.profile.repository.UserRepository
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatRoomFragment : Fragment() {

    companion object {
        private const val TAG = "ChatRoomFragment"

        // 서버 스펙: ISO-8601 "yyyy-MM-dd'T'HH:mm:ss"
        private const val SERVER_TS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

        // ★ 서버가 요구한 최소 타임스탬프 (LocalDateTime.MIN 대신)
        private const val MIN_TIMESTAMP_ISO = "0000-01-01T00:00:00"
    }

    private val args: ChatRoomFragmentArgs by navArgs()
    private lateinit var groupInfoTextView: TextView
    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var leaveButton: Button

    // ★ 증분 조회 기준(항상 유효한 값으로 시작)
    private var lastSinceIso: String = MIN_TIMESTAMP_ISO

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 3000L

    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat_room, container, false)

        groupInfoTextView = view.findViewById(R.id.groupInfoTextView)
        chatContainer     = view.findViewById(R.id.chatContainer)
        messageInput      = view.findViewById(R.id.messageInput)
        sendButton        = view.findViewById(R.id.sendButton)
        leaveButton       = view.findViewById(R.id.leaveRoomButton)

        currentUserId = UserRepository.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인 정보 없음", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return view
        }

        groupInfoTextView.text = "채팅방 ID: ${args.roomId} | 사용자: $currentUserId"

        sendButton.setOnClickListener {
            val content = messageInput.text.toString().trim()
            if (content.isNotBlank()) {
                sendMessage(content)
                messageInput.text.clear()
            } else {
                // ★ 빈 메시지 방지 피드백
                Toast.makeText(requireContext(), "메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        leaveButton.setOnClickListener {
            val userId = currentUserId ?: return@setOnClickListener
            ChatRoomManager.leaveChatRoom(args.roomId, userId) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                        ChatRoomManager.getJoinedChatRooms(userId) {
                            requireActivity().runOnUiThread { findNavController().popBackStack() }
                        }
                    } else {
                        Toast.makeText(requireContext(), "나가기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        startAutoUpdate()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    /** 메시지 전송 */
    private fun sendMessage(content: String) {
        // ★ writerId 널 안전 처리 + 사용자 피드백
        val writerId = LoginSession.userId ?: UserRepository.getCurrentUserId()
        if (writerId.isNullOrBlank()) {
            Log.e(TAG, "sendMessage: writerId is null")
            Toast.makeText(requireContext(), "로그인이 만료되었습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val nowIso = nowIsoString() // 예: "2025-08-05T12:00:00"

        val json = JSONObject().apply {
            put("roomId", args.roomId)
            put("writerId", writerId)
            put("writeTime", nowIso)         // 서버 스펙: 문자열 ISO-8601
            put("contentType", "text")
            put("content", content)
        }

        ChatNetworkHelper.postJson("/chat/upload", json) { success ->
            requireActivity().runOnUiThread {
                if (success) {
                    fetchMessages()
                } else {
                    Log.e(TAG, "sendMessage: /chat/upload failed")
                    Toast.makeText(requireContext(), "메시지 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 메시지 수신 */
    private fun fetchMessages() {
        val req = JSONObject().apply {
            put("roomId", args.roomId)
            put("latestTimestamp", lastSinceIso)  // 항상 포함(최초 요청 시 MIN 값)
        }

        ChatNetworkHelper.postJsonWithResult("/chat/download", req) { res ->
            if (res == null) {
                // ★ 조용히 return 하지 말고 알림/로그
                Log.e(TAG, "fetchMessages: response null")
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "채팅 불러오기에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                return@postJsonWithResult
            }

            // ChatNetworkHelper가 배열 응답을 {"contentList":[...]}로 래핑한다고 가정
            val items = res.optJSONArray("contentList")
            if (items == null) {
                Log.e(TAG, "fetchMessages: contentList missing")
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "메시지 목록이 비어 있습니다.", Toast.LENGTH_SHORT).show()
                }
                return@postJsonWithResult
            }

            // 서버 시각 기준 최신값 계산
            var maxIso: String? = null
            var maxMs: Long = Long.MIN_VALUE

            for (i in 0 until items.length()) {
                val obj       = items.getJSONObject(i)
                val nickname  = obj.optString("writerNickname", "익명")
                val content   = obj.optString("content", "")
                val writeTime = obj.optString("writeTime", null) // 예: "2025-08-06T10:00:00"

                requireActivity().runOnUiThread {
                    addChatMessage(nickname, content)
                }

                val ms = writeTime?.let { parseIsoToMillis(it) } ?: Long.MIN_VALUE
                if (ms > maxMs) { maxMs = ms; maxIso = writeTime }
            }

            // 다음 요청 기준점을 서버 시각으로 갱신 (없으면 기존 값 유지)
            if (maxIso != null) {
                lastSinceIso = maxIso!!
                Log.d(TAG, "fetchMessages: lastSinceIso → $lastSinceIso")
            }
        }
    }

    /** 채팅 말풍선 추가 */
    private fun addChatMessage(nickname: String, message: String) {
        val mine = (nickname == LoginSession.userId)

        val layoutRes = if (mine) {
            R.layout.item_chat_message_me
        } else {
            R.layout.item_chat_message_other
        }

        val messageLayout = LayoutInflater.from(requireContext())
            .inflate(layoutRes, chatContainer, false)

        val nicknameView = messageLayout.findViewById<TextView>(R.id.nicknameTextView)
        val messageView  = messageLayout.findViewById<TextView>(R.id.messageTextView)

        nicknameView.text = nickname
        messageView.text   = message

        chatContainer.addView(messageLayout)
    }

    /** 주기적 폴링 시작 */
    private fun startAutoUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchMessages()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    // --------------------------
    // ISO-8601 유틸
    // --------------------------

    /** 현재 시각을 서버 포맷으로 반환 */
    private fun nowIsoString(): String {
        val sdf = SimpleDateFormat(SERVER_TS_PATTERN, Locale.US)
        // 서버가 UTC 요구 시 주석 해제:
        // sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /** "yyyy-MM-dd'T'HH:mm:ss" → epoch millis */
    private fun parseIsoToMillis(iso: String): Long {
        val sdfLocal = SimpleDateFormat(SERVER_TS_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        return try {
            sdfLocal.parse(iso)?.time ?: Long.MIN_VALUE
        } catch (_: ParseException) {
            Long.MIN_VALUE
        }
    }
}