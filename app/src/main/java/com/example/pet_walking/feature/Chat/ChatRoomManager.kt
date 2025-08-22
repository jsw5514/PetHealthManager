package com.example.pet_walking.feature.Chat
/*
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject

object ChatRoomManager {

    fun createChatRoom(creatorId: String, roomName: String, password: String?, callback: (Int) -> Unit) {
        val json = JSONObject().apply {
            put("creatorId", creatorId)
            put("roomName", roomName)
            if (!password.isNullOrBlank()) put("password", password)
        }

        ApiClient.post("/createChatRoom", json,
            onSuccess = { response ->
                val roomId = response.trim('"').toIntOrNull()?:0
                callback(roomId)
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
            onSuccess = { response ->
                // ✅ 문자열 양끝 따옴표 제거 후 비교
                callback(response.trim('"').equals("true", ignoreCase = true))
            },
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
            onFailure = {
                callback(0)
            }
        )
    }
}*/

import com.example.pet_walking.network.ApiClient
import org.json.JSONArray
import org.json.JSONObject

object ChatRoomManager {

    /** 채팅방 생성: POST /chat/room  */
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
                // 서버가 {"roomId":123} 또는 "123" 둘 중 하나를 줄 수 있으니 모두 대응
                val roomId = try {
                    JSONObject(body).optInt("roomId", 0)
                } catch (_: Exception) {
                    body.trim('"').toIntOrNull() ?: 0
                }
                callback(roomId)
            },
            onFailure = { callback(0) }
        )
    }

    /** 채팅방 초대: POST /chat/room/{roomId}/member */
    fun inviteMember(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply { put("memberId", memberId) }

        ApiClient.post("/chat/room/$roomId/member", json,
            onSuccess = { body ->
                // 2xx면 성공으로 취급, 혹시 "false" 내려오면 실패 처리
                val ok = when {
                    body.trim().equals("false", true) -> false
                    else -> true
                }
                callback(ok)
            },
            onFailure = { callback(false) }
        )
    }

    /** 채팅방 나가기: DELETE /chat/room/{roomId}/member/{memberId} */
    fun leaveChatRoom(roomId: Int, memberId: String, callback: (Boolean) -> Unit) {
        // ApiClient에 delete가 있어야 함 (아래 참고)
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
                    // 배열이 문자열 배열 or 객체 배열일 수 있어 모두 처리
                    val arr = try { JSONArray(body) } catch (_: Exception) {
                        // 혹시 {"members":[...]} 형태면 꺼내서 파싱
                        val obj = JSONObject(body)
                        obj.optJSONArray("members") ?: JSONArray()
                    }
                    for (i in 0 until arr.length()) {
                        val v = arr.get(i)
                        when (v) {
                            is String -> members.add(v)
                            is JSONObject -> {
                                // 우선순위대로 키 탐색
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

    /* ▼ 아래 두 개는 변경 공지가 없어 일단 기존대로 둠.
       서버 스펙이 갱신되면 엔드포인트만 바꿔주면 됨. */

    fun getJoinedChatRooms(userId: String, callback: (List<Pair<Int, String>>) -> Unit) {
        val json = JSONObject().apply { put("userId", userId) }
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
                        rooms.add(roomId to creatorId)
                    }
                } catch (_: Exception) { /* ignore */ }
                callback(rooms)
            },
            onFailure = { callback(emptyList()) }
        )
    }

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