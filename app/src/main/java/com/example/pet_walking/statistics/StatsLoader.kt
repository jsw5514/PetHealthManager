package com.example.pet_walking.statistics

import android.util.Log
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject

object StatsLoader {
    fun loadStats(userId: String, petId: String, onResult: (distance: Double, calories: Double) -> Unit) {
        val json = JSONObject().apply {
            put("downloaderId", userId)
            put("dataId", petId)
        }

        ApiClient.post("/downloadData", json,
            onSuccess = { response ->
                try {
                    val parsed = JSONObject(response)
                    val data = JSONObject(parsed.getString("data"))
                    val distance = data.optDouble("distance", 0.0)
                    val calories = data.optDouble("calories", 0.0)
                    onResult(distance, calories)
                } catch (e: Exception) {
                    Log.e("StatsLoader", "파싱 오류: ${e.message}")
                }
            },
            onFailure = {
                Log.e("StatsLoader", "서버 통계 로딩 실패: $it")
            }
        )
    }
}