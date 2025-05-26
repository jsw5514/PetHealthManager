package com.example.pet_walking.Running.Map

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.example.pet_walking.R

/**
 * Naver Map을 표시하고, 외부에서 전달된 위치 데이터를 기반으로
 * 실시간으로 경로(Polyline)를 그리는 역할을 담당하는 프래그먼트
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    // Naver Map을 표시할 View
    private lateinit var mapView: MapView

    // 현재 위치 추적을 위한 위치 소스
    private lateinit var locationSource: FusedLocationSource

    // 위치 권한 요청 코드 (Android 6.0 이상 대응용)
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    // NaverMap 객체, 지도가 준비되면 초기화됨
    private var naverMap: NaverMap? = null

    // 지도 준비 여부를 외부에서 확인할 수 있도록 제공
    fun isMapReady(): Boolean = naverMap != null

    /**
     * 프래그먼트의 View를 생성하고, MapView 초기화
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 레이아웃 파일 연결
        val view = inflater.inflate(R.layout.map_fragment, container, false)

        // MapView 바인딩 및 초기화
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        // 비동기 방식으로 지도가 준비되면 콜백 받음
        mapView.getMapAsync(this)
        return view
    }

    /**
     * 지도가 준비되었을 때 호출되는 콜백 함수
     * 여기서 지도 설정 및 Polyline 초기화 진행
     */
    override fun onMapReady(naverMap: NaverMap) {
        Log.d("MapFragment", "✅ onMapReady 호출됨")
        this.naverMap = naverMap

        // 위치 소스를 설정해 지도에서 내 위치 버튼 동작 가능하게 함
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 기존에 저장된 경로가 있다면 그려줌
        PolylineManager.updatePolyline(naverMap, PathManager.getPoints())
    }

    /**
     * 외부에서 호출되어 위치 데이터를 추가하고 지도에 경로를 그림
     * @param lat 위도
     * @param lon 경도
     */
    fun addLocation(lat: Double, lon: Double) {
        Log.d("MapFragment", "📌 addLocation 호출됨: $lat, $lon")
        val map = naverMap ?: return

        // PathManager에 점 추가
        PathManager.addPoint(lat, lon)

        // Polyline 업데이트
        PolylineManager.updatePolyline(map, PathManager.getPoints())
    }

    /**
     * Google Roads API를 사용해 경로를 보정한 후 지도에 적용
     * @param apiKey Google Roads API 키
     */
    fun applyCorrectedPolyline(apiKey: String) {
        val map = naverMap ?: return
        val points = PathManager.getPoints()
        if (points.size < 2) return  // 보정할 만큼 데이터가 없으면 종료

        SnapToRoadsService.snapToRoads(
            path = points,
            apiKey = apiKey,
            onResult = { corrected ->
                PolylineManager.applyCorrected(corrected, map)
                Log.d("MapFragment", "✅ 보정된 선 그리기 완료")
            },
            onError = { error ->
                Log.e("MapFragment", "❌ 보정 실패: $error")
            }
        )
    }

    /**
     * 외부에서 보정된 선을 직접 넘겨줄 경우 지도에 적용
     */
    fun drawCorrectedPath(points: List<LatLng>) {
        PolylineManager.applyCorrected(points, naverMap ?: return)
    }

    // MapView 생명주기 연결 (Activity 생명주기와 동기화)
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }

    /**
     * Fragment가 종료될 때 MapView도 정리
     */

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
        naverMap = null  // 참조 해제
    }

    // 저메모리 경고 시 처리
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}