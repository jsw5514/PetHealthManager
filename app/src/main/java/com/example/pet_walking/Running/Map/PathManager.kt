package com.example.pet_walking.Running.Map

import com.naver.maps.geometry.LatLng

/**
 * 러닝프래그먼트에서 블루투스 데이터 받음처리
 * MapFragment에서 PathManager.getPoints()를 이용해 Polyline에 그리기
 * 러닝 종료 후 PathManager.clear() 호출로 경로 초기화
 *
 * 전역에서 동일한 경로 좌표 상태 유지 위해 object로 정의
 */
object PathManager {
    private val pathCoordinates = mutableListOf<LatLng>()

    fun addPoint(lat: Double, lon: Double) {
        pathCoordinates.add(LatLng(lat, lon))
    }

    fun getPoints(): List<LatLng> = pathCoordinates

    fun clear() {
        pathCoordinates.clear()
    }

    fun size(): Int = pathCoordinates.size
}