package com.example.pet_walking.feature.Chat

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.UserRepository
import com.example.pet_walking.feature.Login.LoginSession

class CreateChatRoomFragment : Fragment() {

    private lateinit var roomNameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var createRoomButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_create_chat_room, container, false)

        roomNameEditText = view.findViewById(R.id.roomNameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        createRoomButton = view.findViewById(R.id.createRoomButton)

        createRoomButton.setOnClickListener {
            val roomName = roomNameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val creatorId = UserRepository.getCurrentUserId()

            if (creatorId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return@setOnClickListener
            }

            if (roomName.isBlank()) {
                Toast.makeText(requireContext(), "채팅방 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ChatRoomManager.createChatRoom(creatorId, roomName, password) { roomId ->
                requireActivity().runOnUiThread {
                    if (roomId > 0) {
                        Toast.makeText(requireContext(), "채팅방 생성 완료!", Toast.LENGTH_SHORT).show()
                        val action = CreateChatRoomFragmentDirections.actionCreateChatRoomFragmentToChatRoomFragment(
                            roomId = roomId,
                            userId = creatorId
                        )
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return view
    }
}