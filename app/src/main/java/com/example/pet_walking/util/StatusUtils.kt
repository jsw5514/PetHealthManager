package com.example.pet_walking.util

import kotlin.math.*

object StatusUtils {

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2).pow(2.0) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun calculateActivityIndex(accX: Float, accY: Float, accZ: Float): Double {
        return sqrt(accX.pow(2) + accY.pow(2) + accZ.pow(2)).toDouble()
    }

    fun calculateCalories(activityIndex: Double, weight: Double, distance: Double): Double {
        val MET = if (activityIndex < 1.5) 2.0 else 6.0
        val time = distance / (activityIndex + 1)
        val hours = time / 3600.0
        return MET * weight * hours
    }

    fun calculateDistanceFromCalories(weight: Double, calories: Double): Double {
        val MET = 4.0
        val hours = calories / (MET * weight)
        return hours * 4.0 // 평균 속도 4km/h 가정
    }
}