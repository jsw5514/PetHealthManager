-- 테스트 데이터 삽입용
INSERT INTO USERS VALUES("testid","testpw","testnickname"),("user1","user1","user1"),("user2","user2","user2"),("user3","user3","user3"),("user4","user4","user4"),("user5","user5","user5");
INSERT INTO CHAT_ROOM VALUES(); INSERT INTO CHAT_ROOM VALUES(); INSERT INTO CHAT_ROOM VALUES();
INSERT INTO CHAT_MEMBER VALUES(1,"testid");
INSERT INTO CHAT (ROOM_ID, WRITER_ID, CONTENT_TYPE, CONTENT)
VALUES
(1, 'user1', 'text', 'this is test message'),
(1, 'user2', 'image', '/images/example1.png'),
(2, 'user3', 'video', '/videos/example2.mp4'),
(2, 'user4', 'text', 'this is test message 2'),
(3, 'user5', 'audio', '/audios/example3.mp3');
INSERT INTO DATA (UPLOADER_ID, DATA_ID, META_DATA, CONTENT) VALUES ("testid", "testDataId", "just test data", '{ "content":"this is json content"}');
INSERT INTO DATA (UPLOADER_ID, DATA_ID, META_DATA, CONTENT) VALUES ("testid", "testPetUUID", "pet_profile", ' {
    "name": "1",
    "age": "1",
    "gender": "암컷",
    "weight": 1,
    "imageUri": "",
    "totalDistance": 0,
    "totalCalories": 0
  }');
  INSERT INTO DATA (UPLOADER_ID, DATA_ID, META_DATA, CONTENT) VALUES ("testid", "testPetUUID2", "pet_profile", ' {
    "name": "2",
    "age": "1",
    "gender": "암컷",
    "weight": 1,
    "imageUri": "",
    "totalDistance": 0,
    "totalCalories": 0
  }');

-- 데이터 출력용
SELECT * FROM USERS;
SELECT * FROM CHAT_ROOM;
SELECT * FROM CHAT_MEMBER;
SELECT * FROM CHAT;
SELECT * FROM DATA;

-- 모든 데이터 삭제
DROP TABLE CHAT_MEMBER;
DROP TABLE CHAT;
DROP TABLE CHAT_ROOM;
DROP TABLE DATA;
DROP TABLE USERS;