package com.example.pet_walking.Bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.pet_walking.Bluetooth.ParsedData
import java.io.InputStream
import java.util.*
import kotlin.concurrent.thread

class BluetoothManager(
    // 수신된 데이터가 정상적으로 파싱되었을 때 전달할 콜백
    private val onDataReceived: (ParsedData) -> Unit,

    // 연결 상태가 바뀌었을 때 (성공/실패/해제 등) 전달할 콜백
    private val onConnectionStatusChanged: (Boolean, String) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    @Volatile
    var isListening: Boolean = false
        private set

    // 메인 스레드에 포스트할 핸들러
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val TAG = "BluetoothManager"
    }

    fun getPairedDevices(): Set<BluetoothDevice>? {
        Log.d(TAG, "📱 페어링된 기기 요청됨")
        return bluetoothAdapter?.bondedDevices
    }

    fun connectToDevice(
        device: BluetoothDevice,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        thread {
            try {
                Log.d(TAG, "🔌 ${device.name}(${device.address}) 연결 시도")
                disconnect()

                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream

                Log.i(TAG, "✅ ${device.name} 연결 성공")
                // UI 업데이트 콜백은 메인 스레드로
                mainHandler.post {
                    onConnectionStatusChanged(true, "${device.name} 연결됨")
                    onSuccess()
                }

                startListening()
            } catch (e: Exception) {
                Log.e(TAG, "❌ 연결 실패: ${e.message}")
                mainHandler.post {
                    onConnectionStatusChanged(false, "연결 실패: ${device.name}")
                    onFailure()
                }
            }
        }
    }

    fun startListening() {
        if (isListening) {
            Log.w(TAG, "⛔ 이미 수신 중입니다.")
            return
        }

        if (bluetoothSocket?.isConnected != true || inputStream == null) {
            Log.w(TAG, "⚠️ 소켓 연결 또는 InputStream이 유효하지 않음")
            mainHandler.post {
                onConnectionStatusChanged(false, "수신 실패 (연결 없음)")
            }
            return
        }

        Log.d(TAG, "▶️ 데이터 수신 시작")
        isListening = true
        thread {
            try {
                listenForData()
            } catch (e: Exception) {
                Log.e(TAG, "❌ 수신 중 예외 발생: ${e.message}")
            } finally {
                isListening = false
                Log.i(TAG, "🛑 수신 종료")
            }
        }
    }

    private fun listenForData() {
        val buffer = ByteArray(1024)
        val sb = StringBuilder()

        try {
            while (isListening) {
                val bytes = inputStream?.read(buffer) ?: break
                val received = String(buffer, 0, bytes)
                Log.d(TAG, "📥 수신 데이터: $received")
                sb.append(received)

                var index: Int
                while (sb.indexOf("#").also { index = it } != -1) {
                    val fullLine = sb.substring(0, index).trim()
                    sb.delete(0, index + 1)
                    Log.d(TAG, "🔍 파싱 시도: $fullLine")

                    val parsed = parseReceivedData(fullLine)
                    if (parsed != null) {
                        Log.d(TAG, "✅ 파싱 성공: $parsed")
                        // onDataReceived 역시 메인 스레드로
                        mainHandler.post {
                            onDataReceived(parsed)
                        }
                    } else {
                        Log.w(TAG, "⚠️ 파싱 실패: $fullLine")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 수신 오류: ${e.message}")
            mainHandler.post {
                onConnectionStatusChanged(false, "데이터 수신 중 오류 발생")
            }
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔌 연결 해제 시도")
        isListening = false
        try {
            inputStream?.close()
            bluetoothSocket?.close()
            Log.i(TAG, "🔌 연결 정상 해제 완료")
            mainHandler.post {
                onConnectionStatusChanged(false, "연결 해제됨")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 해제 중 오류: ${e.message}")
        }
    }

    private fun parseReceivedData(line: String): ParsedData? {
        val parts = line.split(",")
            .map { it.trim().replace("<", "").replace(">", "") }
        if (parts.size != 5) return null

        val lat = parts[0].toDoubleOrNull() ?: return null
        val lon = parts[1].toDoubleOrNull() ?: return null
        val accX = parts[2].toFloatOrNull() ?: return null
        val accY = parts[3].toFloatOrNull() ?: return null
        val accZ = parts[4].toFloatOrNull() ?: return null

        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return null

        return ParsedData(lat, lon, accX, accY, accZ)
    }
}