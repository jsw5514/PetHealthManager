package com.example.pet_walking

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
import com.example.pet_walking.bluetooth.BluetoothDataListener
import com.example.pet_walking.bluetooth.BluetoothManager
import com.example.pet_walking.chat.ChatNetworkHelper
import com.example.pet_walking.chat.ChatRoomManager
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.math.*

class RunningFragment : Fragment(), BluetoothDataListener {

    private lateinit var startButton: Button
    private lateinit var goalButton: Button
    private lateinit var stopButton: Button

    private var goalDistance: Double? = null
    private var goalCalories: Double? = null
    private var running = false

    private var lastLat: Double? = null
    private var lastLon: Double? = null

    private var startTime: Long = 0
    private var mapFragment: MapFragment? = null
    private var bluetoothManager: BluetoothManager? = null

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBluetoothDataListener(this)
        (activity as? MainActivity)?.startListeningBluetooth()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.running_fragment, container, false)

        startButton = view.findViewById(R.id.startButton)
        goalButton = view.findViewById(R.id.goalButton)
        stopButton = view.findViewById(R.id.stopButton)
        stopButton.visibility = View.GONE

        if (savedInstanceState == null) {
            val map = MapFragment()
            childFragmentManager.beginTransaction().replace(R.id.mapContainer, map).commit()
            mapFragment = map
        } else {
            mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? MapFragment
        }

        goalButton.setOnClickListener { showGoalSettingDialog() }

        startButton.setOnClickListener {
            val pet = PetRepository.getCurrentPet()
            if (pet == null) {
                Toast.makeText(requireContext(), "반려동물 프로필을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startButton.visibility = View.GONE
            goalButton.visibility = View.GONE
            stopButton.visibility = View.VISIBLE
            running = true
            lastLat = null
            lastLon = null
            startTime = System.currentTimeMillis()

            /*bluetoothManager = BluetoothManager(
                onDataReceived = { lat, lon, accX, accY, accZ ->
                    onBluetoothDataReceived(lat, lon, accX, accY, accZ)
                },
                onConnectionStatusChanged = { isConnected, _ ->
                    if (!isConnected) {
                        Toast.makeText(requireContext(), "❌ 블루투스 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            Log.e("lllllllll", "kasd")
*/
            /*val device = bluetoothManager?.getPairedDevices()?.firstOrNull()

            if (device != null) {
                Log.e("KKK", "ㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇ")
                bluetoothManager?.connectToDevice(
                    device,
                    onSuccess = { Log.d("RunningFragment", "✅ 블루투스 연결 성공") },
                    onFailure = { Log.e("RunningFragment", "❌ 블루투스 연결 실패") }
                )
            } else {
                Log.e("123123", "kdaslkdlsad")
                Toast.makeText(requireContext(), "페어링된 블루투스 기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }*/



        }

        stopButton.setOnClickListener {
            Toast.makeText(requireContext(), "러닝 종료됨", Toast.LENGTH_SHORT).show()
            stopRunning()
        }

        return view
    }
    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setBluetoothDataListener(null)
    }

    private fun stopRunning() {
        running = false
        /*
        bluetoothManager?.disconnect()
        bluetoothManager = null
         */
        startButton.visibility = View.VISIBLE
        goalButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE
        showShareOptionDialog()

        val pet = PetRepository.getCurrentPet()
        val userId =UserRepository.getCurrentUser()?.userId
        if (pet != null && userId != null) {
            uploadRunSummaryToServer(userId, pet.id, pet.totalDistance, pet.totalCalories)
        }
        //Roads API 보정 적용 정지 버튼 누를시 자동 보정함
        val apiKey = getString(R.string.google_roads_api_key)
        mapFragment?.applyCorrectedPolyline(apiKey)

        val bundle = Bundle().apply {
            putDouble("distance", pet?.totalDistance ?: 0.0)
            putDouble("calories", pet?.totalCalories ?: 0.0)
            putLong("durationMin", (System.currentTimeMillis() - startTime) / 1000 / 60)
        }
        findNavController().navigate(R.id.action_runningFragment_to_runSummaryFragment, bundle)
    }

    private fun showShareOptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("러닝 결과 공유")
            .setMessage("러닝 결과를 채팅방에 공유할까요?")
            .setPositiveButton("공유") { _, _ -> showChatRoomPickerDialog() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showChatRoomPickerDialog() {
        val userId = UserRepository.getCurrentUser()?.userId?:return
        ChatRoomManager.getJoinedChatRooms(userId) { rooms ->
            activity?.runOnUiThread {
                if (rooms.isEmpty()) {
                    Toast.makeText(requireContext(), "참여 중인 채팅방이 없습니다.", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                val roomTitles = rooms.map { "채팅방 ${it.first} (${it.second})" }.toTypedArray()
                val roomIds = rooms.map { it.first }

                AlertDialog.Builder(requireContext())
                    .setTitle("공유할 채팅방 선택")
                    .setItems(roomTitles) { _, which ->
                        val selectedRoomId = roomIds[which]
                        val layout = view?.findViewById<View>(R.id.runSummaryLayout)
                        updateRunSummaryUI()
                        if (layout != null) {
                            captureRunSummaryAndSendToChat(layout, selectedRoomId, userId)
                        } else {
                            Toast.makeText(requireContext(), "공유할 레이아웃을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }

    private fun updateRunSummaryUI() {
        val pet = PetRepository.getCurrentPet() ?: return
        val elapsedTimeSec = (System.currentTimeMillis() - startTime) / 1000
        val elapsedMin = elapsedTimeSec / 60

        view?.findViewById<TextView>(R.id.summaryDistance)?.text =
            "총 거리: %.2f km".format(pet.totalDistance)
        view?.findViewById<TextView>(R.id.summaryCalories)?.text =
            "소모 칼로리: %.0f kcal".format(pet.totalCalories)
        view?.findViewById<TextView>(R.id.summaryTime)?.text =
            "운동 시간: ${elapsedMin}분"
    }

    override fun onBluetoothDataReceived(lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float) {
        if (!running) return
        val pet = PetRepository.getCurrentPet() ?: return
        val map = mapFragment
        if (map == null || !map.isMapReady()) {
            Log.w("RunningFragment", "❌ Map이 아직 준비되지 않음 - addLocation 생략")
            return
        }

        map.addLocation(lat, lon)

        val distance = if (lastLat != null && lastLon != null) {
            haversine(lastLat!!, lastLon!!, lat, lon)
        } else 0.0
        lastLat = lat
        lastLon = lon

        val activityIndex = calculateActivityIndex(accX, accY, accZ)
        val caloriesBurned = calculateCalories(activityIndex, pet.weight.toString().toDoubleOrNull() ?: 10.0, distance)

        pet.totalDistance += distance / 1000.0
        pet.totalCalories += caloriesBurned

        val distanceReached = goalDistance?.let { pet.totalDistance >= it } ?: false
        val calorieReached = goalCalories?.let { pet.totalCalories >= it } ?: false

        if (distanceReached || calorieReached) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "목표 달성! 러닝 종료", Toast.LENGTH_LONG).show()
                stopRunning()
            }
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lon2 - lon1)
        val a = sin(dPhi / 2).pow(2.0) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun calculateActivityIndex(accX: Float, accY: Float, accZ: Float): Double {
        return sqrt(accX * accX + accY * accY + accZ * accZ.toDouble())
    }

    private fun calculateCalories(activityIndex: Double, weight: Double, distance: Double): Double {
        val MET = if (activityIndex < 1.5) 2.0 else 6.0
        val time = distance / (activityIndex + 1)
        val hours = time / 3600.0
        return MET * weight * hours
    }

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

    private fun showInputDialog(title: String, unit: String, onSet: (Double) -> Unit) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "숫자 입력 ($unit)"
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("설정") { _, _ ->
                input.text.toString().toDoubleOrNull()?.let { onSet(it) }
                    ?: Toast.makeText(requireContext(), "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    fun captureRunSummaryAndSendToChat(layout: View, roomId: Int, userId: String) {
        // 🔄 새 방식: View를 안전하게 비트맵으로 그리기
        val bitmap = Bitmap.createBitmap(layout.width, layout.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        layout.draw(canvas)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        val json = JSONObject().apply {
            put("roomId", roomId)
            put("writerId", userId)
            put("writeTime", System.currentTimeMillis())
            put("contentType", "image")
            put("content", base64Image)
        }

        ChatNetworkHelper.postJson("/uploadChat", json) { success ->
            if (success) {
                Log.d("RunningShare", "러닝 결과 이미지 전송 성공")
            }
        }
    }

    fun uploadRunSummaryToServer(userId: String, petId: UUID, distance: Double, calories: Double) {
        val json = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId", petId.toString()) // 각 펫 별로 구분
            put("metaData", "running_summary")
            put("data", JSONObject().apply {
                put("distance", distance)
                put("calories", calories)
                put("timestamp", System.currentTimeMillis())
            }.toString())
        }

        ChatNetworkHelper.postJson("/uploadData", json) { success ->
            if (success) Log.d("RunningSave", "러닝 통계 서버 저장 완료")
            else Log.w("RunningSave", "서버 저장 실패")
        }
    }
}