package com.example.pet_walking.Chat

import com.example.pet_walking.network.ApiClient
import org.json.JSONObject

object ChatRoomManager {

    fun createChatRoom(creatorId: String, callback: (Int) -> Unit) {
        val json = JSONObject().apply {
            put("creatorId", creatorId)
        }

        ApiClient.post("/createChatRoom", json,
            onSuccess = { response ->
                callback(response.toIntOrNull() ?: 0)
            },
            onFailure = {
                callback(0)
            }
        )
    }

    fun inviteMember(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("roomId", roomId)
            put("memberId", memberId)
        }

        ApiClient.post("/inviteChatMember", json,
            onSuccess = { response -> callback(response == "true") },
            onFailure = { callback(false) }
        )
    }

    fun leaveChatRoom(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("roomId", roomId)
            put("memberId", memberId)
        }

        ApiClient.post("/leaveChatRoom", json,
            onSuccess = { response -> callback(response == "true") },
            onFailure = { callback(false) }
        )
    }

    fun getChatMembers(roomId: Int, callback: (List<String>) -> Unit) {
        val json = JSONObject().apply {
            put("roomId", roomId)
        }

        ApiClient.post("/getChatMember", json,
            onSuccess = { response ->
                try {
                    val result = JSONObject("{\"list\":$response}")
                    val members = mutableListOf<String>()
                    val array = result.getJSONArray("list")
                    for (i in 0 until array.length()) {
                        members.add(array.getString(i))
                    }
                    callback(members)
                } catch (e: Exception) {
                    callback(emptyList())
                }
            },
            onFailure = { callback(emptyList()) }
        )
    }

    fun getJoinedChatRooms(userId: String, callback: (List<Pair<Int, String>>) -> Unit) {
        val json = JSONObject().apply {
            put("userId", userId)
        }

        ApiClient.post("/getJoinedRooms", json,
            onSuccess = { response ->
                val rooms = mutableListOf<Pair<Int, String>>()
                try {
                    val jsonObj = JSONObject(response)
                    val roomArray = jsonObj.getJSONArray("rooms")
                    for (i in 0 until roomArray.length()) {
                        val room = roomArray.getJSONObject(i)
                        val roomId = room.getInt("roomId")
                        val creatorId = room.getString("creatorId")
                        rooms.add(Pair(roomId, creatorId))
                    }
                } catch (_: Exception) {}
                callback(rooms)
            },
            onFailure = { callback(emptyList()) }
        )
    }
}