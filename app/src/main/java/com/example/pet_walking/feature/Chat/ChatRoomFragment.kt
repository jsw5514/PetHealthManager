package com.example.pet_walking.feature.Chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
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

        // 서버가 요구한 최소 타임스탬프 (LocalDateTime.MIN 대신)
        private const val MIN_TIMESTAMP_ISO = "0000-01-01T00:00:00"
    }

    private val args: ChatRoomFragmentArgs by navArgs()
    private lateinit var groupInfoTextView: TextView
    private lateinit var memberListTextView: TextView
    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var leaveButton: Button
    private lateinit var inviteMemberButton: Button
    private lateinit var chatScrollView: ScrollView

    // 증분 조회 기준(항상 유효한 값으로 시작)
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

        groupInfoTextView   = view.findViewById(R.id.groupInfoTextView)
        memberListTextView  = view.findViewById(R.id.memberListTextView)
        chatScrollView      = view.findViewById(R.id.chatScrollView)
        chatContainer       = view.findViewById(R.id.chatContainer)
        messageInput        = view.findViewById(R.id.messageInput)
        sendButton          = view.findViewById(R.id.sendButton)
        leaveButton         = view.findViewById(R.id.leaveRoomButton)
        inviteMemberButton  = view.findViewById(R.id.inviteMemberButton)

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
                Toast.makeText(requireContext(), "메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        leaveButton.setOnClickListener {
            val userId = currentUserId ?: return@setOnClickListener
            ChatRoomManager.leaveChatRoom(args.roomId, userId) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "나가기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        inviteMemberButton.setOnClickListener { showInviteDialog() }

        // 최초 진입 시 멤버 목록 한 번 로드
        loadMembers()

        startAutoUpdate()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    /** 메시지 전송 */
    private fun sendMessage(content: String) {
        val writerId = LoginSession.userId ?: UserRepository.getCurrentUserId()
        if (writerId.isNullOrBlank()) {
            Log.e(TAG, "sendMessage: writerId is null")
            Toast.makeText(requireContext(), "로그인이 만료되었습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val nowIso = nowIsoString()

        val json = JSONObject().apply {
            put("roomId", args.roomId)
            put("writerId", writerId)
            put("writeTime", nowIso)         // ISO-8601 문자열
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
            put("latestTimestamp", lastSinceIso)
        }

        ChatNetworkHelper.postJsonWithResult("/chat/download", req) { res ->
            if (res == null) {
                Log.e(TAG, "fetchMessages: response null")
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "채팅 불러오기에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                return@postJsonWithResult
            }

            val items = res.optJSONArray("contentList")
            if (items == null) {
                Log.e(TAG, "fetchMessages: contentList missing")
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "메시지 목록이 비어 있습니다.", Toast.LENGTH_SHORT).show()
                }
                return@postJsonWithResult
            }

            var maxIso: String? = null
            var maxMs: Long = Long.MIN_VALUE

            for (i in 0 until items.length()) {
                val obj       = items.getJSONObject(i)
                val nickname  = obj.optString("writerNickname", "익명")
                val content   = obj.optString("content", "")
                val writeTime = obj.optString("writeTime", null)

                requireActivity().runOnUiThread {
                    addChatMessage(nickname, content)
                }

                val ms = writeTime?.let { parseIsoToMillis(it) } ?: Long.MIN_VALUE
                if (ms > maxMs) { maxMs = ms; maxIso = writeTime }
            }

            if (maxIso != null) {
                lastSinceIso = maxIso!!
                Log.d(TAG, "fetchMessages: lastSinceIso → $lastSinceIso")
            }

            // 받아온 뒤 자동 스크롤
            requireActivity().runOnUiThread { scrollToBottom() }
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

    /** 멤버 초대 다이얼로그 */
    private fun showInviteDialog() {
        val input = EditText(requireContext()).apply { hint = "초대할 사용자 ID" }
        AlertDialog.Builder(requireContext())
            .setTitle("유저 초대")
            .setView(input)
            .setPositiveButton("초대") { _, _ ->
                val targetId = input.text.toString().trim()
                if (targetId.isEmpty()) {
                    Toast.makeText(requireContext(), "사용자 ID를 입력하세요.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                ChatRoomManager.inviteMember(args.roomId, targetId) { ok ->
                    activity?.runOnUiThread {
                        if (ok) {
                            Toast.makeText(requireContext(), "초대 완료", Toast.LENGTH_SHORT).show()
                            loadMembers()
                        } else {
                            Toast.makeText(requireContext(), "초대 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /** 멤버 목록 로드 */
    private fun loadMembers() {
        ChatRoomManager.getChatMembers(args.roomId) { members ->
            activity?.runOnUiThread {
                memberListTextView.text =
                    if (members.isNullOrEmpty()) "참여자: -"
                    else "참여자: ${members.joinToString(", ")}"
            }
        }
    }

    /** 스크롤 최하단으로 */
    private fun scrollToBottom() {
        chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }
    }

    // --------------------------
    // ISO-8601 유틸
    // --------------------------

    /** 현재 시각을 서버 포맷으로 반환 */
    private fun nowIsoString(): String {
        val sdf = SimpleDateFormat(SERVER_TS_PATTERN, Locale.US)
        // 서버가 UTC 요구 시:
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