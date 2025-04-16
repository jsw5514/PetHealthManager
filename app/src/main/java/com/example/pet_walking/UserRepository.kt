package com.example.pet_walking

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object UserRepository {
    private val users = mutableMapOf<String, UserProfile>() // key = userId
    private var loggedInUserId: String? = null

    fun registerUser(profile: UserProfile): Boolean {
        if (users.containsKey(profile.userId)) return false
        users[profile.userId] = profile
        return true
    }

    fun login(userId: String, password: String): Boolean {
        val user = users[userId]
        if (user != null && user.password == password) {
            loggedInUserId = userId
            return true
        }
        return false
    }

    fun logout() {
        loggedInUserId = null
    }

    fun getCurrentUser(): UserProfile? = loggedInUserId?.let { users[it] }

    fun addPetToCurrentUser(petId: UUID) {
        getCurrentUser()?.petIds?.add(petId)
    }

    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val json = Gson().toJson(users)
        prefs.edit().putString("users", json).apply()
        prefs.edit().putString("loggedInUserId", loggedInUserId).apply()
    }

    fun loadFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val json = prefs.getString("users", null)
        val savedId = prefs.getString("loggedInUserId", null)

        json?.let {
            val type = object : TypeToken<Map<String, UserProfile>>() {}.type
            val restored = Gson().fromJson<Map<String, UserProfile>>(it, type)
            users.clear()
            users.putAll(restored)
        }

        loggedInUserId = savedId
    }
}