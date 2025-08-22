/*package com.example.pet_walking.feature.profile.repository

import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.profile.data.PetProfile
import org.json.JSONObject
import java.util.*

object PetRepository {
    //private val profiles = mutableMapOf<UUID, PetProfile>()
    internal val profiles = mutableMapOf<UUID, PetProfile>()
    var currentPetId: UUID? = null

    init {
        Log.d("PetRepo", "PetRepository initialized")
    }

    /**
     * 로컬에 추가 후 서버에 업로드합니다.
     */
    fun addProfile(
        profile: PetProfile,
        userId: String,
        onComplete: (Boolean) -> Unit
    ) {
        Log.d("PetRepo", "addProfile() called → userId=$userId, profile.id=${profile.id}")
        profiles[profile.id] = profile
        uploadProfileToServer(userId, profile) { success ->
            Log.d("PetRepo", "addProfile() uploadProfileToServer onComplete → success=$success")
            onComplete(success)
        }
    }

    fun removeProfile(id: UUID) {
        Log.d("PetRepo", "removeProfile() called → id=$id")
        profiles.remove(id)
        if (currentPetId == id) {
            currentPetId = null
            Log.d("PetRepo", "removeProfile() cleared currentPetId")
        }
    }

    fun getAllProfiles(): List<PetProfile> {
        Log.d("PetRepo", "getAllProfiles() → profiles.size=${profiles.size}")
        return profiles.values.toList()
    }

    fun getProfile(id: UUID): PetProfile? {
        Log.d("PetRepo", "getProfile() → id=$id, found=${profiles.containsKey(id)}")
        return profiles[id]
    }

    fun setCurrentPet(id: UUID) {
        Log.d("PetRepo", "setCurrentPet() → id=$id")
        currentPetId = id
    }

    fun getCurrentPet(): PetProfile? {
        Log.d("PetRepo", "getCurrentPet() → currentPetId=$currentPetId")
        return profiles[currentPetId]
    }

    private fun uploadProfileToServer(
        userId: String,
        profile: PetProfile,
        onComplete: (Boolean) -> Unit
    ) {
        Log.d("PetRepo", "uploadProfileToServer() called → userId=$userId, profile.id=${profile.id}")
        val dataJson = JSONObject().apply {
            put("name", profile.name)
            put("age", profile.age)
            put("weight", profile.weight)
            put("gender", profile.gender)
            put("imageUri", profile.imageUri ?: "")
            put("totalDistance", profile.totalDistance)
            put("totalCalories", profile.totalCalories)
        }
        val uploadJson = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId", profile.id.toString())
            put("dataType", "pet_profile")
            put("data", dataJson.toString())
        }

        Log.d("PetRepo", "📤 [upload] Request JSON: $uploadJson")
        ApiClient.post("/uploadProfile", uploadJson,
            onSuccess = { response ->
                Log.d("PetRepo", "📥 [upload] Raw response: $response")
                val success = response.toBooleanStrictOrNull() == true
                Log.d("PetRepo", "📤 [upload] Parsed success=$success")
                onComplete(success)
            },
            onFailure = { error ->
                Log.e("PetRepo", "❌ [upload] onFailure: $error")
                onComplete(false)
            }
        )
    }

    /**
     * 서버에서 petIds 목록만큼 프로필을 내려받아 캐시에 저장합니다.
     *
     * @param userId     요청할 사용자 ID
     * @param petIds     가져올 펫 UUID 리스트
     * @param onComplete 모든 요청 완료 시 (성공/실패 상관없이) 호출
     * @param onError    개별 요청 실패 시 호출
     */
    fun loadProfilesFromServer(
        userId: String,
        petIds: List<UUID>,
        onComplete: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        Log.d("PetRepo", "loadProfilesFromServer() 시작 → userId=$userId, petIds=$petIds")
        profiles.clear()
        var loadedCount = 0

        if (petIds.isEmpty()) {
            Log.e("PetRepo", "loadProfilesFromServer() petIds가 비어있음 → 바로 onComplete 호출")
            onComplete()
            return
        }

        for (id in petIds) {
            val requestJson = JSONObject().apply {
                put("downloaderId", userId)
                put("dataId", id.toString())
                put("dataType", "pet_profile")
            }
            Log.d("PetRepo", "📤 [download] Request for id=$id → JSON: $requestJson")

            ApiClient.post("/downloadData", requestJson,
                onSuccess = { response ->
                    Log.d("PetRepo", "📥 [download] Raw response for id=$id: $response")
                    try {
                        val parsed = JSONObject(response)
                        val returnedMeta = parsed.optString("dataType")
                        if (returnedMeta != "pet_profile") {
                            Log.e("PetRepo", "⚠️ [download] metaData mismatch(id=$id) returned='$returnedMeta', skipping")
                        } else {
                            val dataJson = JSONObject(parsed.getString("data"))
                            val profile = PetProfile(
                                id            = id,
                                name          = dataJson.getString("name"),
                                age           = dataJson.getString("age"),
                                gender        = dataJson.getString("gender"),
                                weight        = dataJson.getDouble("weight"),
                                imageUri      = dataJson.optString("imageUri", "").ifBlank { null },
                                totalDistance = dataJson.optDouble("totalDistance", 0.0),
                                totalCalories = dataJson.optDouble("totalCalories", 0.0)
                            )
                            profiles[id] = profile
                            Log.d("PetRepo", "✅ [download] 프로필 로드 성공 for id=$id → $profile")
                        }
                    } catch (e: Exception) {
                        Log.e("PetRepo", "❌ [download] 프로필 파싱 실패 for id=$id: ${e.message}")
                        profiles.clear()
                        onError("파싱 실패 for id=$id: ${e.message}")
                    } finally {
                        loadedCount++
                        Log.d("PetRepo", "🔄 [download] loadedCount=$loadedCount/${petIds.size}, current keys=${profiles.keys}")
                        if (loadedCount == petIds.size) {
                            Log.d("PetRepo", "loadProfilesFromServer() 완료 → 총 로드 성공 개수=${profiles.size}")
                            onComplete()
                        }
                    }
                },
                onFailure = { error ->
                    Log.e("PetRepo", "❌ [download] onFailure for id=$id: $error")
                    profiles.clear()
                    onError("서버 요청 실패 for id=$id: $error")
                    loadedCount++
                    Log.d("PetRepo", "🔄 [download] loadedCount(after failure)=$loadedCount/${petIds.size}")
                    if (loadedCount == petIds.size) {
                        Log.d("PetRepo", "loadProfilesFromServer() 완료(에러포함) → profiles cleared")
                        onComplete()
                    }
                }
            )
        }
    }
}*/
/*
/**
 * 8월22일 수정전 코드
 */
