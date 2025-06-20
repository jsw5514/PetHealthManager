package com.example.pet_walking.feature.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.UserRepository

class JoinChatRoomFragment : Fragment() {

    private lateinit var editRoomId: EditText   // ← id 를 layout 과 맞춰 주세요!
    private lateinit var joinBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_join_chat_room, container, false)

        editRoomId = v.findViewById(R.id.editJoinRoomId)      // 또는 editJoinRoomId
        joinBtn    = v.findViewById(R.id.buttonJoin)

        joinBtn.setOnClickListener {
            val roomId = editRoomId.text.toString().toIntOrNull()
            val userId = UserRepository.getCurrentUserId()

            if (roomId == null) {
                Toast.makeText(requireContext(), "채팅방 ID를 숫자로 입력해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (userId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val action = JoinChatRoomFragmentDirections
                .actionJoinChatRoomFragmentToChatRoomFragment(roomId, userId)
            findNavController().navigate(action)
        }
        return v
    }
}