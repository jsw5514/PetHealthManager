package com.example.pet_walking.Chat

import android.os.Bundle
import android.util.Log // 로그용 import 추가
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.Login.LoginSession
import com.example.pet_walking.R

class CreateChatRoomFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_create_chat_room, container, false)
        val button = view.findViewById<Button>(R.id.buttonCreateRoom)

        button.setOnClickListener {
            Log.d("CreateChatRoom", "🔘 생성 버튼 클릭됨") // ✅ 버튼 클릭 로그

            val userId = LoginSession.userId
            if (userId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                Log.w("CreateChatRoom", "❗ 로그인 정보 없음. 생성 불가") // ✅ 경고 로그
                return@setOnClickListener
            }

            Log.d("CreateChatRoom", "👤 현재 로그인 ID: $userId") // ✅ 유저 ID 출력

            button.isEnabled = false
            ChatRoomManager.createChatRoom(userId) { roomId ->
                requireActivity().runOnUiThread {
                    button.isEnabled = true
                    if (roomId > 0) {
                        Log.d("CreateChatRoom", "✅ 채팅방 생성 성공 - ID: $roomId") // ✅ 성공 로그
                        val action = CreateChatRoomFragmentDirections
                            .actionCreateChatRoomFragmentToChatRoomFragment(roomId, userId)
                        findNavController().navigate(action)
                    } else {
                        Log.e("CreateChatRoom", "❌ 채팅방 생성 실패") // ✅ 실패 로그
                        Toast.makeText(requireContext(), "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return view
    }
}