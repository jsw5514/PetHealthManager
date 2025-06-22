package com.example.pet_walking.feature.Running.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.Bluetooth.BluetoothDataListener
import com.example.pet_walking.feature.Chat.ChatNetworkHelper
import com.example.pet_walking.feature.Chat.ChatRoomManager
import com.example.pet_walking.MainActivity
import com.example.pet_walking.R
import com.example.pet_walking.feature.Running.Map.MapFragment
import com.example.pet_walking.feature.Running.Map.PathManager
import com.example.pet_walking.feature.Running.model.RunStats
import com.example.pet_walking.feature.Running.testRun.DemoRunSimulator
import com.example.pet_walking.feature.profile.data.PetProfile
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.feature.profile.repository.RunLogRepository
import com.example.pet_walking.feature.profile.repository.UserRepository
import com.example.pet_walking.feature.Running.uploader.StatsUploader
import com.example.pet_walking.util.StatusUtils
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class RunningFragment : Fragment(), BluetoothDataListener {

    // UI 구성요소
    private lateinit var startButton: Button
    private lateinit var goalButton: Button
    private lateinit var stopButton: Button

    // 러닝 상태 변수
    private var goalDistance: Double? = null
    private var goalCalories: Double? = null
    private var running = false
    private var lastLat: Double? = null
    private var lastLon: Double? = null
    private var startTime: Long = 0L

    // 지도 표시용 프래그먼트
    private var mapFragment: MapFragment? = null

    // Bluetooth 리스너 등록
    override fun onResume() {
        super.onResume()
        Log.d("RunningDebug", "Bluetooth listener 등록")
        (activity as? MainActivity)?.setBluetoothDataListener(this)
        (activity as? MainActivity)?.startListeningBluetooth()
    }

    // Bluetooth 리스너 해제
    override fun onPause() {
        super.onPause()
        Log.d("RunningDebug", "Bluetooth listener 해제")
        (activity as? MainActivity)?.setBluetoothDataListener(null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.running_fragment, container, false)

        // 버튼 초기화
        startButton = view.findViewById(R.id.startButton)
        goalButton = view.findViewById(R.id.goalButton)
        stopButton = view.findViewById(R.id.stopButton)
        stopButton.visibility = View.GONE

        // 지도 프래그먼트 삽입
        mapFragment = if (savedInstanceState == null) {
            MapFragment().also {
                childFragmentManager.beginTransaction().replace(R.id.mapContainer, it).commit()
            }
        } else childFragmentManager.findFragmentById(R.id.mapContainer) as? MapFragment

        // 버튼 이벤트 연결
        goalButton.setOnClickListener { showGoalSettingDialog() }
        startButton.setOnClickListener { startRunning() }
        stopButton.setOnClickListener {
            Toast.makeText(requireContext(), "러닝 종료됨", Toast.LENGTH_SHORT).show()
            stopRunning()
        }
        return view
    }

    // 러닝 시작
    private fun startRunning() {
        mapFragment?.startTracking()
        val pet = PetRepository.getCurrentPet()
        if (pet == null) {
            Toast.makeText(requireContext(), "반려동물 프로필을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        // ✅ 지도 초기화 (기존 선 제거)
        mapFragment?.clearPolyline()
        PathManager.clear()

        // ✅ 거리, 칼로리 초기화
        pet.totalDistance = 0.0
        pet.totalCalories = 0.0

        // UI 전환
        startButton.visibility = View.GONE
        goalButton.visibility = View.GONE
        stopButton.visibility = View.VISIBLE

        // 초기화
        running = true
        lastLat = null
        lastLon = null
        startTime = System.currentTimeMillis()

        Log.d("RunningDebug", "러닝 시작됨")
    }

    // 러닝 종료
    private fun stopRunning() {
        Log.d("RunningDebug", "stopRunning() 진입")
        running = false
        Log.d("RunningDebug", "러닝 종료됨")

        //위치 추적 중단
        mapFragment?.stopTracking()

        // UI 복구
        startButton.visibility = View.VISIBLE
        goalButton.visibility  = View.VISIBLE
        stopButton.visibility  = View.GONE

        // 공유 다이얼로그
        showShareOptionDialog()

        val pet    = PetRepository.getCurrentPet() ?: return
        val userId = UserRepository.getCurrentUser()?.userId ?: return

        // 1) RunStats 생성 & 로컬 저장
        val runStats = RunStats(
            distance  = pet.totalDistance,
            calories  = pet.totalCalories
        )
        RunLogRepository.addLog(runStats)

        // 2) 서버 업로드
        StatsUploader.upload(userId, pet.id, pet.totalDistance, pet.totalCalories)
        Log.d("RunningDebug", "upload() 호출 완료")
        StatsUploader.logRun(userId, pet.id, runStats)
        Log.d("RunningDebug", "✅ logRun() 호출 완료")

        // 3) 지도 보정
        mapFragment?.applyCorrectedPolyline(getString(R.string.google_roads_api_key))

        // 4) 요약 화면으로 이동 (Safe Args로 RunStats 전달)
        val action = RunningFragmentDirections.actionRunningFragmentToRunSummaryFragment(runStats)
        findNavController().navigate(action)

        (activity as? MainActivity)?.setBluetoothDataListener(null)
        (activity as? MainActivity)?.stopListeningBluetooth() // 수신 종료
    }

    // Bluetooth 데이터 수신 시 호출
    override fun onBluetoothDataReceived(lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float) {
        if (!running) return
        Log.d("RunningDebug", "데이터 수신됨: ($lat, $lon), 가속도=($accX, $accY, $accZ)")

        val pet = PetRepository.getCurrentPet() ?: return

        // 지도에 경로 추가
        mapFragment?.takeIf { it.isMapReady() }?.addLocation(lat, lon)

        // 이동 거리 계산
        /**
         * 아래 timeStart, timeEnd 요청 시간 측정을 위한 함수
         */
        val timeStart = System.currentTimeMillis()

        val distance = lastLat?.let { lat1 ->
            lastLon?.let { lon1 -> StatusUtils.haversine(lat1, lon1, lat, lon) }
        } ?: 0.0
        val timeEnd = System.currentTimeMillis()
        Log.d("RunningDebug", "이동 거리 계산: $distance m (${timeEnd - timeStart} ms 소요)")

        lastLat = lat
        lastLon = lon

        // 칼로리 계산
        val activityIndex = StatusUtils.calculateActivityIndex(accX, accY, accZ)
        val weight        = pet.weight.takeIf { it > 0 } ?: 10.0
        val calories      = StatusUtils.calculateCalories(activityIndex, weight, distance)
        Log.d("RunningDebug", "칼로리 계산: $calories kcal, 지수=$activityIndex")


        // 누적 기록
        pet.totalDistance += distance / 1000.0
        pet.totalCalories += calories

        Log.d("RunningDebug", "누적 거리=${pet.totalDistance} km, 누적 칼로리=${pet.totalCalories} kcal")

        // 목표 달성 시 자동 종료
        if (isGoalAchieved(pet)) {
            Log.d("RunningDebug", "목표 달성 완료 → 러닝 자동 종료")
            Toast.makeText(requireContext(), "목표 달성! 러닝 종료", Toast.LENGTH_LONG).show()
            stopRunning()
        }
    }

    // 목표 달성 여부 판별
    private fun isGoalAchieved(pet: PetProfile): Boolean {
        return (goalDistance?.let { pet.totalDistance >= it } ?: false) ||
                (goalCalories?.let { pet.totalCalories >= it } ?: false)
    }

    // 목표 설정 다이얼로그
    private fun showGoalSettingDialog() {
        val items = arrayOf("이동 거리 (km)", "소모 칼로리 (kcal)")
        AlertDialog.Builder(requireContext())
            .setTitle("목표 유형 선택")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showInputDialog("목표 거리", "km") { goalDistance = it; goalCalories = null }
                    1 -> showInputDialog("목표 칼로리", "kcal") { goalCalories = it; goalDistance = null }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 숫자 입력 다이얼로그
    private fun showInputDialog(title: String, unit: String, onSet: (Double) -> Unit) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "숫자 입력 ($unit)"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("설정") { _, _ ->
                input.text.toString().toDoubleOrNull()?.let(onSet)
                    ?: Toast.makeText(requireContext(), "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 공유 여부 다이얼로그
    private fun showShareOptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("러닝 결과 공유")
            .setMessage("러닝 결과를 채팅방에 공유할까요?")
            .setPositiveButton("공유") { _, _ -> showChatRoomPickerDialog() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 채팅방 선택 다이얼로그
    private fun showChatRoomPickerDialog() {
        val userId = UserRepository.getCurrentUser()?.userId ?: return
        ChatRoomManager.getJoinedChatRooms(userId) { rooms ->
            activity?.runOnUiThread {
                if (rooms.isEmpty()) {
                    Toast.makeText(requireContext(), "참여 중인 채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val titles = rooms.map { "채팅방 ${it.first} (${it.second})" }.toTypedArray()
                val ids = rooms.map { it.first }

                AlertDialog.Builder(requireContext())
                    .setTitle("공유할 채팅방 선택")
                    .setItems(titles) { _, which ->
                        val layout = view?.findViewById<View>(R.id.runSummaryLayout)
                        updateRunSummaryUI()
                        if (layout != null) {
                            captureRunSummaryAndSendToChat(layout, ids[which], userId)
                        } else {
                            Toast.makeText(requireContext(), "공유할 레이아웃을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }

    // 요약 정보 UI 업데이트
    private fun updateRunSummaryUI() {
        val pet = PetRepository.getCurrentPet() ?: return
        val elapsedMin = (System.currentTimeMillis() - startTime) / 60000
        view?.findViewById<TextView>(R.id.summaryDistance)?.text = "총 거리: %.2f km".format(pet.totalDistance)
        view?.findViewById<TextView>(R.id.summaryCalories)?.text = "소모 칼로리: %.0f kcal".format(pet.totalCalories)
        view?.findViewById<TextView>(R.id.summaryTime)?.text = "운동 시간: ${elapsedMin}분"
    }

    // 요약 이미지 캡처 후 채팅방 전송
    private fun captureRunSummaryAndSendToChat(layout: View, roomId: Int, userId: String) {
        val bitmap = Bitmap.createBitmap(layout.width, layout.height, Bitmap.Config.ARGB_8888)
        layout.draw(android.graphics.Canvas(bitmap))

        val output = ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }.toByteArray()

        val base64 = Base64.encodeToString(output, Base64.NO_WRAP)

        val json = JSONObject().apply {
            put("roomId", roomId)
            put("writerId", userId)
            put("writeTime", System.currentTimeMillis())
            put("contentType", "image")
            put("content", base64)
        }

        ChatNetworkHelper.postJson("/uploadChat", json) { success ->
            if (success) Log.d("RunningDebug", "러닝 요약 이미지 전송 성공")
        }
    }
}
/*
//테스트용 코드 시작

class RunningFragment : Fragment(), BluetoothDataListener {

    /* ====== 설정 ====== */
    /** true → BLE 없이 데모 시뮬레이터 실행, false → 실제 BLE 수신 */
    private val demoMode = true          // 필요하면 Preferences/빌드플래그로 전환

    /* ====== UI ====== */
    private lateinit var startButton: Button
    private lateinit var goalButton : Button
    private lateinit var stopButton : Button
    private var mapFragment: MapFragment? = null

    /* ====== 러닝 상태 ====== */
    private var running = false
    private var goalDistance : Double? = null
    private var goalCalories : Double? = null
    private var lastLat : Double? = null
    private var lastLon : Double? = null
    private var startTime = 0L

    /* ───────────────────────── onResume / onPause ───────────────────────── */
    override fun onResume() {
        super.onResume()
        if (!demoMode) {
            (activity as? MainActivity)?.setBluetoothDataListener(this)
            (activity as? MainActivity)?.startListeningBluetooth()
        }
    }
    override fun onPause() {
        super.onPause()
        if (!demoMode) {
            (activity as? MainActivity)?.setBluetoothDataListener(null)
        }
    }

    /* ───────────────────────── onCreateView ───────────────────────── */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.running_fragment, container, false)

        startButton = v.findViewById(R.id.startButton)
        goalButton  = v.findViewById(R.id.goalButton)
        stopButton  = v.findViewById(R.id.stopButton)
        stopButton.visibility = View.GONE

        // 지도 프래그먼트
        mapFragment = if (savedInstanceState == null) {
            MapFragment().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, it).commit()
            }
        } else childFragmentManager.findFragmentById(R.id.mapContainer) as? MapFragment

        goalButton.setOnClickListener { showGoalSettingDialog() }

        startButton.setOnClickListener {
            if (demoMode) startDemoRunning() else startRealRunning()
        }
        stopButton.setOnClickListener {
            Toast.makeText(requireContext(), "러닝 종료됨", Toast.LENGTH_SHORT).show()
            if (demoMode) stopDemoRunning() else stopRealRunning()
        }
        return v
    }

    /* ───────────────────────── 데모 모드 ───────────────────────── */
    private fun startDemoRunning() {
        prepareForRun()
        DemoRunSimulator.start { stats ->
            finalizeRun(stats)          // 시뮬레이터가 Stop 시점에 콜백
        }
    }
    private fun stopDemoRunning() {
        DemoRunSimulator.stop()        // stop → finalizeRun 콜백 내부 실행
    }

    /* ───────────────────────── 실제 BLE 모드 ───────────────────────── */
    private fun startRealRunning() = prepareForRun()

    private fun stopRealRunning() {
        running = false
        mapFragment?.stopTracking()
        restoreButtons()

        val pet = PetRepository.getCurrentPet() ?: return
        val runStats = RunStats(pet.totalDistance, pet.totalCalories)
        finalizeRun(runStats)
    }

    /* ───────────────────────── 공통 준비 / 종료 ───────────────────────── */
    private fun prepareForRun() {
        val pet = PetRepository.getCurrentPet()
        if (pet == null) {
            Toast.makeText(requireContext(), "반려동물 프로필을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        mapFragment?.startTracking()
        mapFragment?.clearPolyline();  PathManager.clear()

        pet.totalDistance = 0.0; pet.totalCalories = 0.0
        running = true
        lastLat = null; lastLon = null
        startTime = System.currentTimeMillis()

        startButton.visibility = View.GONE
        goalButton.visibility  = View.GONE
        stopButton.visibility  = View.VISIBLE
        Log.d("RunningDebug", "러닝 시작 (demo=$demoMode)")
    }

    private fun finalizeRun(stats: RunStats) {
        restoreButtons()

        val userId = UserRepository.getCurrentUserId() ?: return
        val pet    = PetRepository.getCurrentPet() ?: return

        // 서버 업로드
        StatsUploader.upload (userId, pet.id, stats.distance, stats.calories)
        StatsUploader.logRun(userId, pet.id, stats)

        // 지도 보정(선택) 후 요약 화면
        mapFragment?.applyCorrectedPolyline(getString(R.string.google_roads_api_key))
        val action = RunningFragmentDirections
            .actionRunningFragmentToRunSummaryFragment(stats)
        findNavController().navigate(action)

        if (!demoMode) (activity as? MainActivity)?.setBluetoothDataListener(null)
    }

    private fun restoreButtons() {
        startButton.visibility = View.VISIBLE
        goalButton.visibility  = View.VISIBLE
        stopButton.visibility  = View.GONE
    }

    /* ───────────────────────── BLE 데이터 수신 ───────────────────────── */
    override fun onBluetoothDataReceived(
        lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float
    ) {
        if (!running || demoMode) return      // 데모모드일 때는 무시
        // 아래 기존 로직 그대로 …
        val pet = PetRepository.getCurrentPet() ?: return
        mapFragment?.takeIf { it.isMapReady() }?.addLocation(lat, lon)

        val distance = lastLat?.let { StatusUtils.haversine(it, lastLon!!, lat, lon) } ?: 0.0
        lastLat = lat; lastLon = lon

        val ai   = StatusUtils.calculateActivityIndex(accX, accY, accZ)
        val kcal = StatusUtils.calculateCalories(ai, pet.weight.takeIf{it>0}?:10.0, distance)

        pet.totalDistance += distance / 1000.0
        pet.totalCalories += kcal

       /* if (isGoalAchieved(pet)) {
            Toast.makeText(requireContext(), "목표 달성!", Toast.LENGTH_LONG).show()
            stopRealRunning()
        }*/
    }

    /* ---- 나머지(showGoalSettingDialog, 공유/채팅 관련 함수 등은 원본 그대로) ---- */
    // … 아래 기존 코드 생략 (수정 없음) …
    // 목표 설정 다이얼로그
    private fun showGoalSettingDialog() {
        val items = arrayOf("이동 거리 (km)", "소모 칼로리 (kcal)")
        AlertDialog.Builder(requireContext())
            .setTitle("목표 유형 선택")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showInputDialog("목표 거리", "km") { goalDistance = it; goalCalories = null }
                    1 -> showInputDialog("목표 칼로리", "kcal") { goalCalories = it; goalDistance = null }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 숫자 입력 다이얼로그
    private fun showInputDialog(title: String, unit: String, onSet: (Double) -> Unit) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "숫자 입력 ($unit)"
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("설정") { _, _ ->
                input.text.toString().toDoubleOrNull()?.let(onSet)
                    ?: Toast.makeText(requireContext(), "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 공유 여부 다이얼로그
    private fun showShareOptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("러닝 결과 공유")
            .setMessage("러닝 결과를 채팅방에 공유할까요?")
            .setPositiveButton("공유") { _, _ -> showChatRoomPickerDialog() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 채팅방 선택 다이얼로그
    private fun showChatRoomPickerDialog() {
        val userId = UserRepository.getCurrentUser()?.userId ?: return
        ChatRoomManager.getJoinedChatRooms(userId) { rooms ->
            activity?.runOnUiThread {
                if (rooms.isEmpty()) {
                    Toast.makeText(requireContext(), "참여 중인 채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val titles = rooms.map { "채팅방 ${it.first} (${it.second})" }.toTypedArray()
                val ids = rooms.map { it.first }

                AlertDialog.Builder(requireContext())
                    .setTitle("공유할 채팅방 선택")
                    .setItems(titles) { _, which ->
                        val layout = view?.findViewById<View>(R.id.runSummaryLayout)
                        updateRunSummaryUI()
                        if (layout != null) {
                            captureRunSummaryAndSendToChat(layout, ids[which], userId)
                        } else {
                            Toast.makeText(requireContext(), "공유할 레이아웃을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }

    // 요약 정보 UI 업데이트
    private fun updateRunSummaryUI() {
        val pet = PetRepository.getCurrentPet() ?: return
        val elapsedMin = (System.currentTimeMillis() - startTime) / 60000
        view?.findViewById<TextView>(R.id.summaryDistance)?.text = "총 거리: %.2f km".format(pet.totalDistance)
        view?.findViewById<TextView>(R.id.summaryCalories)?.text = "소모 칼로리: %.0f kcal".format(pet.totalCalories)
        view?.findViewById<TextView>(R.id.summaryTime)?.text = "운동 시간: ${elapsedMin}분"
    }

    // 요약 이미지 캡처 후 채팅방 전송
    private fun captureRunSummaryAndSendToChat(layout: View, roomId: Int, userId: String) {
        val bitmap = Bitmap.createBitmap(layout.width, layout.height, Bitmap.Config.ARGB_8888)
        layout.draw(android.graphics.Canvas(bitmap))

        val output = ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }.toByteArray()

        val base64 = Base64.encodeToString(output, Base64.NO_WRAP)

        val json = JSONObject().apply {
            put("roomId", roomId)
            put("writerId", userId)
            put("writeTime", System.currentTimeMillis())
            put("contentType", "image")
            put("content", base64)
        }

        ChatNetworkHelper.postJson("/uploadChat", json) { success ->
            if (success) Log.d("RunningDebug", "러닝 요약 이미지 전송 성공")
        }
    }
}*/