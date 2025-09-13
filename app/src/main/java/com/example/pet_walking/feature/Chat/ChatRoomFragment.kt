package com.example.pet_walking.feature.Chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private val args: ChatRoomFragmentArgs by navArgs()
    private lateinit var groupInfoTextView: TextView
    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var leaveButton: Button

    // ✅ 서버 스펙: ISO-8601 문자열 타임스탬프 사용
    private var lastSinceIso: String? = null

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
            }
        }

        leaveButton.setOnClickListener {
            val userId = currentUserId ?: return@setOnClickListener
            ChatRoomManager.leaveChatRoom(args.roomId, userId) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                        ChatRoomManager.getJoinedChatRooms(userId) {
                            requireActivity().runOnUiThread {
                                findNavController().popBackStack()
                            }
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
        val writerId = LoginSession.userId ?: return
        val nowIso = nowIsoString() // 예: "2025-08-05T12:00:00"

        val json = JSONObject().apply {
            put("roomId", args.roomId)
            put("writerId", writerId)
            put("writeTime", nowIso)         // ✅ 서버 스펙: 문자열 ISO-8601
            put("contentType", "text")
            put("content", content)
        }

        // 서버 스펙: /chat/upload
        ChatNetworkHelper.postJson("/chat/upload", json) { success ->
            requireActivity().runOnUiThread {
                if (success) {
                    fetchMessages()
                } else {
                    Toast.makeText(requireContext(), "메시지 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 메시지 수신 */
    private fun fetchMessages() {
        val req = JSONObject().apply {
            put("roomId", args.roomId)
            // latestTimestamp는 ISO 문자열. 없으면 생략(서버가 최신 몇 개를 내려주도록)
            lastSinceIso?.let { put("latestTimestamp", it) }
        }

        // 서버 스펙: /chat/download
        ChatNetworkHelper.postJsonWithResult("/chat/download", req) { res ->
            res ?: return@postJsonWithResult

            // 서버가 배열을 직접 반환하는 대신, 헬퍼가 JSONObject로 래핑해
            // "contentList"에 담아주는 구조를 가정 (기존 앱 호환)
            val items = res.optJSONArray("contentList") ?: return@postJsonWithResult

            // 서버 시각 기준으로 가장 큰 writeTime(ISO)을 집계
            var maxIso: String? = lastSinceIso
            var maxMs: Long = lastSinceIso?.let { parseIsoToMillis(it) } ?: Long.MIN_VALUE

            for (i in 0 until items.length()) {
                val obj       = items.getJSONObject(i)
                val nickname  = obj.optString("writerNickname", "익명")
                val content   = obj.optString("content", "")
                val writeTime = obj.optString("writeTime", null) // 예: "2025-08-06T10:00:00"

                requireActivity().runOnUiThread {
                    addChatMessage(nickname, content)
                }

                // 최신 시각 갱신
                val ms = writeTime?.let { parseIsoToMillis(it) } ?: Long.MIN_VALUE
                if (ms > maxMs) {
                    maxMs = ms
                    maxIso = writeTime
                }
            }

            // 다음 요청 기준점 업데이트 (서버 시각을 그대로 사용)
            if (maxIso != null) {
                lastSinceIso = maxIso
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
    // ISO-8601 (서버 스펙) 유틸
    // --------------------------

    /**
     * 서버 예시 "2025-08-05T12:00:00" 형태 출력 (로컬 타임존)
     * ※ 서버가 타임존을 따로 요구하지 않아 예시와 동일 포맷 사용
     */
    private fun nowIsoString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        // 필요 시 타임존 고정: sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /** "yyyy-MM-dd'T'HH:mm:ss" → epoch millis */
    private fun parseIsoToMillis(iso: String): Long {
        val sdfLocal = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        return try {
            sdfLocal.parse(iso)?.time ?: Long.MIN_VALUE
        } catch (_: ParseException) {
            Long.MIN_VALUE
        }
    }
}