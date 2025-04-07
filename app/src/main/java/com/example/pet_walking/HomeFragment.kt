package com.example.pet_walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pet_walking.databinding.HomeFragmentBinding
import androidx.navigation.fragment.findNavController


class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        // 주간 목표 설정 버튼 클릭 시 GoalFragment로 이동
        binding.buttonSetWeeklyGoal.setOnClickListener {
            findNavController().navigate(R.id.goalFragment)
        }
        return binding.root
    }

    // 블루투스 상태 표시
    fun updateBluetoothStatus(status: String) {
        if (isAdded) {
            binding.bluetoothStatusTextView.text = "Bluetooth Status: $status"
        }
    }

    // 이동거리, 소모 칼로리 실시간 표시 함수
    /*fun updateStats(distance: Double, calories: Double) {
        if (isAdded) {
            val km = distance / 1000.0
            binding.textViewDistance.text = "총 이동 거리: %.2f km".format(km)
            binding.textViewCalories.text = "소모 칼로리: %.2f kcal".format(calories)

            // ✅ 목표 표시
            binding.textViewGoalSummary.text = GoalRepository.getGoalSummary()

            // ✅ 달성 여부 표시
            val goalReached = GoalRepository.isGoalReached(km, calories)
            binding.textViewGoalStatus.text = if (goalReached) "달성 여부: ✅" else "달성 여부: ❌"
            binding.textViewGoalStatus.setTextColor(
                resources.getColor(if (goalReached) R.color.green else R.color.red, null)
            )
        }
    }
*/
    fun updateStats() {
        if (isAdded) {
            val distance = SharedStatsRepository.totalDistance
            val calories = SharedStatsRepository.totalCalories
            val km = distance / 1000.0

            binding.textViewDistance.text = "총 이동 거리: %.2f km".format(km)
            binding.textViewCalories.text = "소모 칼로리: %.2f kcal".format(calories)

            // ✅ 목표 표시
            binding.textViewGoalSummary.text = GoalRepository.getGoalSummary()

            // ✅ 달성 여부 표시
            val goalReached = GoalRepository.isGoalReached(km, calories)
            binding.textViewGoalStatus.text = if (goalReached) "달성 여부: ✅" else "달성 여부: ❌"
            binding.textViewGoalStatus.setTextColor(
                resources.getColor(if (goalReached) R.color.green else R.color.red, null)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}