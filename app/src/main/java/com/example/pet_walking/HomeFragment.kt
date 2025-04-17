package com.example.pet_walking

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.bluetooth.BluetoothManager
import com.example.pet_walking.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var bluetoothManager: BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        bluetoothManager = BluetoothManager(
            onDataReceived = { lat, lon, accX, accY, accZ ->
                (activity as? MainActivity)?.processReceivedData(lat, lon, accX, accY, accZ)

                // ✅ 실시간 데이터 표시
                requireActivity().runOnUiThread {
                    binding.rawDataTextView.text = buildString {
                        append("📡 실시간 수신 데이터\n")
                        append("위도: $lat\n")
                        append("경도: $lon\n")
                        append("accX: $accX\n")
                        append("accY: $accY\n")
                        append("accZ: $accZ")
                    }
                }
            },
            onConnectionStatusChanged = { isConnected, message ->
                requireActivity().runOnUiThread {
                    updateBluetoothStatus(message, isConnected)
                    if (!isConnected) {
                        binding.rawDataTextView.text = "📴 블루투스 연결이 해제되었습니다."
                    }
                }
            }
        )

        updateBluetoothStatus("Disconnected", false)

        // 📡 상태 클릭 → 기기 선택
        binding.bluetoothStatusTextView.setOnClickListener {
            showBluetoothDeviceDialog { device ->
                bluetoothManager.connectToDevice(
                    device,
                    onSuccess = {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "✅ 블루투스 연결 성공", Toast.LENGTH_SHORT).show()
                            bluetoothManager.startListening()
                        }
                    },
                    onFailure = {
                        requireActivity().runOnUiThread {
                            Log.e("HomeFragment", "블루투스 연결 실패")
                            Toast.makeText(requireContext(), "❌ 블루투스 연결 실패", Toast.LENGTH_SHORT).show()
                            updateBluetoothStatus("Connection Failed", false)
                        }
                    }
                )
            }
        }

        // 🎯 주간 목표 설정 이동
        binding.buttonSetWeeklyGoal.setOnClickListener {
            findNavController().navigate(R.id.goalFragment)
        }

        return binding.root
    }

    private fun showBluetoothDeviceDialog(onDeviceSelected: (BluetoothDevice) -> Unit) {
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "이 장치는 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
            return
        }

        val pairedDevices = bluetoothAdapter.bondedDevices.toList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(requireContext(), "페어링된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceNames = pairedDevices.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("블루투스 기기 선택")
            .setItems(deviceNames) { _, which ->
                val device = pairedDevices[which]
                onDeviceSelected(device)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    fun updateBluetoothStatus(status: String) {
        updateBluetoothStatus(status, false)
    }

    fun updateBluetoothStatus(status: String, isConnected: Boolean) {
        binding.bluetoothStatusTextView.text = "Bluetooth Status: $status"
        val colorRes = if (isConnected) R.color.green else R.color.red
        binding.bluetoothStatusTextView.setTextColor(requireContext().getColor(colorRes))
    }

    fun updateStats() {
        if (isAdded) {
            val distance = SharedStatsRepository.totalDistance
            val calories = SharedStatsRepository.totalCalories
            val km = distance / 1000.0

            binding.textViewDistance.text = "총 이동 거리: %.2f km".format(km)
            binding.textViewCalories.text = "소모 칼로리: %.2f kcal".format(calories)

            binding.textViewGoalSummary.text = GoalRepository.getGoalSummary()
            val goalReached = GoalRepository.isGoalReached(km, calories)
            binding.textViewGoalStatus.text = if (goalReached) "달성 여부: ✅" else "달성 여부: ❌"
            binding.textViewGoalStatus.setTextColor(
                requireContext().getColor(if (goalReached) R.color.green else R.color.red)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}