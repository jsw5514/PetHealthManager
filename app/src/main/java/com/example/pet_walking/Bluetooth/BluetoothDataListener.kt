package com.example.pet_walking.Bluetooth

//MainActivity가 수신한 블루투스 데이터 실시간으로 다른 프래그먼트에 넘져주는 콜백 인터페이스
interface BluetoothDataListener {
    fun onBluetoothDataReceived(lat: Double, lon: Double, accX: Float, accY: Float, accZ: Float)
}