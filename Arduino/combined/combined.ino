#include <SoftwareSerial.h> //시리얼 통신을 위해 사용(사용 모듈: HM-10 블루투스 모듈)
#include <Wire.h> //I2C 통신을 위해 사용(사용 모듈: ADXL345 가속도 모듈)
SoftwareSerial BTSerial(2, 3); // RX, TX 블루투스 모듈 연결용 시리얼 객체

#define I2C_Address 0x53 //ADXL345의 I2C 주소

//ADXL345의 레지스터 주소
#define POWER_CTL 0x2D
#define DATA_FORMAT 0x31
#define X_axis 0x32
#define Y_axis 0x34
#define Z_axis 0x36

//ADXL345의 DATA_FORMAT 레지스터값
/*note: 해당 레지스터는 여러 필드를 지원하지만 이 코드에서는 측정할 가속도 범위만 설정*/
#define Range_2g 0
#define Range_4g 1
#define Range_8g 2
#define Range_16g 3

void setup() {
  //출력용 시리얼 준비
  Serial.begin(9600);

  //블루투스 모듈 준비
  BTSerial.begin(9600);
  delay(1000);
  BTSerial.write("AT+RESET");

  //가속도 모듈 준비
  Wire.begin();
  Init_ADXL345(Range_2g);
}

//ADXL345 초기화
void Init_ADXL345(byte r) {

  Wire.beginTransmission(I2C_Address);

  //감도설정
  Wire.write(DATA_FORMAT);
  Wire.write(r);
  Wire.endTransmission();

  //측정모드로 전환
  Wire.beginTransmission(I2C_Address);
  Wire.write(POWER_CTL);
  Wire.write(0x08);
  Wire.endTransmission();
}

void loop() {
  while (BTSerial.available()) {
    Serial.write(BTSerial.read());
  }
  while (Serial.available()) {
    BTSerial.write(Serial.read());
  }
  //printAcc();

}

//3축 가속도 출력 함수
void printAcc(){
  Serial.println("가속도 모듈 출력");
  Serial.print("X: ");
  Serial.print(Read_Axis(X_axis));
  Serial.print("  Y: ");
  Serial.print(Read_Axis(Y_axis));
  Serial.print("  Z: ");
  Serial.print(Read_Axis(Z_axis));
  Serial.println();
}

//가속도값 읽기 함수
int Read_Axis(byte a) {
  int data;

  Wire.beginTransmission(I2C_Address);
  Wire.write(a);
  Wire.endTransmission();

  Wire.beginTransmission(I2C_Address);
  Wire.requestFrom(I2C_Address, 2);

  if (Wire.available()) {
    data = (int)Wire.read();
    data = data | (Wire.read() << 8);
  }
  else {
    data = 0;
  }

  Wire.endTransmission();
  return data;
}
