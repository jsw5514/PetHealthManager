package com.example.pet_walking.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
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
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // HC-06 / HM-10 UUID
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()
                inputStream = bluetoothSocket?.inputStream

                onConnectionStatusChanged(true, "${device.name} 연결됨")
                startListening()
                onSuccess()
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Connection failed: ${e.message}")
                onConnectionStatusChanged(false, "연결 실패: ${device.name}")
                onFailure()
            }
        }
    }

    fun startListening() {
        if (bluetoothSocket?.isConnected != true || inputStream == null) {
            Log.e("BluetoothManager", "Bluetooth not connected or inputStream null")
            onConnectionStatusChanged(false, "수신 실패")
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
                            onDataReceived(lat, lon, accX, accY, accZ)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Error while reading data: ${e.message}")
            onConnectionStatusChanged(false, "데이터 수신 중 오류 발생")
        }
    }

    fun disconnect() {
        try {
            inputStream?.close()
            bluetoothSocket?.close()
            onConnectionStatusChanged(false, "연결 해제됨")
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Error while closing connection: ${e.message}")
            e.printStackTrace()
        }
    }
}
