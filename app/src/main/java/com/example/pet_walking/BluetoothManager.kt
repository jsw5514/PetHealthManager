package com.example.pet_walking.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.InputStream
import java.util.*
import kotlin.concurrent.thread

class BluetoothManager(
    private val onDataReceived: (Double, Double, Float, Float, Float) -> Unit,
    private val onConnectionStatusChanged: (Boolean, String) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getPairedDevices(): Set<BluetoothDevice>? {
        return bluetoothAdapter?.bondedDevices
    }

    fun connectToDevice(
        device: BluetoothDevice,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        thread {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream

                mainHandler.post {
                    onConnectionStatusChanged(true, "${device.name} 연결됨")
                    onSuccess()
                }

                startListening()
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Connection failed: ${e.message}")
                mainHandler.post {
                    onConnectionStatusChanged(false, "연결 실패: ${device.name}")
                    onFailure()
                }
            }
        }
    }

    fun startListening() {
        if (bluetoothSocket?.isConnected != true || inputStream == null) {
            Log.e("BluetoothManager", "Bluetooth not connected or inputStream null")
            mainHandler.post {
                onConnectionStatusChanged(false, "수신 실패")
            }
            return
        }

        thread {
            listenForData()
        }
    }

    private fun listenForData() {
        val buffer = ByteArray(1024)
        val stringBuilder = StringBuilder()

        try {
            while (true) {
                val bytes = inputStream?.read(buffer) ?: break
                val incoming = String(buffer, 0, bytes)
                stringBuilder.append(incoming)

                var index: Int
                while (stringBuilder.indexOf("\n").also { index = it } != -1) {
                    val fullLine = stringBuilder.substring(0, index).trim()
                    stringBuilder.delete(0, index + 1)

                    Log.d("BluetoothManager", "🔄 받은 데이터: $fullLine")

                    val parts = fullLine.split(",")

                    // ✅ 데이터 검증
                    if (parts.size != 5) {
                        Log.w("BluetoothManager", "❌ 잘못된 형식: $fullLine")
                        continue
                    }

                    val lat = parts[0].toDoubleOrNull()
                    val lon = parts[1].toDoubleOrNull()
                    val accX = parts[2].toFloatOrNull()
                    val accY = parts[3].toFloatOrNull()
                    val accZ = parts[4].toFloatOrNull()

                    if (
                        lat == null || lon == null ||
                        accX == null || accY == null || accZ == null
                    ) {
                        Log.w("BluetoothManager", "❌ 숫자 파싱 실패: $fullLine")
                        continue
                    }

                    // ✅ 위도 경도 범위 체크
                    if (lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                        Log.w("BluetoothManager", "❌ 위도/경도 범위 오류: $lat, $lon")
                        continue
                    }

                    // 🔽 여기까지 통과한 데이터만 처리
                    onDataReceived(lat, lon, accX, accY, accZ)
                }
            }
        } catch (e: Exception) {
            Log.e("BluetoothManager", "데이터 수신 오류: ${e.message}")
            mainHandler.post {
                onConnectionStatusChanged(false, "데이터 수신 중 오류 발생")
            }
        }
    }

    fun disconnect() {
        try {
            inputStream?.close()
            bluetoothSocket?.close()
            mainHandler.post {
                onConnectionStatusChanged(false, "연결 해제됨")
            }
        } catch (e: Exception) {
            Log.e("BluetoothManager", "연결 종료 중 오류: ${e.message}")
        }
    }
}