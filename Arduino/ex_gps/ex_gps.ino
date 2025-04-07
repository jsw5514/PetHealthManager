#include <SoftwareSerial.h>
//for VK2828U7G5LF

// 소프트웨어 시리얼 객체 생성 (TX: 10, RX: 11)
SoftwareSerial mySerial(4, 5);

void setup() {
    // 하드웨어 시리얼 포트를 초기화하고 보율을 설정합니다
    Serial.begin(9600);
    // 소프트웨어 시리얼 포트를 초기화하고 보율을 설정합니다
    mySerial.begin(9600);
}

void loop() {
    // 소프트웨어 시리얼 포트에서 데이터를 읽습니다
    if (mySerial.available() > 0) {
        // 읽은 데이터를 하드웨어 시리얼 포트로 출력합니다
        while (mySerial.available() > 0) {
            char data = mySerial.read();
            Serial.print(data);
        }
        //Serial.println();
    }
}
