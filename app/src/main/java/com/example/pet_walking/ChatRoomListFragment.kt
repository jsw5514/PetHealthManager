package com.example.pet_walking.chat

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_walking.LoginSession
import com.example.pet_walking.R

class ChatRoomListFragment : Fragment() {

    private val joinedRooms = mutableListOf<Pair<Int, String>>()  // roomId, creatorId
    private lateinit var adapter: ChatRoomListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_chat_room_list, container, false)

        val createBtn = view.findViewById<Button>(R.id.buttonCreateRoom)
        val joinBtn = view.findViewById<Button>(R.id.buttonJoinRoom)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerChatRooms)

        val userId = LoginSession.userId
        if (userId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return view
        }

        adapter = ChatRoomListAdapter(joinedRooms) { roomId, creatorId ->
            val action = ChatRoomListFragmentDirections
                .actionChatRoomListFragmentToChatRoomFragment(roomId, userId)
            findNavController().navigate(action)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 🔄 서버에서 참여 중인 채팅방 목록 받아오기
        ChatRoomManager.getJoinedChatRooms(userId) { rooms ->
            activity?.runOnUiThread {
                joinedRooms.clear()
                joinedRooms.addAll(rooms)
                adapter.notifyDataSetChanged()
            }
        }

        createBtn.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomListFragment_to_createChatRoomFragment)
        }

        joinBtn.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomListFragment_to_joinChatRoomFragment)
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