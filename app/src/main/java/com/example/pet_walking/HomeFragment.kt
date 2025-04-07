package com.example.pet_walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pet_walking.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
/*
    // 블루투스 데이터 표시 (필요한 경우 남겨둠)
    fun updateBluetoothData(data: String) {
        if (isAdded) {
            binding.bluetoothDataTextView.text = "Received Data: $data"
        }
    }
*/
    // 블루투스 상태 표시
    fun updateBluetoothStatus(status: String) {
        if (isAdded) {
            binding.bluetoothStatusTextView.text = "Bluetooth Status: $status"
        }
    }

    // 이동거리, 소모 칼로리 실시간 표시 함수
    fun updateStats(distance: Double, calories: Double) {
        if (isAdded) {
            binding.textViewDistance.text = "총 이동 거리: %.2f m".format(distance)
            binding.textViewCalories.text = "소모 칼로리: %.2f kcal".format(calories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}