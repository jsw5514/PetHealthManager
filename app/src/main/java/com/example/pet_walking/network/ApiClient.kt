/*package com.example.pet_walking.network

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {

    private val client = OkHttpClient()

    // 서버 기본 URL (에뮬레이터 기준: 실제 기기에서 테스트할 경우 주소 변경 필요)
    //private const val BASE_URL = "https://bold-seal-only.ngrok-free.app"
    private const val BASE_URL = "http://10.0.2.2:8000"
    /**
     * 일반적인 POST 요청 (응답: 문자열)
     * @param endpoint API 엔드포인트 (예: "/signIn")
     * @param json 요청 바디로 전송할 JSON 객체
     * @param onSuccess 응답 성공 시 실행될 콜백 (문자열 응답)
     * @param onFailure 실패 시 실행될 콜백 (에러 메시지)
     */
    fun post(
        endpoint: String,
        json: JSONObject,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)
        val fullUrl = BASE_URL + endpoint

        val startTime = System.currentTimeMillis()//시간 측정 위한 함수

        Log.d("ApiClient", "POST 요청 시작: $fullUrl, payload: $json")

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val endTime = System.currentTimeMillis()//시간 측정 End함수
                Log.e("ApiClient", "서버 연결 실패: ${e.message} (${endTime - startTime} ms)")
                onFailure("서버 연결 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val endTime = System.currentTimeMillis()//시간 측정 End함수
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Log.d("ApiClient", "응답 성공: $body (${endTime - startTime} ms)")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "응답 실패: $body (${endTime - startTime} ms)")
                    onFailure("응답 실패: $body")
                }
            }
        })
    }

    /**
     * GET 요청
     * @param fullUrl 전체 요청 URL (BASE_URL이 이미 포함된 형태)
     * @param onSuccess 응답 성공 시 실행될 콜백 (문자열)
     * @param onFailure 실패 시 실행될 콜백 (에러 메시지)
     */
    fun get(
        fullUrl: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("ApiClient", "GET 요청 시작: $fullUrl")

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
                    Log.d("ApiClient", "GET 응답 성공: $body")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "GET 응답 실패: $body")
                    onFailure("GET 응답 실패: $body")
                }
            }
        })
    }

    // GET (headers 지원)
    fun get(
        fullUrl: String,
        headers: Map<String,String>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("ApiClient", "GET 요청 시작 (헤더 포함): $fullUrl")

        val builder = Request.Builder().url(fullUrl).get()
        for ((key, value) in headers) {
            builder.addHeader(key, value)
        }
        val request = builder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "GET 실패: ${e.message}")
                onFailure("GET 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Log.d("ApiClient", "GET 응답 성공: $body")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "GET 응답 실패: $body")
                    onFailure("GET 응답 실패: $body")
                }
            }
        })
    }

    /**
     * POST 요청 후 JSON 응답 기대 시 사용
     * @param endpoint API 엔드포인트
     * @param json 요청 JSON 객체
     * @param onSuccess 응답 성공 시 실행될 콜백 (JSONObject)
     * @param onFailure 실패 시 실행될 콜백 (에러 메시지)
     */
    fun postForResult(
        endpoint: String,
        json: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)
        val fullUrl = BASE_URL + endpoint

        Log.d("ApiClient", "POST (JSON 결과) 요청 시작: $fullUrl, payload: $json")

        val request = Request.Builder()
            .url(fullUrl)
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
                        Log.d("ApiClient", "JSON 파싱 성공: $jsonObject")
                        onSuccess(jsonObject)
                    } catch (e: Exception) {
                        Log.e("ApiClient", "JSON 파싱 오류: ${e.message}")
                        onFailure("응답 파싱 실패: ${e.message}")
                    }
                } else {
                    Log.e("ApiClient", "POST 응답 실패: $body")
                    onFailure("POST 응답 실패: $body")
                }
            }
        })
    }

    fun getByEndpoint(
        endpoint: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val fullUrl = BASE_URL + endpoint
        get(fullUrl, onSuccess, onFailure)
    }
}*/

package com.example.pet_walking.network

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object ApiClient {

    private val client = OkHttpClient()
    private const val BASE_URL = "https://bold-seal-only.ngrok-free.app"
    //private const val BASE_URL = "http://10.0.2.2:8000"

    fun post(
        endpoint: String,
        json: JSONObject,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)
        val fullUrl = BASE_URL + endpoint

        val startTime = System.currentTimeMillis()

        Log.d("ApiClient", "POST 요청 시작: $fullUrl, payload: $json")

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val endTime = System.currentTimeMillis()
                Log.e("ApiClient", "서버 연결 실패: ${e.message} (${endTime - startTime} ms)")
                onFailure("서버 연결 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val endTime = System.currentTimeMillis()
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Log.d("ApiClient", "응답 성공: $body (${endTime - startTime} ms)")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "응답 실패: $body (${endTime - startTime} ms)")
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
        Log.d("ApiClient", "GET 요청 시작: $fullUrl")

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
                    Log.d("ApiClient", "GET 응답 성공: $body")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "GET 응답 실패: $body")
                    onFailure("GET 응답 실패: $body")
                }
            }
        })
    }

    fun get(
        fullUrl: String,
        headers: Map<String, String>,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("ApiClient", "GET 요청 시작 (헤더 포함): $fullUrl")

        val builder = Request.Builder().url(fullUrl).get()
        for ((key, value) in headers) {
            builder.addHeader(key, value)
        }
        val request = builder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "GET 실패: ${e.message}")
                onFailure("GET 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Log.d("ApiClient", "GET 응답 성공: $body")
                    onSuccess(body)
                } else {
                    Log.e("ApiClient", "GET 응답 실패: $body")
                    onFailure("GET 응답 실패: $body")
                }
            }
        })
    }

    fun postForResult(
        endpoint: String,
        json: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)
        val fullUrl = BASE_URL + endpoint

        // metaData → dataType 으로 통일해 사용해야 하므로 호출부에서 수정 필요
        Log.d("ApiClient", "POST (JSON 결과) 요청 시작: $fullUrl, payload: $json")

        val request = Request.Builder()
            .url(fullUrl)
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
                        Log.d("ApiClient", "JSON 파싱 성공: $jsonObject")
                        onSuccess(jsonObject)
                    } catch (e: Exception) {
                        Log.e("ApiClient", "JSON 파싱 오류: ${e.message}")
                        onFailure("응답 파싱 실패: ${e.message}")
                    }
                } else {
                    Log.e("ApiClient", "POST 응답 실패: $body")
                    onFailure("POST 응답 실패: $body")
                }
            }
        })
    }

    fun getByEndpoint(
        endpoint: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val fullUrl = BASE_URL + endpoint
        get(fullUrl, onSuccess, onFailure)
    }
}