package com.example.pet_walking.feature.profile.repository

import android.util.Log
import com.example.pet_walking.feature.profile.data.PetProfile
import com.example.pet_walking.network.ApiClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * --------------------------------------------------------------------
 *  PetRepository
 *  ────────────────────────────────────────────────────────────────────
 *  • 서버 ↔ 앱 사이 펫(Pet) 프로필 동기화 담당
 *    ▶ 로그인 시 서버 응답의 "pets" 배열을 캐시에 저장
 *    ▶ 새 펫 생성/수정 시  `/uploadProfile`(PetDTO) 로 전송
 *  • 실시간 다운로드(/downloadData) 로직은 제거
 *    → 서버가 로그인 응답에 모든 펫 정보를 보내주기 때문
 *  • 모든 퍼블릭 API 는 UI 레이어(뷰모델/프래그먼트) 가 사용
 * --------------------------------------------------------------------
 */
object PetRepository {

    /** 내부 캐시: petId(UUID) → PetProfile (앱 프로세스 생존 동안 유지) */
    internal val profiles: MutableMap<UUID, PetProfile> = mutableMapOf()

    /** 유저가 현재 선택한 펫(없으면 null) */
    var currentPetId: UUID? = null
        private set

    init { Log.d("PetRepo", "PetRepository initialized") }

    /* ─────────────────────────────────────────────────────────────── */
    /* 1) 서버 → 캐시 : 로그인 응답에서 pets 배열 주입                  */
    /* ─────────────────────────────────────────────────────────────── */

    /**
     * 로그인 성공 후 서버가 내려준 전체 JSON(예: `{"id": …, "pets":[…]}`) 을 넘기면
     * 내부 캐시를 초기화하고 `pets` 배열을 파싱·저장한다.
     *
     * @param loginJson  서버 로그인 응답 원본
     */
    /*fun injectPetsFromJson(loginJson: JSONObject) {
        profiles.clear()

        val arr: JSONArray = loginJson.optJSONArray("pets") ?: JSONArray()

        Log.d("PetRepo", "injectPetsFromJson → begin (size=${arr.length()})")

        for (i in 0 until arr.length()) {
            try {
                val petObj = arr.getJSONObject(i)
                val pet    = petObj.toPetProfile()
                profiles[pet.id] = pet
                Log.d("PetRepo", "  ✓ pets[$i] parsed → ${pet.id}")
            } catch (e: Exception) {
                Log.e("PetRepo", "  ✗ pets[$i] parse error: ${e.message}")
            }
        }
        Log.d("PetRepo", "injectPetsFromJson ← done (cache=${profiles.size})")
    }*/

