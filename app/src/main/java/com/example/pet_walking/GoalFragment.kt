package com.example.pet_walking

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class GoalFragment : Fragment() {

    private lateinit var inputDistanceKm: EditText
    private lateinit var inputCalories: EditText
    private lateinit var textExpectedCalories: TextView
    private lateinit var textRequiredDistance: TextView
    private lateinit var buttonSaveGoal: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.goal_fragment, container, false)

        inputDistanceKm = view.findViewById(R.id.inputDistanceKm)
        inputCalories = view.findViewById(R.id.inputCalories)
        textExpectedCalories = view.findViewById(R.id.textExpectedCalories)
        textRequiredDistance = view.findViewById(R.id.textRequiredDistance)
        buttonSaveGoal = view.findViewById(R.id.buttonSaveGoal)

        // 🔧 거리 입력 시 예상 칼로리 자동 계산
        inputDistanceKm.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val distance = s?.toString()?.toDoubleOrNull() ?: 0.0
                val weight = PetRepository.getCurrentPet()?.weight?.toDoubleOrNull() ?: 10.0
                val calories = calculateCalories(weight, distance)
                textExpectedCalories.text = "예상 칼로리: %.2f kcal".format(calories)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔧 칼로리 입력 시 필요한 거리 자동 계산
        inputCalories.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val calories = s?.toString()?.toDoubleOrNull() ?: 0.0
                val weight = PetRepository.getCurrentPet()?.weight?.toDoubleOrNull() ?: 10.0
                val distance = calculateDistanceFromCalories(weight, calories)
                textRequiredDistance.text = "필요한 거리: %.2f km".format(distance)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 목표 저장 버튼 클릭
        buttonSaveGoal.setOnClickListener {
            val distance = inputDistanceKm.text.toString().toDoubleOrNull()
            val calories = inputCalories.text.toString().toDoubleOrNull()

            GoalRepository.weeklyDistanceGoal = distance
            GoalRepository.weeklyCalorieGoal = calories

            Toast.makeText(requireContext(), "주간 목표가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // 홈으로 돌아가기
        }

        return view
    }

    // 거리 → 칼로리 계산
    private fun calculateCalories(weight: Double, distance: Double): Double {
        val MET = 4.0
        val hours = distance / 4.0
        return MET * weight * hours
    }

    // 칼로리 → 거리 계산
    private fun calculateDistanceFromCalories(weight: Double, calories: Double): Double {
        val MET = 4.0
        val hours = calories / (MET * weight)
        return hours * 4.0
    }
}