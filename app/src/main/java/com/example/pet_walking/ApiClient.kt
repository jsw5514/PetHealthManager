package com.example.pet_walking.network

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {
    private val client = OkHttpClient()
    private const val BASE_URL = "http://172.24.251.189:8080" // 로컬 서버 주소 (에뮬레이터 기준)

    // 🔹 일반 POST 요청 (문자열 응답 처리)
    fun post(
        endpoint: String,
        json: JSONObject,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val fullUrl = BASE_URL + endpoint
        Log.d("ApiClient", "📡 요청 시작: $fullUrl, payload: $json")

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()
        Log.d("ApiClient", "📡 요청 시작: ${BASE_URL + endpoint}, payload: $json")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "❌ 서버 연결 실패: ${e.message}")
                onFailure("서버 연결 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Log.d("ApiClient", "✅ 응답 성공: $body")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "❌ 응답 실패: $body")
                    onFailure("응답 실패: $body")
                }
            }
        })
    }

    // 🔹 GET 요청
    fun get(
        fullUrl: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("ApiClient", "🌐 GET 요청 시작: $fullUrl")
        val request = Request.Builder()
            .url(fullUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "❌ GET 실패: ${e.message}")
                onFailure("GET 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Log.d("ApiClient", "✅ GET 응답 성공: $body")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "❌ GET 응답 실패: $body")
                    onFailure("GET 응답 실패: $body")
                }
            }
        })
    }

    // 🔹 POST 요청 후 JSON 객체 응답 기대 시 사용
    fun postForResult(
        endpoint: String,
        json: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val fullUrl = BASE_URL + endpoint
        Log.d("ApiClient", "📡 POST for Result 시작: $fullUrl, payload: $json")

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "❌ POST 실패: ${e.message}")
                onFailure("서버 연결 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val jsonObject = JSONObject(body)
                        Log.d("ApiClient", "✅ JSON 파싱 성공: $jsonObject")
                        onSuccess(jsonObject)
                    } catch (e: Exception) {
                        Log.e("ApiClient", "❌ JSON 파싱 오류: ${e.message}")
                        onFailure("응답 파싱 실패: ${e.message}")
                    }
                } else {
                    Log.e("ApiClient", "❌ POST 응답 실패: $body")
                    onFailure("POST 응답 실패: $body")
                }
            }
        })
    }
}