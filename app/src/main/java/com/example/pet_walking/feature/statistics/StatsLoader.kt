/*
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
*/
/*
package com.example.pet_walking.feature.statistics

import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.Running.model.RunStats
import org.json.JSONArray
import org.json.JSONObject

object StatsLoader {
    /**
     * 서버에 저장된 runLog 묶음(배열)을 단일 /downloadData 로 받아온다.
     *  - dataType = "runLogList"
     *  - data(JSON 문자열) → JSONArray → List<RunStats> 로 파싱
     *
     * 서버 측 저장 예시:
     *  {
     *    "uploaderId": "user123",
     *    "dataId": "pet-uuid",
     *    "dataType": "runLogList",
     *    "data": "[{\"distance\":3.5,\"calories\":120,\"timestamp\":...}, ...]"
     *  }
     */
    fun loadRunLogs(
        userId: String,
        petId: String,
        onResult: (List<RunStats>) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val req = JSONObject().apply {
            put("downloaderId", userId)
            put("dataId",       petId)          // pet UUID
            put("dataType",     "runLogList")   // ✅ 단일 호출용 타입
        }

        Log.d("StatsLoader", "📤 runLogList 요청 → userId=$userId, petId=$petId")

        ApiClient.postForResult("/downloadData", req, { resp ->
            try {
                Log.d("StatsLoader", "📥 응답 수신 성공 → $resp")

                // runLogList 데이터는 배열(JSON 문자열)로 저장돼 있음
                val logsJson = JSONArray(resp.getString("data"))
                val list = mutableListOf<RunStats>()

                for (i in 0 until logsJson.length()) {
                    val o = logsJson.getJSONObject(i)
                    val stats = RunStats(
                        distance  = o.optDouble("distance", 0.0),
                        calories  = o.optDouble("calories", 0.0),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis())
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
            Log.e("StatsLoader", "❌ runLogList 로딩 실패: $err")
            onError(RuntimeException(err))
        })
    }
}*/
/*---------------------------------------------------------------
 * StatsLoader.kt
 * --------------------------------------------------------------
 * • 러닝 한-번(=세션) 저장 시 서버 쪽에서는
 *      dataType = "runLogList"
 *      data     = "[{...},{...}, ...]"   ← JSON-Array 문자열
 *   형태로 저장한다.
 *
 * • 이 파일은 해당 레코드를 내려받아
 *      List<RunStats>
 *   로 변환해 UI 쪽에 넘기는 역할을 한다.
 *
 * • 서버가 “아직 아무 로그도 없다”면 **본문을 비워**(length==0)
 *   응답할 수 있으므로 ⇒ 그 경우 **빈 리스트**를 바로
 *   onResult() 에 전달해 크래시를 방지한다.                ★핵심 수정
 * --------------------------------------------------------------*/
/*
package com.example.pet_walking.feature.statistics

import android.util.Log
import com.example.pet_walking.feature.Running.model.RunStats
import com.example.pet_walking.network.ApiClient
import org.json.JSONArray
import org.json.JSONObject

object StatsLoader {

    /**
     * @param userId   로그인 ID
     * @param petId    해당 펫 UUID (문자열)
     * @param onResult 서버 파싱 성공 → List<RunStats> 반환
     * @param onError  네트워크/파싱 문제 → Throwable 전달
     */
    fun loadRunLogs(
        userId   : String,
        petId    : String,
        onResult : (List<RunStats>) -> Unit,
        onError  : (Throwable) -> Unit = {}
    ) {
        /* 1️⃣  요청 바디 생성  */
        val req = JSONObject()
            .put("downloaderId", userId)
            .put("dataId"      , petId)        // PK
            .put("dataType"    , "running_stats") // ✅ 단일 호출용

        Log.d("StatsLoader", "📤 /downloadData → $req")

        /* 2️⃣  POST (문자열 그대로 받는다) */
        ApiClient.post(
            endpoint  = "/downloadData",
            json      = req,
            onSuccess = { raw ->                      // raw == "" 가능!
                try {
                    /* (a) 본문이 비어 있으면 → 로그 0개 */
                    if (raw.isBlank()) {
                        Log.w("StatsLoader", "응답이 빈 본문 → runLog 0개")
                        onResult(emptyList())
                        return@post
                    }

                    /* (b) JSON 파싱 시작 */
                    val obj      = JSONObject(raw)
                    val dataStr  = obj.optString("data", "[]")
                    val logsJson = JSONArray(dataStr)

                    val list = mutableListOf<RunStats>()
                    for (i in 0 until logsJson.length()) {
                        val o = logsJson.getJSONObject(i)
                        list += RunStats(
                            distance  = o.optDouble("distance",  0.0),
                            calories  = o.optDouble("calories",  0.0),
                            timestamp = o.optLong   ("timestamp", System.currentTimeMillis())
                        )
                    }

                    Log.d("StatsLoader",
                        "📥 파싱 완료 → ${list.size}개 runLog")
                    onResult(list)

                } catch (e: Exception) {
                    Log.e("StatsLoader", "❌ 파싱 실패", e)
                    onError(e)
                }
            },
            onFailure = { err ->
                Log.e("StatsLoader", "❌ 네트워크 실패: $err")
                onError(RuntimeException(err))
            }
        )
    }
}*/
/*
/**
 * 수정전 8월22일
 */
