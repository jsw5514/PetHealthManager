package com.example.pet_walking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pet_walking.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val locationList = mutableListOf<Pair<Double, Double>>()

    private var totalDistance = 0.0
    private var totalCalories = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val navController = navHostFragment?.navController
        navController?.let { binding.bottomNavigationView.setupWithNavController(it) }

        startInfiniteSensorSimulation()
    }

    // 랜덤 데이터 시뮬레이션
    private fun startInfiniteSensorSimulation() {
        CoroutineScope(Dispatchers.Main).launch {
            var step = 0
            while (true) {
                step++
                val latitude = 37.5665 + step * 0.0001
                val longitude = 126.9780 + step * 0.0001
                val accX = Random.nextDouble(0.5, 1.5).toFloat()
                val accY = Random.nextDouble(0.5, 1.5).toFloat()
                val accZ = Random.nextDouble(0.5, 1.5).toFloat()

                processReceivedData(latitude, longitude, accX, accY, accZ)

                delay(1000) // 1초마다 데이터 추가
            }
        }
    }


    private fun processReceivedData(lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float) {
        locationList.add(lat to lon)

        val distance = if (locationList.size > 1) {
            val (prevLat, prevLon) = locationList[locationList.size - 2]
            haversine(prevLat, prevLon, lat, lon)
        } else 0.0

        totalDistance += distance

        val activityIndex = calculateActivityIndex(accX, accY, accZ)
        val caloriesBurned = calculateCalories(activityIndex, 10.0, distance)

        totalCalories += caloriesBurned

        updateHomeAndStatisticsFragments(totalDistance, totalCalories)
    }

    private fun updateHomeAndStatisticsFragments(totalDistance: Double, totalCalories: Double) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()

        if (currentFragment is HomeFragment && currentFragment.isVisible) {
            currentFragment.updateStats(totalDistance, totalCalories)
        }

        if (currentFragment is StatisticsFragment && currentFragment.isVisible) {
            currentFragment.updateStats(totalDistance, totalCalories)
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) * sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    private fun calculateActivityIndex(accX: Float, accY: Float, accZ: Float): Double {
        return sqrt(accX.pow(2) + accY.pow(2) + accZ.pow(2)).toDouble()
    }

    private fun calculateCalories(activityIndex: Double, weight: Double, distance: Double): Double {
        val MET = if (activityIndex < 1.5) 2.0 else 6.0
        val time = distance / (activityIndex + 1)
        val hours = time / 3600.0
        return MET * weight * hours
    }
}