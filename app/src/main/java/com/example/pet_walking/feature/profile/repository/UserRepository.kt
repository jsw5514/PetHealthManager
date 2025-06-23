/*package com.example.pet_walking.feature.profile.repository

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

    fun addPetToCurrentUser(id: UUID) {
        getCurrentUser()?.petIds?.add(id)
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
}*/
/*  UserRepository.kt
 *  ──────────────────────────────────────────────────────────────────
 *  • 사용자 + 펫(profile) 캐시 관리
 *  • 로그인 시 서버가 내려주는 JSON 전체를 파싱
 *      ├─ 사용자 프로필        → users 맵
 *      └─ `pets` 배열         → PetRepository.injectPetsFromJson()
 *  • SharedPreferences 로 오프라인 캐싱
 *  • 레거시(boolean) / 신규(JSON) 두 가지 응답 모두 지원
 */
package com.example.pet_walking.feature.profile.repository

import android.content.Context
import android.util.Log
import com.example.pet_walking.feature.profile.data.PetProfile
import com.example.pet_walking.feature.profile.data.UserProfile
import com.example.pet_walking.network.ApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object UserRepository {

    /*────────────────── 캐시 ──────────────────*/
    private val users = mutableMapOf<String, UserProfile>()   // userId → UserProfile
    private var loggedInUserId: String? = null                // 현재 로그인 ID

    /* getter 도우미 */
    fun getCurrentUser(): UserProfile?   = loggedInUserId?.let { users[it] }
    fun getCurrentUserId():   String?    = loggedInUserId
    private fun setCurrentUser(u: UserProfile) { users[u.userId] = u; loggedInUserId = u.userId }
    fun addPetToCurrentUser(id: UUID)    { getCurrentUser()?.petIds?.add(id) }

    /*──────────────── SharedPreferences ────────────────*/
    fun saveToPreferences(ctx: Context) {
        ctx.getSharedPreferences("UserData", Context.MODE_PRIVATE)
            .edit()
            .putString("users", Gson().toJson(users))
            .putString("loggedInUserId", loggedInUserId)
            .apply()
        Log.d("UserRepo", "🗄️  cache saved (${users.size} users)")
    }

    fun loadFromPreferences(ctx: Context) {
        val p = ctx.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        p.getString("users", null)?.let {
            val type = object : TypeToken<Map<String, UserProfile>>() {}.type
            users.clear(); users.putAll(Gson().fromJson(it, type))
        }
        loggedInUserId = p.getString("loggedInUserId", null)
        Log.d("UserRepo", "🗄️  cache loaded (loggedIn=$loggedInUserId)")
    }

    fun logout(ctx: Context? = null) {
        loggedInUserId = null
        ctx?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
            ?.edit()?.remove("loggedInUserId")?.apply()
        Log.d("UserRepo", "🔒 로그아웃 완료")
    }

    /*────────────────── 로그인 ──────────────────*/
    fun login(
        userId: String,
        password: String,
        callback: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        val req = JSONObject()
            .put("id", userId)
            .put("password", password)

        Log.d("UserRepo", "📤 POST /login → $req")

        ApiClient.post("/login", req,
            onSuccess = { raw ->
                Log.d("UserRepo", "📥 /login 응답 → $raw")

                /* (1) 레거시 boolean 응답 */
                raw.trim().let { plain ->
                    if (plain == "true" || plain == "false") {
                        val ok = plain == "true"
                        if (ok) setCurrentUser(
                            UserProfile(
                                username = "",
                                userId   = userId,
                                password = password,
                                petIds   = mutableListOf()
                            )
                        )
                        callback(ok, if (ok) null else "ID/PW 불일치")
                        return@post
                    }
                }

                /* (2) JSON 응답 파싱 */
                try {
                    val obj = JSONObject(raw)

                    /* 사용자 프로필 */
                    val me = UserProfile(
                        username = obj.optString("nickname", ""),
                        userId   = obj.getString("id"),
                        password = obj.getString("password"),
                        petIds   = mutableListOf()
                    )
                    setCurrentUser(me)

                    /* 펫 배열 → PetRepository */
                    injectPetsFromJson(obj.optJSONArray("pets") ?: JSONArray())

                    callback(true, null)

                } catch (e: Exception) {
                    Log.e("UserRepo", "❌ JSON 파싱 오류: ${e.message}")
                    callback(false, "파싱 실패: ${e.message}")
                }
            },
            onFailure = { err ->
                Log.e("UserRepo", "❌ /login 네트워크 오류: $err")
                callback(false, "네트워크 오류: $err")
            }
        )
    }

    /*────────────────── 회원가입(동기) ──────────────────*/
    fun registerUser(profile: UserProfile): Boolean {
        var ok = false
        val body = JSONObject()
            .put("id", profile.userId)
            .put("password", profile.password)

        val lock = Object()

        ApiClient.post("/signIn", body,
            onSuccess = {
                ok = it.trim().toBooleanStrictOrNull() == true
                if (ok) setCurrentUser(profile)
                synchronized(lock) { lock.notify() }
            },
            onFailure = { err ->
                Log.e("UserRepo", "❌ 회원가입 실패: $err")
                synchronized(lock) { lock.notify() }
            })

        synchronized(lock) { lock.wait(3000) }   // 최대 3초 대기
        return ok
    }

    /*──────────────── pets 배열 → PetRepository ────────────────*/
    private fun injectPetsFromJson(arr: JSONArray) {
        val petRepo = PetRepository
        petRepo.profiles.clear()

        Log.d("UserRepo", "🔄 pets 배열 파싱 (size=${arr.length()})")

        /* 잘못된 UUID 대비 안전 변환 */
        fun safeUuid(raw: String): UUID? =
            try { UUID.fromString(raw) }
            catch (_: IllegalArgumentException) {
                Log.e("UserRepo", "잘못된 UUID '$raw' → 스킵"); null
            }

        for (i in 0 until arr.length()) {
            val j = arr.getJSONObject(i)
            val id = safeUuid(j.getString("petId")) ?: continue

            val pet = PetProfile(
                id             = id,
                name           = j.getString("name"),
                age            = j.getString("age"),
                gender         = j.getString("gender"),
                weight         = j.getDouble("weight"),
                imageUri       = j.optString("imgUrl").ifBlank { null },
                totalDistance  = j.optDouble("totalDistance", 0.0),
                totalCalories  = j.optDouble("totalCalories", 0.0)
            )

            petRepo.profiles[id] = pet
            users[loggedInUserId]?.petIds?.add(id)
            Log.d("UserRepo", "  ✓ pet[$i] 캐시 → $id")
        }

        /* 첫 번째 펫을 기본 선택 */
        petRepo.setCurrentPet(petRepo.profiles.keys.firstOrNull())
        Log.d("UserRepo", "✅ 총 ${petRepo.profiles.size}마리 캐시 완료")
    }
}