package com.example.pet_walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private var naverMap: NaverMap? = null //null 가능성 반영
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

        // 초기 설정만 하고, map에는 나중에 좌표 2개 이상일 때만 넣는다
        polyline.color = 0xFF00AAFF.toInt()
        polyline.width = 10
    }

    // 외부에서 호출: 위치를 추가하고 선을 그림
    fun addLocation(lat: Double, lon: Double) {
        val map = naverMap ?: return // 초기화 안됐으면 리턴

        val newPoint = LatLng(lat, lon)
        pathCoordinates.add(newPoint)

        if (pathCoordinates.size >= 2) {
            polyline.coords = pathCoordinates
            polyline.map = map
        }
        if (pathCoordinates.size >= 2) {
            polyline.coords = pathCoordinates
            polyline.map = map // 여기서만 map 설정
        }
    }

    // 생명주기 동기화
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onDestroyView() { mapView.onDestroy(); super.onDestroyView(); naverMap = null }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}