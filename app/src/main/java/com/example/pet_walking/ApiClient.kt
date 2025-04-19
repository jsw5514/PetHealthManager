package com.example.pet_walking.network

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {
    private val client = OkHttpClient()
    private const val BASE_URL = "http://10.0.2.2:8080" // 로컬 서버 IP (에뮬레이터 기준)

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
}