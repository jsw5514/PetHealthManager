package com.example.pet_walking.feature.Chat

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_walking.feature.Login.LoginSession
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.UserRepository

class ChatRoomListFragment : Fragment() {

    private val joinedRooms = mutableListOf<Pair<Int, String>>()  // roomId, creatorId
    private lateinit var adapter: ChatRoomListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_chat_room_list, container, false)

        val createBtn = view.findViewById<Button>(R.id.buttonCreateRoom)
        val joinBtn = view.findViewById<Button>(R.id.buttonJoinRoom)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerChatRooms)

        val userId = UserRepository.getCurrentUserId()

        // 로그인된 경우에만 어댑터와 채팅방 목록 초기화
        if (!userId.isNullOrBlank()) {
            adapter = ChatRoomListAdapter(joinedRooms) { roomId, creatorId ->
                val action = ChatRoomListFragmentDirections
                    .actionChatRoomListFragmentToChatRoomFragment(roomId, userId)
                findNavController().navigate(action)
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter

            // 참여 중인 채팅방 목록 불러오기
            ChatRoomManager.getJoinedChatRooms(userId) { rooms ->
                activity?.runOnUiThread {
                    joinedRooms.clear()
                    joinedRooms.addAll(rooms)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        // ✅ 생성 버튼 클릭 시점에서 로그인 여부 확인
        createBtn.setOnClickListener {
            Log.d("ChatRoomListFragment", "👆 생성 버튼 클릭됨")
            val currentUser = LoginSession.userId
            if (currentUser.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_chatRoomListFragment_to_createChatRoomFragment)
            }
        }

        // ✅ 참여 버튼 클릭 시점에서 로그인 여부 확인
        joinBtn.setOnClickListener {
            val currentUser = LoginSession.userId
            if (currentUser.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_chatRoomListFragment_to_joinChatRoomFragment)
            }
        }

        return view
    }

    private fun refreshChatRoomList(userId: String) {
        ChatRoomManager.getJoinedChatRooms(userId) { rooms ->
            activity?.runOnUiThread {
                joinedRooms.clear()
                joinedRooms.addAll(rooms)
                adapter.notifyDataSetChanged()
            }
        }
    }
}