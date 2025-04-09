package com.example.pet_walking

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pet_walking.bluetooth.BluetoothManager
import com.example.pet_walking.databinding.ActivityMainBinding
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val locationList = mutableListOf<Pair<Double, Double>>()

    private lateinit var bluetoothManager: BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val BLUETOOTH_PERMISSION_REQUEST = 1001



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PetRepository.loadFromPreferences(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 📌 네비게이션 바 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val navController = navHostFragment?.navController
        navController?.let { binding.bottomNavigationView.setupWithNavController(it) }

        // ✅ 블루투스 매니저 초기화
        bluetoothManager = BluetoothManager(
            onDataReceived = { lat, lon, accX, accY, accZ ->
                processReceivedData(lat, lon, accX, accY, accZ)
            },
            onConnectionStatusChanged = { isConnected, message ->
                runOnUiThread {
                    updateBluetoothStatus(message)
                }
            }
        )

        // ✅ 블루투스 권한 확인 후 기기 선택
        checkAndRequestBluetoothPermission()
    }

    private fun checkAndRequestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                    BLUETOOTH_PERMISSION_REQUEST
                )
            } else {
                showBluetoothDeviceDialog()
            }
        } else {
            showBluetoothDeviceDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showBluetoothDeviceDialog()
            } else {
                Toast.makeText(this, "BLUETOOTH_CONNECT 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 📡 블루투스 기기 선택 다이얼로그
    private fun showBluetoothDeviceDialog() {
        val pairedDevices = bluetoothManager.getPairedDevices()?.toList() ?: emptyList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "페어링된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceNames = pairedDevices.map { it.name ?: "이름 없는 기기" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("블루투스 기기 선택")
            .setItems(deviceNames) { _, which ->
                val device = pairedDevices[which]
                bluetoothManager.connectToDevice(
                    device,
                    onSuccess = { updateBluetoothStatus("Connected to ${device.name}") },
                    onFailure = { updateBluetoothStatus("Connection failed") }
                )
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 📍 GPS + 가속도 데이터 수신 시 처리
    fun processReceivedData(lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float) {
        locationList.add(lat to lon)

        val distance = if (locationList.size > 1) {
            val (prevLat, prevLon) = locationList[locationList.size - 2]
            haversine(prevLat, prevLon, lat, lon)
        } else 0.0

        val activityIndex = calculateActivityIndex(accX, accY, accZ)

        // ✅ 선택된 펫의 체중 사용 (없거나 숫자 변환 실패 시 기본값 10.0)
        val currentPet = PetRepository.getCurrentPet()
        val weight = currentPet?.weight?.let { it.toString().toDoubleOrNull() } ?: 10.0
        val caloriesBurned = calculateCalories(activityIndex, weight, distance)

        SharedStatsRepository.totalDistance += distance
        SharedStatsRepository.totalCalories += caloriesBurned

        updateVisibleFragments()
    }

    private fun updateVisibleFragments() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()

        if (currentFragment is HomeFragment && currentFragment.isVisible) {
            currentFragment.updateStats()
        }
        if (currentFragment is StatisticsFragment && currentFragment.isVisible) {
            currentFragment.updateStats()
        }
    }

    // 🟢 Bluetooth 연결 상태 텍스트 업데이트
    private fun updateBluetoothStatus(message: String) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
        if (currentFragment is HomeFragment) {
            currentFragment.updateBluetoothStatus(message)
        }
    }

    // 거리 계산 (Haversine)
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2).pow(2.0) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    // 활동 강도 계산
    private fun calculateActivityIndex(accX: Float, accY: Float, accZ: Float): Double {
        return sqrt(accX.pow(2) + accY.pow(2) + accZ.pow(2)).toDouble()
    }

    // 칼로리 계산
    private fun calculateCalories(activityIndex: Double, weight: Double, distance: Double): Double {
        val MET = if (activityIndex < 1.5) 2.0 else 6.0
        val time = distance / (activityIndex + 1)
        val hours = time / 3600.0
        return MET * weight * hours
    }
}