package com.example.pet_walking

import android.content.Context
import android.util.Log
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject
import java.util.*

object UserRepository {
    private val users = mutableMapOf<String, UserProfile>() // 서버 연동 시 로컬 저장은 캐시로 사용 가능
    private var loggedInUserId: String? = null

    // ✅ 서버와 연동된 회원가입
    fun registerUser(profile: UserProfile): Boolean {
        var result = false
        val json = JSONObject().apply {
            put("id", profile.userId)
            put("password", profile.password)
        }

        val lock = Object()
        ApiClient.post("/signIn", json,
            onSuccess = {
                result = it.toBooleanStrictOrNull() == true
                if (result) {
                    users[profile.userId] = profile
                    loggedInUserId = profile.userId
                }
                synchronized(lock) { lock.notify() }
            },
            onFailure = {
                Log.e("UserRepository", "회원가입 실패: $it")
                synchronized(lock) { lock.notify() }
            }
        )

        synchronized(lock) { lock.wait(3000) } // 최대 3초 대기
        return result
    }

    // ✅ 서버와 연동된 로그인
    fun login(userId: String, password: String): Boolean {
        var result = false
        val json = JSONObject().apply {
            put("id", userId)
            put("password", password)
        }

        val lock = Object()
        ApiClient.post("/login", json,
            onSuccess = {
                result = it.toBooleanStrictOrNull() == true
                if (result) {
                    loggedInUserId = userId
                    if (!users.containsKey(userId)) {
                        users[userId] = UserProfile(
                            userId = userId,
                            username = "알 수 없음", // 서버에서 사용자 정보 받아오지 않음 (확장 필요)
                            birthdate = "",
                            gender = "",
                            password = password
                        )
                    }
                }
                synchronized(lock) { lock.notify() }
            },
            onFailure = {
                Log.e("UserRepository", "로그인 실패: $it")
                synchronized(lock) { lock.notify() }
            }
        )

        synchronized(lock) { lock.wait(3000) }
        return result
    }

    fun logout() {
        loggedInUserId = null
    }

    // ✅ 현재 로그인한 유저 정보 반환
    fun getCurrentUser(): UserProfile? = loggedInUserId?.let { users[it] }

    // ✅ 현재 로그인한 유저의 ID 반환 (단순 추출용)
    fun getCurrentUserId(): String? = loggedInUserId

    fun addPetToCurrentUser(petId: UUID) {
        getCurrentUser()?.petIds?.add(petId)
    }

    // ✅ 로컬 SharedPreferences 저장은 캐시용으로 남겨둠 (선택적으로 사용)
    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val json = com.google.gson.Gson().toJson(users)
        prefs.edit().putString("users", json).apply()
        prefs.edit().putString("loggedInUserId", loggedInUserId).apply()
    }

    fun loadFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val json = prefs.getString("users", null)
        val savedId = prefs.getString("loggedInUserId", null)

        json?.let {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, UserProfile>>() {}.type
            val restored = com.google.gson.Gson().fromJson<Map<String, UserProfile>>(it, type)
            users.clear()
            users.putAll(restored)
        }

        loggedInUserId = savedId
    }
}