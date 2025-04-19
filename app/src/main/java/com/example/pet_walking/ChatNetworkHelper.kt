package com.example.pet_walking.chat

import com.example.pet_walking.network.ApiClient
import org.json.JSONObject

object ChatNetworkHelper {

    fun postJson(endpoint: String, json: JSONObject, callback: (Boolean) -> Unit) {
        ApiClient.post(
            endpoint,
            json,
            onSuccess = { response ->
                callback(response == "true")
            },
            onFailure = {
                callback(false)
            }
        )
    }

    fun postJsonWithResult(endpoint: String, json: JSONObject, callback: (JSONObject?) -> Unit) {
        ApiClient.post(
            endpoint,
            json,
            onSuccess = { response ->
                try {
                    callback(JSONObject(response))
                } catch (e: Exception) {
                    callback(null)
                }
            },
            onFailure = {
                callback(null)
            }
        )
    }
}