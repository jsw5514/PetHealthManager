package com.example.pet_walking.Bluetooth
/**
 * 블루투스 장치에서 수신한 1개의 데이터 세트를 담는 데이터 클래스
 *
 * - lat: 위도 (GPS)
 * - lon: 경도 (GPS)
 * - accX, accY, accZ: X/Y/Z 축 가속도 값
 *
 * BluetoothManager에서 문자열 데이터를 파싱한 결과로 사용되고
 * 거리 계산, 칼로리 계산, 지도 이동 경로 표시 등에 활용
 */
data class ParsedData(
    val lat: Double,
    val lon: Double,
    val accX: Float,
    val accY: Float,
    val accZ: Float
)