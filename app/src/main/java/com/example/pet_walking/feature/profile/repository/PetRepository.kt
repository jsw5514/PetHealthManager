package com.example.pet_walking.feature.profile.repository


import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.profile.data.PetProfile
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

    private fun uploadProfileToServer(
        userId: String,
        profile: PetProfile,
        onComplete: (Boolean) -> Unit
    ) {
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
                val success = response.toBooleanStrictOrNull() == true
                Log.d("PetRepo", if (success) "펫 프로필 업로드 성공" else "펫 프로필 업로드 실패 (서버 응답: $response)")
                onComplete(success)
            },
            onFailure = { error ->
                Log.e("PetRepo", " 펫 프로필 업로드 실패: $error")
                onComplete(false)
            }
        )
    }

    fun loadProfilesFromServer(
        userId: String,
        petIds: List<UUID>,
        onComplete: () -> Unit
    ) {
        // 1) 호출 직전에 로컬 캐시 초기화
        profiles.clear()
        var loadedCount = 0

        for (id in petIds) {
            val requestJson = JSONObject().apply {
                put("downloaderId", userId)
                put("dataId", id.toString())
            }
            Log.d("PetRepo", "다운로드 요청 JSON: $requestJson")

            ApiClient.post("/downloadData", requestJson,
                onSuccess = { response ->
                    try {
                        // 2) 응답이 빈 문자열이면 JSONObject(response)에서 예외 발생
                        val parsed = JSONObject(response)
                        val dataString = parsed.getString("data")
                        val dataJson = JSONObject(dataString)

                        // 3) 정상 파싱된 경우에만 맵에 추가
                        val profile = PetProfile(
                            id = id,
                            name = dataJson.getString("name"),
                            age = dataJson.getString("age"),
                            gender = dataJson.getString("gender"),
                            weight = dataJson.getDouble("weight"),
                            imageUri = dataJson.optString("imageUri", "").ifBlank { null },
                            totalDistance = dataJson.optDouble("totalDistance", 0.0),
                            totalCalories = dataJson.optDouble("totalCalories", 0.0)
                        )
                        profiles[id] = profile
                        Log.d("PetRepo", "프로필 로드 성공: ${profile.name}")

                    } catch (e: Exception) {
                        // 4) 파싱 예외 발생 시 로컬 캐시 전부 삭제
                        Log.e("PetRepo", "프로필 파싱 실패: ${e.message} → 로컬 데이터 삭제")
                        profiles.clear()
                    } finally {
                        // 5) 완료 카운트 체크
                        loadedCount++
                        if (loadedCount == petIds.size) {
                            onComplete()
                        }
                    }
                },
                onFailure = { error ->
                    // 6) 네트워크 에러 시에도 로컬 캐시 전부 삭제
                    Log.e("PetRepo", "서버 요청 실패: $error → 로컬 데이터 삭제")
                    profiles.clear()

                    loadedCount++
                    if (loadedCount == petIds.size) {
                        onComplete()
                    }
                }
            )
        }
    }
}