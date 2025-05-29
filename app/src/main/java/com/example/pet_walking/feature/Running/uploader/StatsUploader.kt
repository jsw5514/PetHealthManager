// com/example/pet_walking/feature/running/uploader/StatsUploader.kt
package com.example.pet_walking.feature.running.uploader

import android.util.Log
import com.example.pet_walking.feature.Running.model.RunStats
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

/**
 * 러닝 세션 하나마다 호출해서
 *  • summary(전체 누적 덮어쓰기)
 *  • daily, weekly, monthly, yearly(기간별 누적)
 * 을 한 번에 서버로 업로드합니다.
 */
object StatsUploader {

    /**
     * @param userId    유저 ID
     * @param petId     펫 ID
     * @param distance  누적 이동 거리 (km)
     * @param calories  누적 소모 칼로리 (kcal)
     * @param timestamp 기록 시각 (밀리초)
     */
    fun upload(
        userId: String,
        petId: UUID,
        distance: Double,
        calories: Double,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val dayKey   = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH))
        val weekKey  = "%04d-W%02d".format(cal.get(Calendar.YEAR),
            cal.get(Calendar.WEEK_OF_YEAR))
        val monthKey = "%04d-%02d".format(cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1)
        val yearKey  = cal.get(Calendar.YEAR).toString()

        // 서버에 보낼 JSON 구조
        val dataJson = JSONObject().apply {
            // 전체 누적 통계 덮어쓰기
            put("summary", JSONObject().apply {
                put("totalDistance", distance)
                put("totalCalories", calories)
            })
            // 일별
            put("daily", JSONObject().apply {
                put(dayKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
            // 주별
            put("weekly", JSONObject().apply {
                put(weekKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
            // 월별
            put("monthly", JSONObject().apply {
                put(monthKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
            // 년별
            put("yearly", JSONObject().apply {
                put(yearKey, JSONObject().apply {
                    put("distance", distance)
                    put("calories", calories)
                })
            })
        }

        val payload = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId",     petId.toString())
            put("metaData",   "running_stats")
            put("data",       dataJson.toString())
        }

        // ApiClient 로 POST
        ApiClient.post("/uploadData", payload,
            onSuccess = { resp ->
                Log.d("StatsUploader", "통계 업로드 성공: $resp")
            },
            onFailure = { err ->
                Log.e("StatsUploader", "통계 업로드 실패: $err")
            }
        )
    }
    fun logRun(userId: String, petId: UUID, stats: RunStats) {
        val payload = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId",     petId.toString())
            put("metaData",   "run_log")  // summary와 구분
            put("data", JSONObject().apply {
                put("distance",  stats.distance)
                put("calories",  stats.calories)
                put("timestamp", stats.timestamp)
            }.toString())
        }

        ApiClient.post("/uploadData", payload,
            onSuccess = { Log.d("StatsUploader", "로그 저장 성공") },
            onFailure = { Log.e("StatsUploader", "로그 저장 실패: $it") }
        )
    }
}