package com.example.pet_walking.feature.Chat

import android.util.Log
import com.example.pet_walking.network.ApiClient
import org.json.JSONArray
import org.json.JSONObject

object ChatRoomManager {

    /** 채팅방 생성: POST /chat/room */
    fun createChatRoom(
        creatorId: String,
        roomName: String,
        password: String?,
        callback: (Int) -> Unit
    ) {
        val json = JSONObject().apply {
            put("creatorId", creatorId)
            put("roomName", roomName)
            if (!password.isNullOrBlank()) put("password", password)
        }

        ApiClient.post("/chat/room", json,
            onSuccess = { body ->
                val roomId = try {
                    JSONObject(body).optInt("roomId", 0)      // {"roomId":123}
                } catch (_: Exception) {
                    body.trim('"').toIntOrNull() ?: 0         // "123"
                }
                callback(roomId)
            },
            onFailure = { callback(0) }
        )
    }

    /** 멤버 초대: POST /chat/room/{roomId}/member  (body: {"memberId": "<id>"}) */
    fun inviteMember(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply { put("memberId", memberId) }

        ApiClient.post("/chat/room/$roomId/member", json,
            onSuccess = { body ->
                // 빈 본문(204/200)도 성공으로 간주. 서버가 "false"만 보내면 실패 처리.
                val ok = !body.trim().equals("false", ignoreCase = true)
                callback(ok)
            },
            onFailure = { callback(false) }
        )
    }

    /** 채팅방 나가기: DELETE /chat/room/{roomId}/member/{memberId} */
    fun leaveChatRoom(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        ApiClient.delete("/chat/room/$roomId/member/$memberId",
            onSuccess = { _ -> callback(true) },
            onFailure = { _ -> callback(false) }
        )
    }

    /** 채팅방 멤버 조회: GET /chat/room/{roomId}/member */
    fun getChatMembers(roomId: Int, callback: (List<String>) -> Unit) {
        ApiClient.getByEndpoint("/chat/room/$roomId/member",
            onSuccess = { body ->
                try {
                    val members = mutableListOf<String>()
                    val arr = try { JSONArray(body) } catch (_: Exception) {
                        JSONObject(body).optJSONArray("members") ?: JSONArray()
                    }
                    for (i in 0 until arr.length()) {
                        val v = arr.get(i)
                        when (v) {
                            is String -> members.add(v)
                            is JSONObject -> {
                                val name = v.optString("nickname",
                                    v.optString("memberId",
                                        v.optString("userId", "")))
                                if (name.isNotBlank()) members.add(name)
                            }
                        }
                    }
                    callback(members)
                } catch (_: Exception) {
                    callback(emptyList())
                }
            },
            onFailure = { callback(emptyList()) }
        )
    }

    /**
     * 참여 중인 채팅방 목록: POST /char/room/list
     * body: {"userId":"<id>"}
     * 응답은 배열 또는 {"rooms":[...]} 형태 모두 처리
     */
    fun getJoinedChatRooms(userId: String, callback: (List<Pair<Int, String>>) -> Unit) {
        val body = JSONObject().put("userId", userId)

        ApiClient.post("/chat/room/list", body,
            onSuccess = { response ->
                val rooms = mutableListOf<Pair<Int, String>>()
                try {
                    val txt = response.trim()
                    val arr: JSONArray = when {
                        txt.startsWith("[") -> JSONArray(txt)
                        else -> {
                            val obj = JSONObject(txt)
                            obj.optJSONArray("rooms")
                                ?: obj.optJSONArray("list")
                                ?: JSONArray()
                        }
                    }
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val roomId = o.optInt("roomId", o.optInt("id", 0))
                        val creatorId = o.optString(
                            "creatorId",
                            o.optString("ownerId", o.optString("creator", ""))
                        )
                        if (roomId != 0) rooms.add(roomId to creatorId)
                    }
                } catch (e: Exception) {
                    Log.e("ChatRoomManager", "getJoinedChatRooms parse error", e)
                }
                callback(rooms)
            },
            onFailure = {
                Log.e("ChatRoomManager", "getJoinedChatRooms api failure")
                callback(emptyList())
            }
        )
    }

    /**
     * (레거시) 임의 참가 API – 서버에 없으면 사용하지 마세요.
     * 현재 서버가 초대(Invite) 방식만 지원하면 이 함수는 쓰이지 않습니다.
     */
    fun joinChatRoom(userId: String, roomName: String, password: String, callback: (Int) -> Unit) {
        val json = JSONObject().apply {
            put("userId", userId)
            put("roomName", roomName)
            put("password", password)
        }
        ApiClient.post("/joinChatRoom", json,
            onSuccess = { response ->
                val roomId = response.trim('"').toIntOrNull() ?: 0
                callback(roomId)
            },
            onFailure = { callback(0) }
        )
    }
}