    /* ─────────────────────────────────────────────────────────────── */
    /* 2) 캐시 → 서버 : 신규/수정 업로드                               */
    /* ─────────────────────────────────────────────────────────────── */

    /**
     * 새 PetProfile 을 로컬 캐시에 넣고 즉시 서버로 업로드.
     *
     * @param profile   생성된 PetProfile
     * @param userId    로그인한 유저 ID
     * @param onComplete true=업로드 성공 / false=실패
     */
    fun addProfile(
        profile   : PetProfile,
        userId    : String,
        onComplete: (Boolean) -> Unit
    ) {
        Log.d("PetRepo", "addProfile() → id=${profile.id}, userId=$userId")

        profiles[profile.id] = profile
        uploadProfileToServer(userId, profile, onComplete)
    }

    /**
     * 내부: PetDTO 사양으로 JSON 직렬화 후 `/uploadProfile` 호출
     */
    private fun uploadProfileToServer(
        userId    : String,
        profile   : PetProfile,
        onComplete: (Boolean) -> Unit
    ) {
        /* PetDTO 와 동일한 키로 채움 */
        val json = JSONObject().apply {
            put("petId"        , profile.id.toString())   // PK
            put("userId"       , userId)                  // FK
            put("name"         , profile.name)
            put("age"          , profile.age)
            put("gender"       , profile.gender)
            put("weight"       , profile.weight)
            put("imgUrl"       , profile.imageUri ?: "")
            put("totalDistance", profile.totalDistance)
            put("totalCalories", profile.totalCalories)
        }

        Log.d("PetRepo", "📤 [uploadProfile] $json")

        ApiClient.post(
            endpoint  = "/uploadProfile",
            json      = json,
            onSuccess = { resp ->
                val ok = resp.trim().toBooleanStrictOrNull() == true
                Log.d("PetRepo", "📥 uploadProfile 응답=[$resp] → success=$ok")
                onComplete(ok)
            },
            onFailure = { err ->
                Log.e("PetRepo", "❌ uploadProfile 실패: $err")
                onComplete(false)
            }
        )
    }

    /* ─────────────────────────────────────────────────────────────── */
    /* 3) 캐시 관련 편의 메서드                                        */
    /* ─────────────────────────────────────────────────────────────── */

    fun removeProfile(id: UUID) {
        Log.d("PetRepo", "removeProfile() → id=$id")
        profiles.remove(id)
        if (currentPetId == id) currentPetId = null
    }

    fun setCurrentPet(id: UUID?) { currentPetId = id }
    fun getCurrentPet(): PetProfile?       = profiles[currentPetId]
    fun getProfile(id: UUID): PetProfile?  = profiles[id]
    fun getAllProfiles(): List<PetProfile> = profiles.values.toList()
}

/* ────────────────────────────────────────────────────────────────── */
/* 확장 함수 : JSONObject → PetProfile                               */
/* ────────────────────────────────────────────────────────────────── */
private fun JSONObject.toPetProfile(): PetProfile =
    PetProfile(
        id            = UUID.fromString(getString("petId")),
        name          = getString("name"),
        age           = getString("age"),
        gender        = getString("gender"),
        weight        = getDouble("weight"),
        imageUri      = optString("imgUrl").ifBlank { null },
        totalDistance = optDouble("totalDistance", 0.0),
        totalCalories = optDouble("totalCalories", 0.0)
    )*/
package com.example.pet_walking.feature.profile.repository

import android.util.Log
import com.example.pet_walking.feature.profile.data.PetProfile
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject
import java.util.UUID

object PetRepository {

    /** 내부 캐시: petId(UUID) → PetProfile (앱 프로세스 생존 동안 유지) */
    internal val profiles: MutableMap<UUID, PetProfile> = mutableMapOf()

    /** 유저가 현재 선택한 펫(없으면 null) */
    var currentPetId: UUID? = null
        private set

    init { Log.d("PetRepo", "PetRepository initialized") }

    /* ─────────────────────────────────────────────────────────────── */
    /* 업로드(신규/수정)                                                */
    /* ─────────────────────────────────────────────────────────────── */

