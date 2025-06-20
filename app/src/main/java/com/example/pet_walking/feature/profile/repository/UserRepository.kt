package com.example.pet_walking.feature.profile.repository

import android.content.Context
import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.profile.data.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.util.UUID

object UserRepository {
    // 로컬 캐시용 맵
    private val users = mutableMapOf<String, UserProfile>()
    private var loggedInUserId: String? = null

    /** 현재 로그인한 유저 정보 반환 */
    fun getCurrentUser(): UserProfile? =
        loggedInUserId?.let { users[it] }

    /** 현재 로그인한 유저 ID 반환 */
    fun getCurrentUserId(): String? = loggedInUserId

    /** 캐시에 새 유저 등록 */
    fun setCurrentUser(user: UserProfile) {
        users[user.userId] = user
        loggedInUserId = user.userId
    }

    fun addPetToCurrentUser(petId: UUID) {
        getCurrentUser()?.petIds?.add(petId)
    }

    /** SharedPreferences에 캐시 저장 */
    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("users", Gson().toJson(users))
            .putString("loggedInUserId", loggedInUserId)
            .apply()
    }

    /** SharedPreferences에서 캐시 불러오기 */
    fun loadFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        prefs.getString("users", null)?.let { json ->
            val type = object : TypeToken<Map<String, UserProfile>>() {}.type
            val restored: Map<String, UserProfile> = Gson().fromJson(json, type)
            users.clear()
            users.putAll(restored)
        }
        loggedInUserId = prefs.getString("loggedInUserId", null)
        Log.d("UserRepository", "[DEBUG] loggedInUserId → $loggedInUserId")
    }

    /** 로그아웃 처리 */
    fun logout(context: Context? = null) {
        loggedInUserId = null
        context?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
            ?.edit()
            ?.remove("loggedInUserId")
            ?.apply()
    }

    /**
     * 서버 연동 로그인 (비동기 콜백)
     *
     * @param userId   로그인 할 유저 ID
     * @param password 비밀번호
     * @param callback (성공 여부, 에러 메시지) -> Unit
     */
    fun login(
        userId: String,
        password: String,
        callback: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        val json = JSONObject().apply {
            put("id", userId)
            put("password", password)
        }

        ApiClient.post(
            endpoint = "/login",
            json = json,
            onSuccess = { resp ->
                // legacy: handle plain "true"/"false" responses
                val trimmed = resp.trim()
                if (trimmed == "true" || trimmed == "false") {
                    val successFlag = trimmed == "true"
                    if (successFlag) {
                        // simple user profile when only boolean success is returned
                        val profile = UserProfile(
                            userId   = userId,
                            username = "",
                            password = password,
                            petIds   = mutableListOf()
                        )
                        setCurrentUser(profile)
                        callback(true, null)
                    } else {
                        callback(false, "아이디 또는 비밀번호가 올바르지 않습니다.")
                    }
                    return@post
                }
                try {
                    val obj = JSONObject(resp)
                    val success = obj.optBoolean("success", false)
                    if (success) {
                        // 서버에서 내려준 정보 파싱
                        val username = obj.optString("username", "")
                        val pwdFromServer = obj.optString("password", password)

                        // petIds 배열 파싱
                        val petIds = mutableListOf<UUID>()
                        obj.optJSONArray("petIds")?.let { arr ->
                            for (i in 0 until arr.length()) {
                                petIds.add(UUID.fromString(arr.getString(i)))
                            }
                        }

                        // 캐시에 저장
                        val profile = UserProfile(
                            userId   = userId,
                            username = username,
                            password = pwdFromServer,
                            petIds   = petIds.toMutableList()
                        )
                        setCurrentUser(profile)
                        callback(true, null)
                    } else {
                        callback(false, "아이디 또는 비밀번호가 올바르지 않습니다.")
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "로그인 응답 파싱 오류: ${e.message}")
                    callback(false, "응답 파싱 오류: ${e.message}")
                }
            },
            onFailure = { err ->
                Log.e("UserRepository", "로그인 요청 실패: $err")
                callback(false, "서버 요청 실패: $err")
            }
        )
    }

    /**
     * 서버 연동 회원가입 (동기 방식)
     *
     * @return 성공 여부
     */
    fun registerUser(profile: UserProfile): Boolean {
        var result = false
        val json = JSONObject().apply {
            put("id", profile.userId)
            put("password", profile.password)
        }
        val lock = Object()
        ApiClient.post(
            endpoint = "/signIn",
            json = json,
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
        synchronized(lock) { lock.wait(3000) }  // 최대 3초 대기
        return result
    }
}