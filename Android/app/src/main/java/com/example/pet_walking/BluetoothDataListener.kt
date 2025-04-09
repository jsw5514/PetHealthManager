package com.example.pet_walking.bluetooth

interface BluetoothDataListener {
    fun onBluetoothDataReceived(lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float)
}