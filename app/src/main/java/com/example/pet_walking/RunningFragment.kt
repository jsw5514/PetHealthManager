package com.example.pet_walking

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RunningFragment : Fragment() {

    private lateinit var startButton: Button
    private lateinit var goalButton: Button
    private lateinit var stopButton: Button

    private var goalDistance: Double? = null
    private var goalCalories: Double? = null
    private var running = false

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
            childFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, MapFragment())
                .commit()
        }

        // 🎯 목표 설정 버튼
        goalButton.setOnClickListener {
            showGoalSettingDialog()
        }

        // ▶️ 러닝 시작
        startButton.setOnClickListener {
            val currentPet = PetRepository.getCurrentPet()
            if (currentPet == null) {
                Toast.makeText(requireContext(), "반려동물 프로필을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startButton.visibility = View.GONE
            goalButton.visibility = View.GONE
            stopButton.visibility = View.VISIBLE
            running = true

            lifecycleScope.launch {
                val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? MapFragment
                while (mapFragment?.isMapReady() != true) delay(100)

                repeat(1000) { i ->
                    if (!isActive || !running) return@repeat

                    delay(1000)

                    // ✅ 임의 위치 데이터 시뮬레이션
                    val lat = 37.5665 + i * 0.0001
                    val lon = 126.9780 + i * 0.0001
                    mapFragment?.addLocation(lat, lon)

                    // ✅ 거리, 칼로리 계산 (임의 누적)
                    val pet = PetRepository.getCurrentPet()!!
                    val stepDistance = 0.05 // km
                    val caloriesBurned = calculateCalories(pet.weight.toDoubleOrNull() ?: 10.0, stepDistance)

                    pet.totalDistance += stepDistance
                    pet.totalCalories += caloriesBurned

                    // 🎯 목표 달성 체크
                    val distanceReached = goalDistance?.let { pet.totalDistance >= it } ?: false
                    val calorieReached = goalCalories?.let { pet.totalCalories >= it } ?: false

                    if (distanceReached || calorieReached) {
                        Toast.makeText(requireContext(), "🎉 목표 달성! 러닝 종료", Toast.LENGTH_LONG).show()
                        stopRunning()
                        return@repeat
                    }
                }
            }
        }

        // ⏹️ 수동 종료
        stopButton.setOnClickListener {
            Toast.makeText(requireContext(), "러닝 수동 종료됨", Toast.LENGTH_SHORT).show()
            stopRunning()
        }

        return view
    }

    private fun stopRunning() {
        running = false
        startButton.visibility = View.VISIBLE
        goalButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE

        // TODO: 통계Fragment에 누적 값 전달할 수 있음
    }

    // 🔧 거리 or 칼로리 목표 설정 다이얼로그
    private fun showGoalSettingDialog() {
        val items = arrayOf("이동 거리 (km)", "소모 칼로리 (kcal)")
        AlertDialog.Builder(requireContext())
            .setTitle("목표 유형 선택")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showInputDialog("목표 거리", "km") { value ->
                        goalDistance = value
                        goalCalories = null
                        Toast.makeText(requireContext(), "목표 거리 ${value}km 설정됨", Toast.LENGTH_SHORT).show()
                    }
                    1 -> showInputDialog("목표 칼로리", "kcal") { value ->
                        goalCalories = value
                        goalDistance = null
                        Toast.makeText(requireContext(), "목표 칼로리 ${value}kcal 설정됨", Toast.LENGTH_SHORT).show()
                    }
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
                val value = input.text.toString().toDoubleOrNull()
                if (value != null) onSet(value)
                else Toast.makeText(requireContext(), "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 🔥 칼로리 계산 공식
    private fun calculateCalories(weight: Double, distanceKm: Double): Double {
        val MET = 4.0 // 보통 속도 기준
        val hours = distanceKm / 4.0 // 평균 속도 4km/h 가정
        return MET * weight * hours
    }
}