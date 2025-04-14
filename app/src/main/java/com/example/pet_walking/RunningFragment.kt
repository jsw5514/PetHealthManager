package com.example.pet_walking

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.pet_walking.bluetooth.BluetoothDataListener
import com.example.pet_walking.bluetooth.BluetoothManager
import kotlin.math.*

class RunningFragment : Fragment(), BluetoothDataListener {

    private lateinit var startButton: Button
    private lateinit var goalButton: Button
    private lateinit var stopButton: Button

    private var goalDistance: Double? = null
    private var goalCalories: Double? = null
    private var running = false

    private var lastLat: Double? = null
    private var lastLon: Double? = null

    private var mapFragment: MapFragment? = null
    private var bluetoothManager: BluetoothManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.running_fragment, container, false)

        startButton = view.findViewById(R.id.startButton)
        goalButton = view.findViewById(R.id.goalButton)
        stopButton = view.findViewById(R.id.stopButton)
        stopButton.visibility = View.GONE

        if (savedInstanceState == null) {
            val map = MapFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, map)
                .commit()
            mapFragment = map
        } else {
            mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? MapFragment
        }

        goalButton.setOnClickListener { showGoalSettingDialog() }

        startButton.setOnClickListener {
            val pet = PetRepository.getCurrentPet()
            if (pet == null) {
                Toast.makeText(requireContext(), "반려동물 프로필을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startButton.visibility = View.GONE
            goalButton.visibility = View.GONE
            stopButton.visibility = View.VISIBLE
            running = true
            lastLat = null
            lastLon = null

            bluetoothManager = BluetoothManager(
                onDataReceived = { lat, lon, accX, accY, accZ ->
                    onBluetoothDataReceived(lat, lon, accX, accY, accZ)
                },
                onConnectionStatusChanged = { _, _ -> } // 필요 시 상태 표시 추가
            )
            bluetoothManager?.startListening()
        }

        stopButton.setOnClickListener {
            Toast.makeText(requireContext(), "러닝 수동 종료됨", Toast.LENGTH_SHORT).show()
            stopRunning()
        }

        return view
    }

    private fun stopRunning() {
        running = false
        bluetoothManager?.disconnect()
        bluetoothManager = null
        startButton.visibility = View.VISIBLE
        goalButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE
    }

    override fun onBluetoothDataReceived(lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float) {
        if (!running) return

        val pet = PetRepository.getCurrentPet() ?: return
        val map = mapFragment ?: return

        map.addLocation(lat, lon)

        val distance = if (lastLat != null && lastLon != null) {
            haversine(lastLat!!, lastLon!!, lat, lon)
        } else 0.0

        lastLat = lat
        lastLon = lon

        val activityIndex = calculateActivityIndex(accX, accY, accZ)
        val caloriesBurned = calculateCalories(activityIndex, pet.weight.toString().toDoubleOrNull() ?: 10.0, distance)

        pet.totalDistance += distance / 1000.0 // m → km
        pet.totalCalories += caloriesBurned

        val distanceReached = goalDistance?.let { pet.totalDistance >= it } ?: false
        val calorieReached = goalCalories?.let { pet.totalCalories >= it } ?: false

        if (distanceReached || calorieReached) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "🎉 목표 달성! 러닝 종료", Toast.LENGTH_LONG).show()
                stopRunning()
            }
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lon2 - lon1)

        val a = sin(dPhi / 2).pow(2.0) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun calculateActivityIndex(accX: Float, accY: Float, accZ: Float): Double {
        return sqrt(accX * accX + accY * accY + accZ * accZ.toDouble())
    }

    private fun calculateCalories(activityIndex: Double, weight: Double, distance: Double): Double {
        val MET = if (activityIndex < 1.5) 2.0 else 6.0
        val time = distance / (activityIndex + 1)
        val hours = time / 3600.0
        return MET * weight * hours
    }

    private fun showGoalSettingDialog() {
        val items = arrayOf("이동 거리 (km)", "소모 칼로리 (kcal)")
        AlertDialog.Builder(requireContext())
            .setTitle("목표 유형 선택")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showInputDialog("목표 거리", "km") { goalDistance = it; goalCalories = null }
                    1 -> showInputDialog("목표 칼로리", "kcal") { goalCalories = it; goalDistance = null }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showInputDialog(title: String, unit: String, onSet: (Double) -> Unit) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "숫자 입력 ($unit)"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("설정") { _, _ ->
                input.text.toString().toDoubleOrNull()?.let { onSet(it) }
                    ?: Toast.makeText(requireContext(), "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}