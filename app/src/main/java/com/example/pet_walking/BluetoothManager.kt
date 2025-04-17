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
    private val mainHandler = Handler(Looper.getMainLooper()) // ✅ UI 스레드용 핸들러

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

                // 🔄 상태 콜백 UI 스레드에서 실행
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

                    Log.d("BluetoothManager", "Received line: $fullLine")
                    val parts = fullLine.split(",")
                    if (parts.size == 5) {
                        val lat = parts[0].toDoubleOrNull()
                        val lon = parts[1].toDoubleOrNull()
                        val accX = parts[2].toFloatOrNull()
                        val accY = parts[3].toFloatOrNull()
                        val accZ = parts[4].toFloatOrNull()

                        if (lat != null && lon != null && accX != null && accY != null && accZ != null) {
                            // 💡 반드시 UI 스레드에서 실행할 필요는 없지만, 필요하면 mainHandler.post로 래핑 가능
                            onDataReceived(lat, lon, accX, accY, accZ)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Error while reading data: ${e.message}")
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
            Log.e("BluetoothManager", "Error while closing connection: ${e.message}")
            e.printStackTrace()
        }
    }
}