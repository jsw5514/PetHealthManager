package com.example.pet_walking.network

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {
    private val client = OkHttpClient()
    private const val BASE_URL = "http://10.0.2.2:8080" // 에뮬레이터 기준 로컬 서버 주소

    // 🔹 일반 POST (성공 여부만 확인)
    fun post(
        endpoint: String,
        json: JSONObject,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(BASE_URL + endpoint)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "서버 연결 실패: ${e.message}")
                onFailure("서버 연결 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    onSuccess(body)
                } else {
                    onFailure("응답 실패: $body")
                }
            }
        })
    }

    // 🔹 GET 요청 (ID 중복 체크 등)
    fun get(
        fullUrl: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url(fullUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "GET 실패: ${e.message}")
                onFailure("GET 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    onSuccess(body)
                } else {
                    onFailure("GET 응답 실패: $body")
                }
            }
        })
    }

    // 🔹 POST + JSON 응답 파싱용 (e.g., /downloadData)
    fun postForResult(
        endpoint: String,
        json: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(BASE_URL + endpoint)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "POST 실패: ${e.message}")
                onFailure("서버 연결 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val jsonObject = JSONObject(body)
                        onSuccess(jsonObject)
                    } catch (e: Exception) {
                        Log.e("ApiClient", "JSON 파싱 오류: ${e.message}")
                        onFailure("응답 파싱 실패: ${e.message}")
                    }
                } else {
                    onFailure("POST 응답 실패: $body")
                }
            }
        })
    }
}