//  com/example/pet_walking/feature/statistics/StatsLoader.kt
package com.example.pet_walking.feature.statistics

import android.util.Log
import com.example.pet_walking.feature.Running.model.RunStats
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * -------------------------------------------------------------------
 *  StatsLoader
 *  ------------------------------------------------------------------
 *  • 서버에서 dataType = "running_stats" 레코드를 1건 내려받는다.
 *    └ data(문자열) = { summary:{…}, daily:{…}, weekly:{…} … }
 *
 *  • daily 맵을 day-key ▶︎ RunStats 로 풀어서 List<RunStats> 로 반환
 *    └ StatsFragment 는 이 리스트를 가지고
 *      - 총합(거리/칼로리) 계산
 *      - 기간별 그룹핑(키 파싱) 후 차트 표시
 *
 *  • 서버에 기록이 하나도 없으면 본문이 빈 문자열("") → 빈 리스트 반환
 * -------------------------------------------------------------------
 */
object StatsLoader {

    // yyyy-MM-dd → long(UTC midnight) 변환용
    private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * @param userId   로그인 ID
     * @param petId    펫 UUID (문자열)
     * @param onResult 성공 시 List<RunStats>
     * @param onError  실패(네트워크/파싱) 시 Throwable 전달
     */
    fun loadRunLogs(
        userId: String,
        petId: String,
        onResult: (List<RunStats>) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {

        /* 1) 요청 바디 생성 */
        val req = JSONObject()
            .put("downloaderId", userId)
            .put("dataId"      , petId)
            .put("dataType"    , "running_stats")

        Log.d("StatsLoader", "📤 /downloadData → $req")

        /* 2) POST */
        ApiClient.post(
            endpoint = "/downloadData",
            json     = req,
            onSuccess = { raw ->
                try {
                    /* (a) 기록이 없을 때: 빈 본문 */
                    if (raw.isBlank()) {
                        Log.i("StatsLoader", "서버에 저장된 통계 없음 → 빈 리스트 반환")
                        onResult(emptyList())
                        return@post
                    }

                    /* (b) 파싱 시작 */
                    val record   = JSONObject(raw)               // 전체 레코드
                    val data     = JSONObject(record.getString("data"))
                    val dailyObj = data.optJSONObject("daily") ?: JSONObject()

                    val list = mutableListOf<RunStats>()

                    // daily 키(yyyy-MM-dd) → RunStats
                    dailyObj.keys().forEach { key ->
                        val entry = dailyObj.getJSONObject(key)
                        list += RunStats(
                            distance  = entry.optDouble("distance", 0.0),
                            calories  = entry.optDouble("calories", 0.0),
                            timestamp = parseDateUtc(key)
                        )
                    }

                    Log.d("StatsLoader", "📥 파싱 완료 → ${list.size}개 RunStats")
                    onResult(list)

                } catch (e: Exception) {
                    Log.e("StatsLoader", "❌ running_stats 파싱 실패", e)
                    onError(e)
                }
            },
            onFailure = { err ->
                Log.e("StatsLoader", "❌ 네트워크 실패: $err")
                onError(RuntimeException(err))
            }
        )
    }

    /* yyyy-MM-dd → UTC timestamp(00:00:00) */
    private fun parseDateUtc(key: String): Long =
        kotlin.runCatching { DATE_FMT.parse(key)?.time ?: System.currentTimeMillis() }
            .getOrDefault(System.currentTimeMillis())
}*/
package com.example.pet_walking.feature.statistics

import android.util.Log
import com.example.pet_walking.feature.Running.model.RunStats
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * -------------------------------------------------------------------
 *  StatsLoader
 *  ------------------------------------------------------------------
 *  • GET /data?downloaderId=...&dataId=...&dataType=running_stats
 *  • 응답의 data(JSON 문자열 또는 객체)에서
 *      daily:{ "yyyy-MM-dd": {distance, calories}, ... }
 *    를 꺼내 List<RunStats> 로 변환한다.
 *  • 서버에 기록이 없거나 본문이 비면 → 빈 리스트 반환.
 * -------------------------------------------------------------------
 */
object StatsLoader {