    /**
     * 새 PetProfile 을 로컬 캐시에 넣고 즉시 서버로 업로드.
     *
     * @param profile   생성된 PetProfile
     * @param userId    로그인한 유저 ID
     * @param onComplete true=업로드 성공 / false=실패
     */
    fun addProfile(
        profile   : PetProfile,
        userId    : String,
        onComplete: (Boolean) -> Unit
    ) {
        Log.d("PetRepo", "addProfile() → id=${profile.id}, userId=$userId")
        profiles[profile.id] = profile
        uploadProfileToServer(userId, profile, onComplete)
    }

    /** 내부: 서버 규격에 맞춰 JSON 직렬화 후 POST /pet/profile 호출 */
    private fun uploadProfileToServer(
        userId    : String,
        profile   : PetProfile,
        onComplete: (Boolean) -> Unit
    ) {
        val json = JSONObject().apply {
            put("petId"        , profile.id.toString())   // PK
            put("userId"       , userId)                  // FK
            put("name"         , profile.name)
            put("age"          , profile.age)
            put("gender"       , profile.gender)
            put("weight"       , profile.weight)
            put("imgUrl"       , profile.imageUri ?: "")
            put("totalDistance", profile.totalDistance)
            put("totalCalories", profile.totalCalories)
        }

        Log.d("PetRepo", "📤 [POST /pet/profile] $json")

        ApiClient.post(
            endpoint  = "/pet/profile",
            json      = json,
            onSuccess = { resp ->
                val ok = isOkResponse(resp)
                Log.d("PetRepo", "📥 /pet/profile 응답=[$resp] → success=$ok")
                onComplete(ok)
            },
            onFailure = { err ->
                Log.e("PetRepo", "❌ /pet/profile 실패: $err")
                onComplete(false)
            }
        )
    }

    /* ─────────────────────────────────────────────────────────────── */
    /* 삭제(서버 + 캐시)                                               */
    /* ─────────────────────────────────────────────────────────────── */

    /**
     * 서버에서 펫 프로필 삭제 후(DELETE /pet/profile), 캐시에서도 제거.
     *
     * @param userId  로그인 유저 ID (서버 검증용)
     * @param petId   삭제할 펫 UUID
     */
    fun deleteProfile(
        userId: String,
        petId: UUID,
        onComplete: (Boolean) -> Unit
    ) {
        val params = mapOf(
            "userId" to userId,
            "petId"  to petId.toString()
        )

        Log.d("PetRepo", "🗑️ [DELETE /pet/profile] params=$params")

        ApiClient.delete(
            endpoint  = "/pet/profile",
            params    = params,
            onSuccess = { resp ->
                val ok = isOkResponse(resp)
                if (ok) {
                    profiles.remove(petId)
                    if (currentPetId == petId) currentPetId = null
                    Log.d("PetRepo", "✅ 삭제 완료 (서버+캐시): $petId")
                } else {
                    Log.w("PetRepo", "⚠️ 서버 삭제 응답 false: $resp")
                }
                onComplete(ok)
            },
            onFailure = { err ->
                Log.e("PetRepo", "❌ /pet/profile 삭제 실패: $err")
                onComplete(false)
            }
        )
    }

    /* ─────────────────────────────────────────────────────────────── */
    /* 캐시 관련 편의 메서드                                           */
    /* ─────────────────────────────────────────────────────────────── */

    /** 로컬 캐시만 제거(서버 통신 없음) — 필요 시 UI에서 직접 호출 */
    fun removeProfileLocal(id: UUID) {
        Log.d("PetRepo", "removeProfileLocal() → id=$id")
        profiles.remove(id)
        if (currentPetId == id) currentPetId = null
    }

    /** 과거 호환용(로컬 제거) */
    fun removeProfile(id: UUID) = removeProfileLocal(id)

    fun setCurrentPet(id: UUID?) { currentPetId = id }
    fun getCurrentPet(): PetProfile?       = currentPetId?.let { profiles[it] }
    fun getProfile(id: UUID): PetProfile?  = profiles[id]
    fun getAllProfiles(): List<PetProfile> = profiles.values.toList()

    /* ─────────────────────────────────────────────────────────────── */
    /* 내부 유틸                                                      */
    /* ─────────────────────────────────────────────────────────────── */

    private fun isOkResponse(resp: String): Boolean = try {
        val s = resp.trim()
        when {
            s.startsWith("{") -> JSONObject(s).optBoolean("success", true)
            else -> s.trim('"').equals("true", true) || s.isEmpty()
        }
    } catch (_: Exception) { true }
}