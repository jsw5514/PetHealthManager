#include <SoftwareSerial.h> //시리얼 통신을 위해 사용(사용 모듈: HM-10 블루투스 모듈,VK2828U7G5LF GPS 모듈)
#include <Wire.h> //I2C 통신을 위해 사용(사용 모듈: ADXL345 가속도 모듈)
#include <TinyGPS++.h> //GPS 모듈이 전송하는 NMEA 0183 데이터를 파싱하기위해 사용

SoftwareSerial BTSerial(2, 3); //(RX, TX) 블루투스 모듈 연결용 시리얼 객체
SoftwareSerial GPSSerial(4, 5); //(RX, TX) GPS 모듈 연결용 시리얼 객체
TinyGPSPlus gps; //gps 데이터 파싱을 위한 TinyGPS++객체(선언시에 객체 자동생성)
char gps_buf_latitude[16];//위도 데이터 저장용 버퍼
char gps_buf_longitude[16];//경도 데이터 저장용 버퍼

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

//출력 간격 관리
#define OUTPUT_INTERVAL 1000 
unsigned long time_before=0;
//가속도, gps 데이터 출력 함수
//매개변수
//serial : 출력할 시리얼 객체
void printData(Stream &serial){
  unsigned long now = millis(); //현재 시간 가져오기
  if(now >= time_before + OUTPUT_INTERVAL){
    time_before = now;
    //TODO 출력형식 변경
    serial.print("----------\n"); //데이터 구분선 출력
    printAcc(serial);//가속도 출력
    printGPS(serial);//gps 출력
  }
}

//3축 가속도 출력 함수
//매개변수
//serial : 출력할 시리얼 객체
void printAcc(Stream &serial){
  serial.write("가속도 모듈 출력\n");
  serial.write("X: ");
  serial.print(Read_Axis(X_axis));
  serial.write("  Y: ");
  serial.print(Read_Axis(Y_axis));
  serial.write("  Z: ");
  serial.print(Read_Axis(Z_axis));
  serial.write("\n");
}

//gps 출력 함수
//매개변수
//serial : 출력할 시리얼 객체
void printGPS(Stream &serial){
  serial.write("gps 모듈 출력\n");
  serial.write("위도: ");
  serial.write(gps_buf_latitude);
  serial.write("  경도: ");
  serial.write(gps_buf_longitude);
  serial.write("\n");
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

void setup() {
  //디버그 출력용 시리얼 준비
  Serial.begin(9600);

  //블루투스 모듈 준비
  BTSerial.begin(9600);
  delay(1000);
  BTSerial.write("AT+RESET");

  //가속도 모듈 준비
  Wire.begin();
  Init_ADXL345(Range_2g);

  //gps 모듈 준비
  GPSSerial.begin(9600);
}

void loop() {
  //시리얼 입력을 블루투스 모듈에 그대로 전달(AT 명령어 송신용)
  while (BTSerial.available()) {
    Serial.write(BTSerial.read());
  }

  //모듈에서 받은 GPS데이터를 TinyGPSPlus 객체에 전달
  while (GPSSerial.available() > 0) {
    char c = GPSSerial.read();
    gps.encode(c);
  }

  // 유효한 위치 데이터가 업데이트되었는지 확인
  if (gps.location.isUpdated()) {
    //그대로 출력하는 대신 버퍼에 저장
    dtostrf(gps.location.lat(), 0, 6, gps_buf_latitude);//(number, width, precision, buffer)
    dtostrf(gps.location.lng(), 0, 6, gps_buf_longitude);//(number, width, precision, buffer)
    // Serial.print("Latitude: ");
    // Serial.println(gps.location.lat(), 6); //위도 소수점 6자리로 출력
    // Serial.print("Longitude: ");
    // Serial.println(gps.location.lng(), 6); //경도 소수점 6자리로 출력
  }

  //데이터 출력
  printData(BTSerial);
}
