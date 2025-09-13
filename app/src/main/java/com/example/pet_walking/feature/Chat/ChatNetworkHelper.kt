package com.example.pet_walking.feature.Chat

import com.example.pet_walking.network.ApiClient
import org.json.JSONArray
import org.json.JSONObject

object ChatNetworkHelper {

    /** 본문이 비어있어도, "true" 이거나 유효한 JSON(객체/배열)이면 성공 처리 */
    fun postJson(endpoint: String, json: JSONObject, callback: (Boolean) -> Unit) {
        ApiClient.post(
            endpoint,
            json,
            onSuccess = { response ->
                val body = response?.trim().orEmpty()
                val ok = try {
                    when {
                        body.isEmpty() -> true                       // ResponseEntity<Void>
                        body.equals("true", ignoreCase = true) -> true
                        body.startsWith("{") -> JSONObject(body) != null
                        body.startsWith("[") -> JSONArray(body) != null
                        else -> false
                    }
                } catch (_: Exception) { false }
                callback(ok)
            },
            onFailure = { callback(false) }
        )
    }

    /**
     * 서버가 객체 또는 배열을 돌려줘도 안전.
     * - 객체면 그대로 콜백
     * - 배열이면 {"contentList": [...]}로 래핑해서 콜백 (Chat 화면 호환)
     */
    fun postJsonWithResult(endpoint: String, json: JSONObject, callback: (JSONObject?) -> Unit) {
        ApiClient.post(
            endpoint,
            json,
            onSuccess = { response ->
                val body = response?.trim().orEmpty()
                try {
                    when {
                        body.startsWith("{") -> {
                            callback(JSONObject(body))
                        }
                        body.startsWith("[") -> {
                            val arr = JSONArray(body)
                            val wrapped = JSONObject().put("contentList", arr)
                            callback(wrapped)
                        }
                        body.isEmpty() -> {
                            // 내용이 없으면 결과 없음으로 간주
                            callback(JSONObject().put("contentList", JSONArray()))
                        }
                        else -> callback(null)
                    }
                } catch (_: Exception) {
                    callback(null)
                }
            },
            onFailure = { callback(null) }
        )
    }
}