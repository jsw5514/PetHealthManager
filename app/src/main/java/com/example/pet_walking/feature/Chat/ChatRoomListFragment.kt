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
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.UserRepository

class ChatRoomListFragment : Fragment() {

    private val joinedRooms = mutableListOf<Pair<Int, String>>()  // roomId, creatorId
    private lateinit var adapter: ChatRoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // ── 뷰 inflate ─────────────────────────────────────────────
        val view = inflater.inflate(
            R.layout.fragment_chat_room_list,
            container,
            false
        )

        // ── 위젯 참조 ─────────────────────────────────────────────
        val createBtn   = view.findViewById<Button>(R.id.buttonCreateRoom)
        val joinBtn     = view.findViewById<Button>(R.id.buttonJoinRoom)
        val recycler    = view.findViewById<RecyclerView>(R.id.recyclerChatRooms)

        // ── 로그인 사용자인지 확인 ─────────────────────────────────
        val userId = UserRepository.getCurrentUserId()
        Log.d("ChatRoomListFragment", "userId = $userId")

        if (!userId.isNullOrBlank()) {

            // 리스트-어댑터 초기화
            adapter = ChatRoomListAdapter(joinedRooms) { roomId, _ ->
                val action = ChatRoomListFragmentDirections
                    .actionChatRoomListFragmentToChatRoomFragment(roomId, userId)
                findNavController().navigate(action)
            }

            recycler.layoutManager = LinearLayoutManager(requireContext())
            recycler.adapter       = adapter

            // 참여 중인 채팅방 가져오기
            refreshChatRoomList(userId)
        }

        // ── 채팅방 “생성” 버튼 ────────────────────────────────────
        createBtn.setOnClickListener {
            Log.d("ChatRoomListFragment", "👆 생성 버튼 클릭됨")
            val currentUser = UserRepository.getCurrentUserId()     // ✅ 변경
            if (currentUser.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            } else {
                findNavController()
                    .navigate(R.id.action_chatRoomListFragment_to_createChatRoomFragment)
            }
        }

        // ── 채팅방 “참여” 버튼 ────────────────────────────────────
        joinBtn.setOnClickListener {
            val currentUser = UserRepository.getCurrentUserId()     // ✅ 변경
            if (currentUser.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            } else {
                findNavController()
                    .navigate(R.id.action_chatRoomListFragment_to_joinChatRoomFragment)
            }
        }

        return view
    }

    private fun refreshChatRoomList(userId: String) {
        ChatRoomManager.getJoinedChatRooms(userId) { rooms ->
            activity?.runOnUiThread {
                joinedRooms.apply {
                    clear()
                    addAll(rooms)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
}