    // yyyy-MM-dd → long(UTC midnight) 변환용
    private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun loadRunLogs(
        userId: String,
        petId: String,
        onResult: (List<RunStats>) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        // ✅ 새 규격: GET /data
        val endpoint =
            "/data?downloaderId=${userId}&dataId=${petId}&dataType=running_stats"

        Log.d("StatsLoader", "📤 GET $endpoint")

        ApiClient.getByEndpoint(
            endpoint = endpoint,
            onSuccess = { raw ->
                try {
                    val body = raw.trim()
                    if (body.isEmpty()) {
                        Log.i("StatsLoader", "서버에 저장된 통계 없음 → 빈 리스트 반환")
                        onResult(emptyList())
                        return@getByEndpoint
                    }

                    // 1) 최상위 JSONObject 시도
                    val dataJson: JSONObject? = runCatching {
                        val root = JSONObject(body)
                        when {
                            // 케이스 A: { ..., "data": "{...}" }
                            root.has("data") && root.get("data") is String ->
                                JSONObject(root.getString("data"))

                            // 케이스 B: { ..., "data": { ... } }
                            root.has("data") && root.get("data") is JSONObject ->
                                root.getJSONObject("data")

                            // 케이스 C: 최상위가 곧바로 daily/summary 를 가짐
                            root.has("daily") || root.has("summary") -> root

                            else -> null
                        }
                    }.getOrNull()
                        ?: runCatching { JSONObject(body) }.getOrNull() // 케이스 D: 본문이 곧바로 JSON 문자열

                    if (dataJson == null) {
                        Log.w("StatsLoader", "예상치 못한 응답 형식 → 빈 리스트 반환")
                        onResult(emptyList())
                        return@getByEndpoint
                    }

                    val dailyObj = dataJson.optJSONObject("daily") ?: JSONObject()

                    // 날짜 키 정렬(yyyy-MM-dd 이므로 문자열 정렬 == 시간 오름차순)
                    val keys = dailyObj.keys().asSequence().toList().sorted()

                    val list = ArrayList<RunStats>(keys.size)
                    for (k in keys) {
                        val day = dailyObj.optJSONObject(k) ?: continue
                        list += RunStats(
                            distance  = day.optDouble("distance", 0.0),
                            calories  = day.optDouble("calories", 0.0),
                            timestamp = parseDateUtc(k)
                        )
                    }

                    Log.d("StatsLoader", "📥 파싱 완료 → ${list.size}개 RunStats")
                    onResult(list)

                } catch (e: Exception) {
                    Log.e("StatsLoader", "❌ running_stats 파싱 실패", e)
                    onError(e)
                }
            },
            onFailure = { err ->
                Log.e("StatsLoader", "❌ GET /data 실패: $err")
                onError(RuntimeException(err))
            }
        )
    }

    /** yyyy-MM-dd → UTC timestamp(해당 날짜 00:00:00) */
    private fun parseDateUtc(key: String): Long =
        runCatching { DATE_FMT.parse(key)?.time ?: System.currentTimeMillis() }
            .getOrDefault(System.currentTimeMillis())
}