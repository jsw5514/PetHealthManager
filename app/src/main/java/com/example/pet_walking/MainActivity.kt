package com.example.pet_walking

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pet_walking.Bluetooth.BluetoothDataListener
import com.example.pet_walking.Home.HomeFragment
import com.example.pet_walking.Bluetooth.BluetoothManager
import com.example.pet_walking.databinding.ActivityMainBinding
import com.example.pet_walking.profile.repository.PetRepository
import com.example.pet_walking.profile.repository.UserRepository

class MainActivity : AppCompatActivity() {

    // ViewBinding 객체 → activity_main.xml과 연결
    private lateinit var binding: ActivityMainBinding

    // 블루투스 통신 전체를 담당할 BluetoothManager 인스턴스
    private lateinit var bluetoothManager: BluetoothManager

    // Android 12 이상에서 요구되는 권한 요청 코드
    private val BLUETOOTH_PERMISSION_REQUEST = 1001

    // Bluetooth 데이터를 수신할 프래그먼트에서 등록하는 리스너 (예: RunningFragment)
    private var dataListener: BluetoothDataListener? = null

    // BluetoothManager 인스턴스를 외부(프래그먼트 등)에서 가져갈 수 있게 제공
    fun getBluetoothManager(): BluetoothManager = bluetoothManager

    // Bluetooth 수신 데이터 리스너 등록 함수
    fun setBluetoothDataListener(listener: BluetoothDataListener?) {
        Log.d("MainActivity", "setBluetoothDataListener 호출됨")
        this.dataListener = listener
    }

    // 블루투스 데이터 수신 시작 (수동으로 호출해야 함)
    fun startListeningBluetooth() {
        Log.d("MainActivity", "startListeningBluetooth 호출됨")
        if (!bluetoothManager.isListening) bluetoothManager.startListening()
    }

    // 액티비티 생성 시 수행되는 초기화 루틴
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate 시작됨")

        // ViewBinding 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 유저와 펫 데이터 초기화 (로컬 + 서버)
        initUserAndPet()

        // 바텀 네비게이션바와 네비게이션 컨트롤러 연결
        setupNavigation()

        // BluetoothManager 인스턴스 초기화
        initBluetoothManager()

        // 블루투스 권한 체크 및 요청
        checkAndRequestBluetoothPermission()
    }

    /**
     * 로그인한 유저의 ID와 펫 ID 목록을 불러오고
     * 서버에서 펫 프로필들을 로드한 뒤 첫 번째 펫을 현재 선택된 펫으로 설정
     */
    private fun initUserAndPet() {
        Log.d("MainActivity", "initUserAndPet 호출됨")
        UserRepository.loadFromPreferences(this)  // SharedPreferences에서 유저 정보 복원
        val user = UserRepository.getCurrentUser()
        val petIds = user?.petIds ?: return  // 유저 또는 펫 목록이 없으면 종료

        PetRepository.loadProfilesFromServer(user.userId, petIds) {
            // 첫 번째 펫을 기본 선택
            petIds.firstOrNull()?.let {
                PetRepository.setCurrentPet(it)
                Log.d("MainActivity", "첫 번째 펫 선택됨: $it")
            }
        }
    }

    /**
     * 네비게이션 컨트롤러와 바텀 네비게이션 뷰를 연결하고,
     * 로그인되어 있지 않다면 LoginFragment로 이동시킴
     */
    private fun setupNavigation() {
        Log.d("MainActivity", "setupNavigation 호출됨")
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val navController = navHost?.navController

        navController?.let {
            // 바텀 네비게이션 UI 연결
            binding.bottomNavigationView.setupWithNavController(it)
            binding.bottomNavigationView.visibility = View.VISIBLE

            // 로그인되지 않았으면 로그인 화면으로 이동
            if (UserRepository.getCurrentUser() == null) {
                Log.d("MainActivity", "로그인 정보 없음 → loginFragment 이동")
                it.navigate(R.id.loginFragment)
            }
        }
    }

    /**
     * BluetoothManager 초기화.
     * 데이터 수신 시에는 dataListener를 통해 프래그먼트에 전달,
     * 연결 상태 변경 시에는 홈 화면에 상태 메시지 전달
     */
    private fun initBluetoothManager() {
        Log.d("MainActivity", "initBluetoothManager 호출됨")
        bluetoothManager = BluetoothManager(
            onDataReceived = { data ->
                Log.d("MainActivity", "Bluetooth 데이터 수신됨: $data")
                dataListener?.onBluetoothDataReceived(data.lat, data.lon, data.accX, data.accY, data.accZ)
            },
            onConnectionStatusChanged = { _, message ->
                Log.d("MainActivity", "Bluetooth 연결 상태 변경: $message")
                runOnUiThread { updateBluetoothStatus(message) }
            }
        )
    }

    /**
     * Android 12 이상에서 BLUETOOTH_CONNECT 권한이 필요한 경우 요청
     */
    private fun checkAndRequestBluetoothPermission() {
        Log.d("MainActivity", "checkAndRequestBluetoothPermission 호출됨")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {

            Log.d("MainActivity", "BLUETOOTH_CONNECT 권한 요청")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                BLUETOOTH_PERMISSION_REQUEST
            )
        }
    }

    /**
     * 사용자가 권한 요청에 응답했을 때 결과를 처리
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
            Log.d("MainActivity", "onRequestPermissionsResult - granted: $granted")
            Toast.makeText(
                this,
                if (granted) "블루투스 권한 허용됨" else "BLUETOOTH_CONNECT 권한이 필요합니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * HomeFragment에서 Bluetooth 연결 상태 메시지를 표시할 수 있도록 전달
     */
    private fun updateBluetoothStatus(message: String) {
        Log.d("MainActivity", "updateBluetoothStatus 호출됨: $message")
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val currentFragment = navHost?.childFragmentManager?.fragments?.firstOrNull()
        if (currentFragment is HomeFragment) {
            currentFragment.updateBluetoothStatus(message)
        }
    }
}