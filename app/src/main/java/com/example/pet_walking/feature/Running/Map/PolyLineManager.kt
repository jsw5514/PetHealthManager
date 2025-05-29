package com.example.pet_walking.feature.Running.Map

import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.geometry.LatLng

object PolylineManager {
    private val polyline = PathOverlay().apply {
        color = 0xFF00AAFF.toInt()
        width = 10
    }

    fun updatePolyline(map: NaverMap, points: List<LatLng>) {
        if (points.size >= 2) {
            polyline.coords = points
            polyline.map = map
        } else {
            polyline.map = null
        }
    }

    fun applyCorrected(points: List<LatLng>, map: NaverMap) {
        polyline.coords = points
        polyline.map = map
    }
}