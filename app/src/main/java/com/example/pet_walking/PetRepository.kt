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
        val json = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId", profile.id.toString())
            put("metaData", "pet_profile")
            put("data", JSONObject().apply {
                put("name", profile.name)
                put("age", profile.age)
                put("weight", profile.weight)
                put("gender", profile.gender)
                put("imageUri", profile.imageUri?.toString() ?: "")
                put("totalDistance", profile.totalDistance)
                put("totalCalories", profile.totalCalories)
            }.toString())
        }

        ApiClient.post("/uploadData", json,
            onSuccess = {
                Log.d("PetRepo", "펫 프로필 업로드 성공")
                onComplete(true)
            },
            onFailure = {
                Log.w("PetRepo", "펫 프로필 업로드 실패: $it")
                onComplete(false)
            }
        )
    }

    fun loadProfilesFromServer(userId: String, petIds: List<UUID>, onComplete: () -> Unit) {
        profiles.clear()
        var loaded = 0

        for (id in petIds) {
            val json = JSONObject().apply {
                put("downloaderId", userId)
                put("dataId", id.toString())
            }

            ApiClient.post("/downloadData", json,
                onSuccess = { response ->
                    try {
                        val parsed = JSONObject(response)
                        val data = JSONObject(parsed.getString("data"))

                        val profile = PetProfile(
                            id = id,
                            name = data.getString("name"),
                            age = data.getInt("age"),
                            gender = data.getString("gender"),
                            weight = data.getDouble("weight"),
                            imageUri = data.optString("imageUri", "").takeIf { it.isNotEmpty() }?.let { Uri.parse(it) },
                            totalDistance = data.optDouble("totalDistance", 0.0),
                            totalCalories = data.optDouble("totalCalories", 0.0)
                        )

                        profiles[id] = profile
                    } catch (e: Exception) {
                        Log.e("PetRepo", "프로필 파싱 실패: ${e.message}")
                    } finally {
                        loaded++
                        if (loaded == petIds.size) onComplete()
                    }
                },
                onFailure = {
                    Log.w("PetRepo", "프로필 불러오기 실패: $it")
                    loaded++
                    if (loaded == petIds.size) onComplete()
                }
            )
        }
    }
}