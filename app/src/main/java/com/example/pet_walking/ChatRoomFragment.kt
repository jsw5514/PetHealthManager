package com.example.pet_walking.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pet_walking.LoginSession
import com.example.pet_walking.R
import org.json.JSONObject


class ChatRoomFragment : Fragment() {

    private val args: ChatRoomFragmentArgs by navArgs()
    private lateinit var groupInfoTextView: TextView
    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var leaveButton: Button

    private var lastTimestamp: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 3000L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_chat_room, container, false)

        groupInfoTextView = view.findViewById(R.id.groupInfoTextView)
        chatContainer = view.findViewById(R.id.chatContainer)
        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)
        leaveButton = view.findViewById(R.id.leaveRoomButton)

        val currentUserId = LoginSession.userId
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
            ChatRoomManager.leaveChatRoom(args.roomId, currentUserId) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                        ChatRoomManager.getJoinedChatRooms(currentUserId) { updatedRooms ->
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

    private fun sendMessage(content: String) {
        val writerId = LoginSession.userId ?: return
        val currentTime = System.currentTimeMillis()
        val json = JSONObject().apply {
            put("roomId", args.roomId)
            put("writerId", writerId)
            put("writeTime", currentTime)
            put("contentType", "text")
            put("content", content)
        }

        ChatNetworkHelper.postJson("/uploadChat", json) { success ->
            if (success) {
                requireActivity().runOnUiThread {
                    fetchMessages()
                }
            }
        }
    }

    private fun fetchMessages() {
        val json = JSONObject().apply {
            put("roomId", args.roomId)
            put("latestTimestamp", lastTimestamp)
        }

        ChatNetworkHelper.postJsonWithResult("/downloadChat", json) { result ->
            result?.let { jsonObj ->
                val contentList = jsonObj.getJSONArray("contentList")
                for (i in 0 until contentList.length()) {
                    val item = contentList.getJSONObject(i)
                    val nickname = item.getString("writerNickname")
                    val content = item.getString("content")
                    requireActivity().runOnUiThread {
                        addChatMessage(nickname, content)
                    }
                }
                lastTimestamp = System.currentTimeMillis()
            }
        }
    }

    private fun addChatMessage(nickname: String, message: String) {
        val isMyMessage = nickname == LoginSession.userId

        val layoutRes = if (isMyMessage) {
            R.layout.item_chat_message_me  // 내 메시지용 레이아웃
        } else {
            R.layout.item_chat_message_me     // 상대 메시지용 레이아웃
        }

        val messageLayout = LayoutInflater.from(requireContext())
            .inflate(layoutRes, chatContainer, false)

        val nicknameView = messageLayout.findViewById<TextView>(R.id.nicknameTextView)
        val messageView = messageLayout.findViewById<TextView>(R.id.messageTextView)

        nicknameView.text = nickname
        messageView.text = message

        chatContainer.addView(messageLayout)
    }

    private fun startAutoUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchMessages()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }
}