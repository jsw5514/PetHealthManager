package com.example.pet_walking.chat

import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object ChatNetworkHelper {
    private val client = OkHttpClient()
    private const val serverUrl = "서버 주소"

    // 메시지 업로드
    fun postJson(endpoint: String, json: JSONObject, callback: (Boolean) -> Unit) {
        val body = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(serverUrl + endpoint)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful && response.body?.string() == "true")
            }
        })
    }

    // 메시지 수신
    fun postJsonWithResult(endpoint: String, json: JSONObject, callback: (JSONObject?) -> Unit) {
        val body = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(serverUrl + endpoint)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (response.isSuccessful && !bodyStr.isNullOrBlank()) {
                    callback(JSONObject(bodyStr))
                } else {
                    callback(null)
                }
            }
        })
    }
}