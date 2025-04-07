package com.example.pet_walking

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pet_walking.PetRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RunningFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.running_fragment, container, false)

        val startButton: Button = view.findViewById(R.id.startButton)
        val goalButton: Button = view.findViewById(R.id.goalButton)

        // 지도 추가
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.mapContainer, MapFragment())
                .commit()
        }

        // 목표 설정
        goalButton.setOnClickListener {
            val editText = EditText(requireContext())
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            editText.hint = "목표 거리 (km)"

            AlertDialog.Builder(requireContext())
                .setTitle("목표 설정")
                .setMessage("목표 거리를 입력해주세요 (km 단위)")
                .setView(editText)
                .setPositiveButton("설정") { _, _ ->
                    val input = editText.text.toString()
                    val goalDistance = input.toDoubleOrNull()
                    if (goalDistance != null) {
                        Toast.makeText(requireContext(), "목표 거리: $goalDistance km 설정됨", Toast.LENGTH_SHORT).show()
                        // TODO: 추후 상태 저장 (Room)
                    } else {
                        Toast.makeText(requireContext(), "유효한 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 러닝 시작 (데이터 누적)
        startButton.setOnClickListener {
            val currentPet = PetRepository.getCurrentPet()
            if (currentPet == null) {
                Toast.makeText(requireContext(), "프로필을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? MapFragment
                while (mapFragment?.isMapReady() != true) {
                    delay(100)
                }

                repeat(10) { i ->
                    delay(1000)
                    val lat = 37.5665 + i * 0.0001
                    val lon = 126.9780 + i * 0.0001
                    mapFragment?.addLocation(lat, lon)

                    // ✅ 거리 및 칼로리 누적 (예시: 0.1km, 5kcal 증가)
                    currentPet.totalDistance += 0.1
                    currentPet.totalCalories += 5.0
                }

                Toast.makeText(requireContext(), "러닝 완료 - 총 거리 ${currentPet.totalDistance}km", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}