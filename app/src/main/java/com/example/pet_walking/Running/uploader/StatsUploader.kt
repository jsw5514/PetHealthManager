package com.example.pet_walking_repectory.Running.uploader

import android.util.Log
import com.example.pet_walking.Chat.ChatNetworkHelper
import org.json.JSONObject
import java.util.*

object StatsUploader {

    fun upload(userId: String, petId: UUID, distance: Double, calories: Double, timestamp: Long = System.currentTimeMillis()) {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        val dayKey   = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        val weekKey  = String.format("%04d-W%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.WEEK_OF_YEAR))
        val monthKey = String.format("%04d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        val yearKey  = calendar.get(Calendar.YEAR).toString()

        val data = JSONObject().apply {
            put("summary", JSONObject().apply {
                put("totalDistance", distance)
                put("totalCalories", calories)
            })
            put("daily", JSONObject().apply {
                put(dayKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
            put("weekly", JSONObject().apply {
                put(weekKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
            put("monthly", JSONObject().apply {
                put(monthKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
            put("yearly", JSONObject().apply {
                put(yearKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
        }

        val payload = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId", petId.toString())
            put("metaData", "running_stats")
            put("data", data.toString())
        }

        ChatNetworkHelper.postJson("/uploadData", payload) { success ->
            if (success) {
                Log.d("StatsUploader", "러닝 통계 업로드 완료")
            } else {
                Log.w("StatsUploader", "러닝 통계 업로드 실패")
            }
        }
    }
}