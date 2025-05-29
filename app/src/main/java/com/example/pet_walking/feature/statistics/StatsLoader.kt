// com/example/pet_walking/statistics/StatsLoader.kt
package com.example.pet_walking.feature.statistics

import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.Running.model.RunStats
import org.json.JSONObject

object StatsLoader {
    /**
     * 서버에 저장된 runLog(metaData="runLog") 전체를 내려받아
     * List<RunStats> 로 파싱해서 onResult 로 전달
     */
    fun loadRunLogs(
        userId: String,
        petId: String,
        onResult: (List<RunStats>) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val json = JSONObject().apply {
            put("downloaderId", userId)
            put("dataId",       petId)
            put("metaData",     "runLog")
        }
        ApiClient.postForResult("/downloadDataList", json, { respJson ->
            try {
                val arr = respJson.getJSONArray("logs")
                val list = mutableListOf<RunStats>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val data = JSONObject(obj.getString("data"))
                    list += RunStats(
                        distance  = data.optDouble("distance", 0.0),
                        calories  = data.optDouble("calories", 0.0),
                        timestamp = data.optLong("timestamp", System.currentTimeMillis())
                    )
                }
                onResult(list)
            } catch (e: Exception) {
                Log.e("StatsLoader", "로그 파싱 오류", e)
                onError(e)
            }
        }, { err ->
            Log.e("StatsLoader", "로그 리스트 로딩 실패: $err")
            onError(RuntimeException(err))
        })
    }
}