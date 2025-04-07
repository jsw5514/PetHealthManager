#include <TinyGPS++.h>
#include <SoftwareSerial.h>

//TinyGPSPlus 객체 생성
TinyGPSPlus gps;

// 소프트웨어 시리얼 포트 생성 (RX: 4, TX: 5)
SoftwareSerial GPSSerial(4, 5);

// GPS 데이터 처리 상태 추적용 변수
long validDataCount = 0; //유효한 데이터
long totalReceivedCount = 0; //수신한 총 데이터

void setup() {
    Serial.begin(9600);
    GPSSerial.begin(9600);
    Serial.println("GPS 데이터 모니터링 시작");
}

void loop() {
    while (mySerial.available() > 0) {
        char c = GPSSerial.read();
        gps.encode(c); //TinyGPSPlus 객체에 GPS 모듈 출력 전달
        totalReceivedCount++; //수신받은 총 데이터 수 카운트
    }

    // 유효한 위치 데이터가 업데이트되었는지 확인
    if (gps.location.isUpdated()) {
        validDataCount++;//유효한 데이터 수 카운트
        Serial.print("Latitude: ");
        Serial.println(gps.location.lat(), 6); //위도 소수점 6자리로 출력
        Serial.print("Longitude: ");
        Serial.println(gps.location.lng(), 6); //경도 소수점 6자리로 출력
    }

    // 수신 상태 출력
    if (totalReceivedCount % 100 == 0) { // 100개의 문자를 처리할 때마다 상태 확인
        Serial.print("Total Characters Received: ");
        Serial.println(totalReceivedCount);
        Serial.print("valid Data Count: ");
        Serial.println(validDataCount);
    }
}