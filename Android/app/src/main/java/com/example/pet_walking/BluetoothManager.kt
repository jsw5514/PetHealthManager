package com.example.pet_walking.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.concurrent.thread

class BluetoothManager(
    private val onDataReceived: (Double, Double, Float, Float, Float) -> Unit,
    private val onConnectionStatusChanged: (Boolean, String) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null

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
                // HC-06 UUID (Serial Port Profile)
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()

                onConnectionStatusChanged(true, "${device.name} 연결됨")
                startListening() // 연결되면 수신 시작
                onSuccess()
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Connection failed: ${e.message}")
                onConnectionStatusChanged(false, "연결 실패: ${device.name}")
                onFailure()
            }
        }
    }

    fun startListening() {
        val socket = bluetoothSocket ?: return
        val reader = BufferedReader(InputStreamReader(socket.inputStream))

        thread {
            try {
                while (true) {
                    val line = reader.readLine() ?: break
                    Log.d("BluetoothManager", "Received: $line")

                    val parts = line.trim().split(",")
                    if (parts.size == 5) {
                        val lat = parts[0].toDoubleOrNull()
                        val lon = parts[1].toDoubleOrNull()
                        val accX = parts[2].toFloatOrNull()
                        val accY = parts[3].toFloatOrNull()
                        val accZ = parts[4].toFloatOrNull()

                        if (lat != null && lon != null && accX != null && accY != null && accZ != null) {
                            onDataReceived(lat, lon, accX, accY, accZ)
                        }
                    } else {
                        Log.w("BluetoothManager", "Invalid data format: $line")
                    }
                }
            } catch (e: Exception) {
                Log.e("BluetoothManager", "수신 중 오류: ${e.message}")
                onConnectionStatusChanged(false, "데이터 수신 오류")
            }
        }
    }

    fun disconnect() {
        try {
            bluetoothSocket?.close()
            onConnectionStatusChanged(false, "연결 해제됨")
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Disconnect error: ${e.message}")
        }
    }
}

