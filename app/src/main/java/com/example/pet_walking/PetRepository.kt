package com.example.pet_walking

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object PetRepository {
    private val profiles = mutableMapOf<UUID, PetProfile>()
    var currentPetId: UUID? = null

    fun addProfile(profile: PetProfile) {
        profiles[profile.id] = profile
    }

    fun removeProfile(id: UUID) {
        profiles.remove(id)
        if (currentPetId == id) currentPetId = null
    }

    fun getAllProfiles(): List<PetProfile> = profiles.values.toList()

    fun getProfile(id: UUID): PetProfile? = profiles[id]

    fun updateDistanceAndCalories(id: UUID, distance: Double, calories: Double) {
        profiles[id]?.let {
            it.totalDistance += distance
            it.totalCalories += calories
        }
    }

    fun setCurrentPet(id: UUID) {
        currentPetId = id
    }

    fun getCurrentPet(): PetProfile? = currentPetId?.let { profiles[it] }

    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences("PetData", Context.MODE_PRIVATE)
        val json = Gson().toJson(profiles)
        prefs.edit().putString("profiles", json).apply()
        prefs.edit().putString("currentPetId", currentPetId?.toString()).apply()
    }

    fun loadFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences("PetData", Context.MODE_PRIVATE)
        val json = prefs.getString("profiles", null)
        val savedId = prefs.getString("currentPetId", null)

        json?.let {
            val type = object : TypeToken<Map<UUID, PetProfile>>() {}.type
            val restored = Gson().fromJson<Map<UUID, PetProfile>>(it, type)
            profiles.clear()
            profiles.putAll(restored)
        }

        savedId?.let {
            currentPetId = UUID.fromString(it)
        }
    }
}