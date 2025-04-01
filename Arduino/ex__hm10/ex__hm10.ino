#include <SoftwareSerial.h>
SoftwareSerial BTSeril(2, 3); // RX, TX

void setup() {
  Serial.begin(9600);
  BTSerial.begin(9600);
  Serial.println("HM-10 테스트 시작!");
  delay(1000);
  BTSerial.write("AT+RESET");
}

void loop() {
  while (BTSerial.available()) {
    Serial.write(BTSerial.read());
  }
  while (Serial.available()) {
    BTSerial.write(Serial.read());
  }
}