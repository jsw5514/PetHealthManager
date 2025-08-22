package com.example.pet_walking.feature.Chat
/*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pet_walking.feature.Login.LoginSession
import com.example.pet_walking.R
import com.example.pet_walking.feature.Chat.ChatRoomManager
import com.example.pet_walking.feature.profile.repository.UserRepository

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

        val currentUserId = UserRepository.getCurrentUserId()
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
            R.layout.item_chat_message_other     // 상대 메시지용 레이아웃
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
}*/

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pet_walking.R
import com.example.pet_walking.feature.Login.LoginSession
import com.example.pet_walking.feature.profile.repository.UserRepository
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

        // ✅ 변경: /uploadChat → /chat/upload
        ChatNetworkHelper.postJson("/chat/upload", json) { success ->
            if (success) {
                requireActivity().runOnUiThread { fetchMessages() }
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "메시지 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchMessages() {
        val json = JSONObject().apply {
            put("roomId", args.roomId)
            put("latestTimestamp", lastTimestamp)
        }

        // ✅ 변경: /downloadChat → /chat/download
        ChatNetworkHelper.postJsonWithResult("/chat/download", json) { result ->
            result?.let { jsonObj ->
                val contentList = jsonObj.optJSONArray("contentList") ?: return@postJsonWithResult
                for (i in 0 until contentList.length()) {
                    val item     = contentList.getJSONObject(i)
                    val nickname = item.optString("writerNickname", "익명")
                    val content  = item.optString("content", "")
                    requireActivity().runOnUiThread {
                        addChatMessage(nickname, content)
                    }
                }
                // 서버에서 타임스탬프를 내려주면 그 값을 쓰는 게 안전하지만,
                // 현재 스키마를 모르므로 우선 클라이언트 시각으로 갱신 유지
                lastTimestamp = System.currentTimeMillis()
            }
        }
    }

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

    private fun startAutoUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchMessages()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }
}