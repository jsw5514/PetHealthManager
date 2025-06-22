// com/example/pet_walking/statistics/StatsLoader.kt
package com.example.pet_walking.feature.statistics

import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.Running.model.RunStats
import org.json.JSONObject

object StatsLoader {
    /**
     * 서버에 저장된 runLog(dataType="runLog") 전체를 내려받아
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
            put("dataType",     "runLog")  // 수정된 부분
        }

        Log.d("StatsLoader", "📤 runLog 요청 → userId=$userId, petId=$petId")

        ApiClient.postForResult("/downloadDataList", json, { respJson ->
            try {
                Log.d("StatsLoader", "📥 응답 수신 성공 → $respJson")

                val arr = respJson.getJSONArray("logs")
                val list = mutableListOf<RunStats>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val data = JSONObject(obj.getString("data"))
                    val stats = RunStats(
                        distance  = data.optDouble("distance", 0.0),
                        calories  = data.optDouble("calories", 0.0),
                        timestamp = data.optLong("timestamp", System.currentTimeMillis())
                    )
                    Log.d("StatsLoader", "✅ 파싱된 로그[$i]: $stats")
                    list += stats
                }

                Log.d("StatsLoader", "📊 총 ${list.size}개의 runLog 로드 완료")
                onResult(list)

            } catch (e: Exception) {
                Log.e("StatsLoader", "❌ 로그 파싱 오류", e)
                onError(e)
            }
        }, { err ->
            Log.e("StatsLoader", "❌ 로그 리스트 로딩 실패: $err")
            onError(RuntimeException(err))
        })
    }
}