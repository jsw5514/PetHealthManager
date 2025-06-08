package com.example.pet_walking.feature.Running.Map

import android.util.Log
import com.example.pet_walking.network.ApiClient
import com.naver.maps.geometry.LatLng
import org.json.JSONObject
/**
 * Google Roads API를 사용하여 GPS 경로를 실제 도로에 맞춰 보정해주는 서비스 클래스
 */
object SnapToRoadsService {
    /**
     * 주어진 경로(path)를 Google Roads API를 사용하여 보정하고 콜백으로 결과를 반환
     *
     * @param path GPS 좌표 리스트 (LatLng)
     * @param apiKey Google Roads API Key
     * @param onResult 성공 시 호출될 콜백. 보정된 좌표 리스트가 전달됨
     * @param onError 실패 또는 예외 발생 시 호출될 콜백. 에러 메시지 전달
     */

    fun snapToRoads(
        path: List<LatLng>,
        apiKey: String,
        onResult: (List<LatLng>) -> Unit,
        onError: (String) -> Unit
    ) {
        // 경로가 비어있으면 보정하지 않고 바로 에러 처리
        if (path.isEmpty()) {
            onError("경로가 비어 있습니다.")
            return
        }
// 좌표들을 Roads API에서 요구하는 형식으로 변환
        val pathParam = path.joinToString("|") { "${it.latitude},${it.longitude}" }
        Log.d("SnapToRoadsService", "pathParam: $pathParam")
        val url = "https://roads.googleapis.com/v1/snapToRoads?interpolate=true&path=$pathParam&key=$apiKey"
        Log.d("SnapToRoadsService", "API 호출 URL: $url")
        val headers = mapOf(
            "X-Android-Package" to "com.example.pet_walking",
            "X-Android-Cert" to "EB:28:98:F4:F1:79:B7:CF:D3:93:71:AF:EA:A2:60:F4:2B:1F:20:29"
        )
// HTTP GET 요청 실행
        ApiClient.get(
            fullUrl = url,
            headers = headers,
            onSuccess = { response ->
                try {
                    // JSON 응답 파싱
                    val json = JSONObject(response)
                    val snappedPoints = json.getJSONArray("snappedPoints")
                    val correctedPath = mutableListOf<LatLng>()
// 각 보정된 좌표를 LatLng 리스트로 변환
                    for (i in 0 until snappedPoints.length()) {
                        val loc = snappedPoints.getJSONObject(i).getJSONObject("location")
                        val lat = loc.getDouble("latitude")
                        val lng = loc.getDouble("longitude")
                        correctedPath.add(LatLng(lat, lng))
                    }
                    //결과 콜백 호출
                    onResult(correctedPath)
                } catch (e: Exception) {
                    onError("파싱 오류: ${e.message}")
                }
            },
            onFailure = { error ->
                onError("요청 실패: $error")
            }
        )
    }
}