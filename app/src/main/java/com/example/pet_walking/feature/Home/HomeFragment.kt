package com.example.pet_walking.feature.Home

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.MainActivity
import com.example.pet_walking.R
import com.example.pet_walking.Bluetooth.BluetoothManager
import com.example.pet_walking.feature.Running.model.RunStats
import com.example.pet_walking.databinding.HomeFragmentBinding
import com.example.pet_walking.feature.profile.repository.GoalRepository
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.feature.profile.repository.UserRepository
import com.example.pet_walking.feature.statistics.StatsLoader

class HomeFragment : Fragment() {

    // ViewBinding 객체
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    // 블루투스 어댑터 객체 (기기 내장)
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Android 12 이상에서 요구되는 권한 목록
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    // 뷰 생성 시 실행되는 함수
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        // 권한 요청
        checkBluetoothPermissions()

        // MainActivity에서 BluetoothManager 가져오기
        val main = activity as? MainActivity ?: return binding.root
        val bluetoothManager: BluetoothManager = main.getBluetoothManager()

        // 초기 블루투스 상태 표시
        updateBluetoothStatus("Disconnected", false)

        // 블루투스 상태 텍스트 클릭 → 기기 선택 다이얼로그 실행
        binding.bluetoothStatusTextView.setOnClickListener {
            showBluetoothDeviceDialog { device ->
                bluetoothManager.connectToDevice(
                    device,
                    onSuccess = {
                        // 연결 성공 시 UI만 갱신 (startListening은 내부에서 이미 호출됨)
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "✅ 블루투스 연결 성공", Toast.LENGTH_SHORT).show()
                            updateBluetoothStatus("${device.name} 연결됨", true)
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

        // 목표 설정 버튼 → GoalFragment 이동
        binding.buttonSetWeeklyGoal.setOnClickListener {
            findNavController().navigate(R.id.goalFragment)
        }

        return binding.root
    }

    // 블루투스 기기 선택 다이얼로그 생성 함수
    private fun showBluetoothDeviceDialog(onDeviceSelected: (BluetoothDevice) -> Unit) {
        // 블루투스 지원 안되는 경우
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "이 장치는 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 블루투스 꺼져 있는 경우 → 사용자에게 활성화 요청
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
            return
        }

        // 페어링된 기기 목록 확인
        val pairedDevices = bluetoothAdapter.bondedDevices.toList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(requireContext(), "페어링된 기기가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 기기 목록을 다이얼로그로 표시
        val deviceNames = pairedDevices.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("블루투스 기기 선택")
            .setItems(deviceNames) { _, which ->
                onDeviceSelected(pairedDevices[which])
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // Android 12 이상에서 동적 권한 요청
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

    // 블루투스 상태 텍스트만 갱신하는 함수 (기본 연결 아님)
    fun updateBluetoothStatus(status: String) = updateBluetoothStatus(status, false)

    // 블루투스 상태 텍스트와 색상 갱신
    fun updateBluetoothStatus(status: String, isConnected: Boolean) {
        binding.bluetoothStatusTextView.text = "Bluetooth Status: $status"
        val colorRes = if (isConnected) R.color.green else R.color.red
        binding.bluetoothStatusTextView.setTextColor(requireContext().getColor(colorRes))
    }

    // Fragment가 다시 보일 때 실행됨 → 통계 정보 서버에서 로딩
    override fun onResume() {
        super.onResume()

        val user = UserRepository.getCurrentUser()
        val pet = PetRepository.getCurrentPet()

        // 유저나 반려동물 정보가 없으면 기본값 표시 후 종료
        if (user == null || pet == null) {
            binding.textViewDistance.text = "총 이동 거리: -"
            binding.textViewCalories.text = "소모 칼로리: -"
            binding.textViewGoalSummary.text = "목표 없음"
            binding.textViewGoalStatus.text = "달성 여부: -"
            binding.textViewGoalStatus.setTextColor(requireContext().getColor(R.color.gray))
            return
        }

        // 서버로부터 통계 정보 로딩
        StatsLoader.loadRunLogs(user.userId, pet.id.toString(), { runs: List<RunStats> ->
            // runs 의 distance 필드는 km 단위로 저장되어 있다고 가정
            val totalDistance = runs.sumOf { it.distance }
            val totalCalories = runs.sumOf { it.calories }

            requireActivity().runOnUiThread {
                // 누적 통계 표시
                binding.textViewDistance.text = "총 이동 거리: %.2f km".format(totalDistance)
                binding.textViewCalories.text = "소모 칼로리: %.2f kcal".format(totalCalories)

                // 기존 목표 표시
                binding.textViewGoalSummary.text = GoalRepository
                    .getGoalSummary(user.userId, pet.id.toString())
                val reached = GoalRepository.isGoalReached(
                    user.userId, pet.id.toString(), totalDistance, totalCalories
                )
                binding.textViewGoalStatus.apply {
                    text = if (reached) "달성 여부: ✅" else "달성 여부: ❌"
                    setTextColor(requireContext().getColor(
                        if (reached) R.color.green else R.color.red
                    ))
                }
            }
        }, { error ->
            // 에러 발생 시 로그 출력만
            Log.e("HomeFragment", "러닝 로그 로딩 실패", error)
        })
    }

    // 뷰가 파괴될 때 ViewBinding 해제 → 메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}