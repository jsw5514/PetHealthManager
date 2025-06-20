package com.example.pet_walking.feature.profile.repository

import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.profile.data.PetProfile
import org.json.JSONObject
import java.util.*

object PetRepository {
    private val profiles = mutableMapOf<UUID, PetProfile>()
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
            put("metaData", "pet_profile")
            put("data", dataJson.toString())
        }

        Log.d("PetRepo", "📤 [upload] Request JSON: $uploadJson")
        ApiClient.post("/uploadData", uploadJson,
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
            Log.d("PetRepo", "loadProfilesFromServer() petIds가 비어있음 → 바로 onComplete 호출")
            onComplete()
            return
        }

        for (id in petIds) {
            val requestJson = JSONObject().apply {
                put("downloaderId", userId)
                put("dataId", id.toString())
                put("metaData", "pet_profile")
            }
            Log.d("PetRepo", "📤 [download] Request for id=$id → JSON: $requestJson")

            ApiClient.post("/downloadData", requestJson,
                onSuccess = { response ->
                    Log.d("PetRepo", "📥 [download] Raw response for id=$id: $response")
                    try {
                        val parsed = JSONObject(response)
                        val returnedMeta = parsed.optString("metaData")
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
}