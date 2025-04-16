package com.example.pet_walking.chat

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.LoginSession
import com.example.pet_walking.R
import org.json.JSONObject

class CreateChatRoomFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_create_chat_room, container, false)

        val button = view.findViewById<Button>(R.id.buttonCreateRoom)
        button.setOnClickListener {
            val userId = LoginSession.userId
            if (userId.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ChatRoomManager.createChatRoom(userId) { roomId ->
                activity?.runOnUiThread {
                    if (roomId > 0) {
                        // 🔄 채팅방 목록 갱신
                        ChatRoomManager.getJoinedChatRooms(userId) { updatedRooms ->
                            requireActivity().runOnUiThread {
                                // navBack + 결과 반영
                                findNavController().popBackStack()
                            }
                        }

                        // 바로 입장도 가능하면 아래 유지
                        val action = CreateChatRoomFragmentDirections
                            .actionCreateChatRoomFragmentToChatRoomFragment(roomId, userId)
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