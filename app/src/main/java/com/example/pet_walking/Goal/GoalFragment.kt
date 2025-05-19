package com.example.pet_walking.Goal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pet_walking.databinding.GoalFragmentBinding
import com.example.pet_walking.profile.repository.GoalRepository
import com.example.pet_walking.profile.repository.PetRepository
import com.example.pet_walking.profile.repository.UserRepository
import com.example.pet_walking.util.StatusUtils


class GoalFragment : Fragment() {

    private var _binding: GoalFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GoalFragmentBinding.inflate(inflater, container, false)

        val userId = UserRepository.getCurrentUser()?.userId
        val pet = PetRepository.getCurrentPet()
        val petId = pet?.id?.toString()
        val weight = pet?.weight

        if (userId == null || petId == null || weight == null) {
            Toast.makeText(requireContext(), "유저 또는 반려동물 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        setupDistanceWatcher(weight)
        setupCalorieWatcher(weight)
        setupSaveButton(userId, petId)

        return binding.root
    }

    // 거리 입력 시 예상 칼로리 계산
    private fun setupDistanceWatcher(weight: Double) {
        binding.inputDistanceKm.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val distance = s?.toString()?.toDoubleOrNull() ?: return
                val calories = StatusUtils.calculateCalories(4.0, weight, distance)
                binding.textExpectedCalories.text = "예상 칼로리: %.2f kcal".format(calories)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // 칼로리 입력 시 필요한 거리 계산
    private fun setupCalorieWatcher(weight: Double) {
        binding.inputCalories.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val calories = s?.toString()?.toDoubleOrNull() ?: return
                val distance = StatusUtils.calculateDistanceFromCalories(weight, calories)
                binding.textRequiredDistance.text = "필요한 거리: %.2f km".format(distance)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // 저장 버튼 클릭 시 목표 저장
    private fun setupSaveButton(userId: String, petId: String) {
        binding.buttonSaveGoal.setOnClickListener {
            val distance = binding.inputDistanceKm.text.toString().toDoubleOrNull()
            val calories = binding.inputCalories.text.toString().toDoubleOrNull()

            if (distance == null && calories == null) {
                Toast.makeText(requireContext(), "목표 거리 또는 칼로리를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            GoalRepository.setGoal(userId, petId, distance, calories)

            Toast.makeText(requireContext(), "주간 목표가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}