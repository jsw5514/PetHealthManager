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
        this.naverMap = naverMap
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true

        polyline.color = 0xFF00AAFF.toInt() // 파란색
        polyline.width = 10
    }

    // 외부에서 GPS 좌표 추가 → 선 연결
    fun addLocation(lat: Double, lon: Double) {
        val map = naverMap ?: return
        val newPoint = LatLng(lat, lon)
        pathCoordinates.add(newPoint)

        if (pathCoordinates.size >= 2) {
            polyline.coords = pathCoordinates
            polyline.map = map
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
            },
            onError = { error ->
                Log.e("SnapToRoads", "보정 실패: $error")
            }
        )
    }

    // 생명주기 동기화 (MapView는 반드시 필요)
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