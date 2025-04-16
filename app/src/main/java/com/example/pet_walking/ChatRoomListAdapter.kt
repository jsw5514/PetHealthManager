package com.example.pet_walking.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pet_walking.R

class ChatRoomListAdapter(
    private val roomList: List<Pair<Int, String>>,
    private val onItemClick: (Int, String) -> Unit
) : RecyclerView.Adapter<ChatRoomListAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roomInfo: TextView = view.findViewById(R.id.textRoomInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun getItemCount(): Int = roomList.size

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val (roomId, creatorId) = roomList[position]
        holder.roomInfo.text = "채팅방 ID: $roomId | 생성자: $creatorId"
        holder.itemView.setOnClickListener {
            onItemClick(roomId, creatorId)
        }
    }
}