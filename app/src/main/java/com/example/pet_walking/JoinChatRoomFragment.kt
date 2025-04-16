package com.example.pet_walking.chat

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.LoginSession
import com.example.pet_walking.R

class JoinChatRoomFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_join_chat_room, container, false)

        val roomIdInput = view.findViewById<EditText>(R.id.editJoinRoomId)
        val joinButton = view.findViewById<Button>(R.id.buttonJoin)

        joinButton.setOnClickListener {
            val roomIdStr = roomIdInput.text.toString()
            val userId = LoginSession.userId

            if (roomIdStr.isBlank() || userId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "방 ID 또는 로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roomId = roomIdStr.toIntOrNull()
            if (roomId == null || roomId <= 0) {
                Toast.makeText(requireContext(), "올바른 방 ID를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 서버에 참여 요청
            ChatRoomManager.inviteMember(roomId, userId) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        // 🔄 참여 후 목록 새로고침
                        ChatRoomManager.getJoinedChatRooms(userId) { _ ->
                            requireActivity().runOnUiThread {
                                // 채팅방으로 이동
                                val action = JoinChatRoomFragmentDirections
                                    .actionJoinChatRoomFragmentToChatRoomFragment(roomId, userId)
                                findNavController().navigate(action)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "채팅방 참여 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return view
    }
}