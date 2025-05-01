package com.example.pet_walking

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.bluetooth.BluetoothManager
import com.example.pet_walking.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    //MainActivity 의 매니저를 그대로 사용
    // private lateinit var bluetoothManager: BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    /* ─────────────────────────────────────────── */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        checkBluetoothPermissions()

        /* ✅ MainActivity 로부터 기존 매니저 참조 */
        val main = activity as? MainActivity ?: return binding.root
        val bluetoothManager: BluetoothManager = main.getBluetoothManager()

        /* 초기 상태 */
        updateBluetoothStatus("Disconnected", false)

        /* 연결 버튼(텍스트) 탭 → 기기 선택 */
        binding.bluetoothStatusTextView.setOnClickListener {
            showBluetoothDeviceDialog { device ->
                bluetoothManager.connectToDevice(
                    device,
                    onSuccess = {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "✅ 블루투스 연결 성공", Toast.LENGTH_SHORT).show()
                            bluetoothManager.startListening()     // 한 곳에서만 listen
                        }
                    },
                    onFailure = {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "❌ 블루투스 연결 실패", Toast.LENGTH_SHORT).show()
                            updateBluetoothStatus("Connection Failed", false)
                        }
                    }
                )
            }
        }

        /* 주간 목표 설정 화면 이동 */
        binding.buttonSetWeeklyGoal.setOnClickListener {
            findNavController().navigate(R.id.goalFragment)
        }

        return binding.root
    }

    /* ────── 기기 선택 다이얼로그 ────── */

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
                onDeviceSelected(pairedDevices[which])
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /* ────── 권한 체크 ────── */

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val need = bluetoothPermissions.any {
                ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
            }
            if (need) {
                ActivityCompat.requestPermissions(requireActivity(), bluetoothPermissions, 1001)
            }
        }
    }

    /* ────── UI 헬퍼 ────── */

    fun updateBluetoothStatus(status: String) = updateBluetoothStatus(status, false)

    fun updateBluetoothStatus(status: String, isConnected: Boolean) {
        binding.bluetoothStatusTextView.text = "Bluetooth Status: $status"
        val colorRes = if (isConnected) R.color.green else R.color.red
        binding.bluetoothStatusTextView.setTextColor(requireContext().getColor(colorRes))
    }

    fun updateStats() {
        if (!isAdded) return

        val distanceM = SharedStatsRepository.totalDistance
        val calories  = SharedStatsRepository.totalCalories
        val km = distanceM / 1000.0

        binding.textViewDistance.text  = "총 이동 거리: %.2f km".format(km)
        binding.textViewCalories.text  = "소모 칼로리: %.2f kcal".format(calories)

        binding.textViewGoalSummary.text = GoalRepository.getGoalSummary()
        val reached = GoalRepository.isGoalReached(km, calories)
        binding.textViewGoalStatus.text = if (reached) "달성 여부: ✅" else "달성 여부: ❌"
        binding.textViewGoalStatus.setTextColor(
            requireContext().getColor(if (reached) R.color.green else R.color.red)
        )
    }

    /* ────── 라이프사이클 ────── */

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}