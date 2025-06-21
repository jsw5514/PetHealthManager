package com.example.pet_walking.feature.Chat

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.UserRepository
import org.json.JSONObject

class JoinChatRoomFragment : Fragment() {

    private lateinit var editRoomName: EditText
    private lateinit var editPassword: EditText
    private lateinit var joinBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_join_chat_room, container, false)
        editRoomName = v.findViewById(R.id.editRoomName)
        editPassword = v.findViewById(R.id.editPassword)
        joinBtn = v.findViewById(R.id.buttonJoin)

        joinBtn.setOnClickListener {
            val roomName = editRoomName.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val userId = UserRepository.getCurrentUserId()

            if (roomName.isBlank() || userId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "입력 정보를 확인하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ChatRoomManager.joinChatRoom(userId, roomName, password) { roomId ->
                requireActivity().runOnUiThread {
                    if (roomId > 0) {
                        Toast.makeText(requireContext(), "참여 완료", Toast.LENGTH_SHORT).show()
                        val action = JoinChatRoomFragmentDirections
                            .actionJoinChatRoomFragmentToChatRoomFragment(roomId, userId)
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "참여 실패: 이름 또는 비밀번호가 틀립니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return v
    }
}