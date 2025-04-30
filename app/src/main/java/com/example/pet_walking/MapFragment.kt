package com.example.pet_walking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pet_walking.network.ApiClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import org.json.JSONObject

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private var naverMap: NaverMap? = null
    private val pathCoordinates = mutableListOf<LatLng>()
    private val polyline = PathOverlay()

    fun isMapReady(): Boolean = naverMap != null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.map_fragment, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("MapFragment", "✅ onMapReady 호출됨")
        this.naverMap = naverMap
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true

        polyline.color = 0xFF00AAFF.toInt() // 파란색
        polyline.width = 10

        if (pathCoordinates.size >= 2) {
            Log.d("MapFragment", "📌 onMapReady 내에서 경로 그리기")
            polyline.coords = pathCoordinates
            polyline.map = naverMap
        }
    }

    fun addLocation(lat: Double, lon: Double) {
        Log.d("MapFragment", "📌 addLocation 호출됨: $lat, $lon")

        val map = naverMap
        if (map == null) {
            Log.e("MapFragment", "❌ naverMap is null (아직 onMapReady 안됨)")
            return
        }

        val newPoint = LatLng(lat, lon)
        pathCoordinates.add(newPoint)
        Log.d("MapFragment", "🟢 좌표 추가됨, 총 ${pathCoordinates.size}개")

        if (pathCoordinates.size >= 2) {
            polyline.coords = pathCoordinates
            polyline.map = map
            Log.d("MapFragment", "✅ 선 연결 완료")
        } else {
            Log.w("MapFragment", "❌ 좌표 수 부족 (1개), 선 연결 생략")
        }
    }

    fun snapToRoads(
        path: List<LatLng>,
        apiKey: String,
        onResult: (List<LatLng>) -> Unit,
        onError: (String) -> Unit
    ) {
        val pathParam = path.joinToString("|") { "${it.latitude},${it.longitude}" }
        val url = "https://roads.googleapis.com/v1/snapToRoads?interpolate=true&path=$pathParam&key=$apiKey"

        ApiClient.get(fullUrl = url,
            onSuccess = { response ->
                try {
                    val json = JSONObject(response)
                    val snappedPoints = json.getJSONArray("snappedPoints")
                    val correctedPath = mutableListOf<LatLng>()

                    for (i in 0 until snappedPoints.length()) {
                        val loc = snappedPoints.getJSONObject(i).getJSONObject("location")
                        val lat = loc.getDouble("latitude")
                        val lng = loc.getDouble("longitude")
                        correctedPath.add(LatLng(lat, lng))
                    }

                    onResult(correctedPath)
                } catch (e: Exception) {
                    onError("파싱 오류: ${e.message}")
                }
            },
            onFailure = {
                onError("요청 실패: $it")
            }
        )
    }

    fun applyCorrectedPolyline(apiKey: String) {
        if (pathCoordinates.size < 2 || naverMap == null) return

        snapToRoads(
            path = pathCoordinates,
            apiKey = apiKey,
            onResult = { corrected ->
                polyline.coords = corrected
                polyline.map = naverMap
                Log.d("MapFragment", "✅ 보정된 선 그리기 완료")
            },
            onError = { error ->
                Log.e("SnapToRoads", "❌ 보정 실패: $error")
            }
        )
    }

    // 생명주기 동기화
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
        naverMap = null
    }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}