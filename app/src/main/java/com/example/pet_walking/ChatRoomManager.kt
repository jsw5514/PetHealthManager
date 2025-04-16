package com.example.pet_walking.chat

import com.example.pet_walking.LoginSession
import okhttp3.*
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ChatRoomManager {
    private val client = OkHttpClient()
    private const val serverUrl = "http://10.0.2.2:8080"

    fun createChatRoom(creatorId: String, callback: (Int) -> Unit) {
        val json = JSONObject().apply {
            put("creatorId", creatorId)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/createChatRoom")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(0)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()?.toIntOrNull() ?: 0
                callback(body)
            }
        })
    }

    fun inviteMember(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("roomId", roomId)
            put("memberId", memberId)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/inviteChatMember")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.body?.string() == "true")
            }
        })
    }

    fun leaveChatRoom(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("roomId", roomId)
            put("memberId", memberId)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/leaveChatRoom")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.body?.string() == "true")
            }
        })
    }

    fun getChatMembers(roomId: Int, callback: (List<String>) -> Unit) {
        val json = JSONObject().apply {
            put("roomId", roomId)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/getChatMember")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                val members = mutableListOf<String>()
                if (result != null) {
                    val jsonArray = JSONObject("{\"list\":$result}").getJSONArray("list")
                    for (i in 0 until jsonArray.length()) {
                        members.add(jsonArray.getString(i))
                    }
                }
                callback(members)
            }
        })
    }

    fun getJoinedChatRooms(userId: String, callback: (List<Pair<Int, String>>) -> Unit) {
        val json = JSONObject().apply {
            put("userId", userId)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/getJoinedRooms")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                val rooms = mutableListOf<Pair<Int, String>>()
                try {
                    val jsonObj = JSONObject(result)
                    val roomArray = jsonObj.getJSONArray("rooms")
                    for (i in 0 until roomArray.length()) {
                        val room = roomArray.getJSONObject(i)
                        val roomId = room.getInt("roomId")
                        val creatorId = room.getString("creatorId")
                        rooms.add(Pair(roomId, creatorId))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                callback(rooms)
            }
        })
    }
}