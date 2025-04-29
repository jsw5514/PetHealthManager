package com.example.pet_walking

import android.util.Log
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject
import java.util.*

object PetRepository {
    private val profiles = mutableMapOf<UUID, PetProfile>()
    var currentPetId: UUID? = null

    fun addProfile(profile: PetProfile, userId: String, onComplete: (Boolean) -> Unit) {
        profiles[profile.id] = profile
        uploadProfileToServer(userId, profile, onComplete)
    }

    fun removeProfile(id: UUID) {
        profiles.remove(id)
        if (currentPetId == id) currentPetId = null
    }

    fun getAllProfiles(): List<PetProfile> = profiles.values.toList()

    fun getProfile(id: UUID): PetProfile? = profiles[id]

    fun setCurrentPet(id: UUID) {
        currentPetId = id
    }

    fun getCurrentPet(): PetProfile? = profiles[currentPetId]

    private fun uploadProfileToServer(userId: String, profile: PetProfile, onComplete: (Boolean) -> Unit) {
        // 내부 데이터 JSON 생성
        val dataJson = JSONObject().apply {
            put("name", profile.name)
            put("age", profile.age)
            put("weight", profile.weight)
            put("gender", profile.gender)
            put("imageUri", profile.imageUri ?: "")
            put("totalDistance", profile.totalDistance)
            put("totalCalories", profile.totalCalories)
        }

        // 최종 업로드 JSON 생성 (data를 문자열로 넣음)
        val uploadJson = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId", profile.id.toString())
            put("metaData", "pet_profile")
            put("data", dataJson.toString())  // 반드시 문자열로 변환
        }

        Log.d("PetRepo", "📤 업로드 요청 JSON: $uploadJson")

        ApiClient.post("/uploadData", uploadJson,
            onSuccess = { response ->
                Log.d("PetRepo", "✅ 펫 프로필 업로드 성공: $response")
                onComplete(true)
            },
            onFailure = { error ->
                Log.e("PetRepo", "❌ 펫 프로필 업로드 실패: $error")
                onComplete(false)
            }
        )
    }

    fun loadProfilesFromServer(userId: String, petIds: List<UUID>, onComplete: () -> Unit) {
        profiles.clear()
        var loadedCount = 0

        for (id in petIds) {
            val requestJson = JSONObject().apply {
                put("downloaderId", userId)
                put("dataId", id.toString())
            }

            Log.d("PetRepo", "📥 다운로드 요청 JSON: $requestJson")

            ApiClient.post("/downloadData", requestJson,
                onSuccess = { response ->
                    try {
                        val parsed = JSONObject(response)
                        val dataString = parsed.getString("data")
                        val dataJson = JSONObject(dataString)

                        val profile = PetProfile(
                            id = id,
                            name = dataJson.getString("name"),
                            age = dataJson.getString("age"),
                            gender = dataJson.getString("gender"),
                            weight = dataJson.getDouble("weight"),
                            imageUri = dataJson.optString("imageUri", "").takeIf { it.isNotEmpty() },
                            totalDistance = dataJson.optDouble("totalDistance", 0.0),
                            totalCalories = dataJson.optDouble("totalCalories", 0.0)
                        )

                        profiles[id] = profile
                        Log.d("PetRepo", "✅ 프로필 로드 성공: ${profile.name}")
                    } catch (e: Exception) {
                        Log.e("PetRepo", "❌ 프로필 파싱 실패: ${e.message}")
                    } finally {
                        loadedCount++
                        if (loadedCount == petIds.size) {
                            onComplete()
                        }
                    }
                },
                onFailure = { error ->
                    Log.e("PetRepo", "❌ 서버 요청 실패: $error")
                    loadedCount++
                    if (loadedCount == petIds.size) {
                        onComplete()
                    }
                }
            )
        }
    }
}