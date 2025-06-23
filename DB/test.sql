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
INSERT INTO PET VALUES ("testPetUUID","testid","1","1","암컷",1,"",0,0);
INSERT INTO PET VALUES ("testPetUUID2","testid","2","1","암컷",1,"",0,0);
INSERT INTO DATA (UPLOADER_ID, DATA_ID, DATA_TYPE, CONTENT) VALUES ("testid", "testPetUUID", "just test data", '{ "content":"this is json content"}');
   INSERT INTO DATA (UPLOADER_ID, DATA_ID, DATA_TYPE, CONTENT) VALUES ("testid", "testPetUUID", "running_stats", '{
    "summary": {
      "totalDistance": 7.3,
      "totalCalories": 320
    },
    "daily": {
      "2025-06-08": {
        "distance": 3,
        "calories": 100
      },
      "2025-06-09": {
        "distance": 4.3,
        "calories": 220
      }
    },
    "weekly": {
      "2025-W24": {
        "distance": 7.3,
        "calories": 320
      }
    },
    "monthly": {
      "2025-06": {
        "distance": 7.3,
        "calories": 320
      }
    },
    "yearly": {
      "2025": {
        "distance": 1,
        "calories": 1
      }
    }
  }
');
   INSERT INTO DATA (UPLOADER_ID, DATA_ID, DATA_TYPE, CONTENT) VALUES ("testid", "testPetUUID2", "running_stats", '{
    "summary": {
      "totalDistance": 7.3,
      "totalCalories": 320
    },
    "daily": {
      "2025-06-08": {
        "distance": 3,
        "calories": 100
      },
      "2025-06-09": {
        "distance": 4.3,
        "calories": 220
      }
    },
    "weekly": {
      "2025-W24": {
        "distance": 7.3,
        "calories": 320
      }
    },
    "monthly": {
      "2025-06": {
        "distance": 7.3,
        "calories": 320
      }
    },
    "yearly": {
      "2025": {
        "distance": 1,
        "calories": 1
      }
    }
  }
');

-- 데이터 출력용
SELECT * FROM USERS;
SELECT * FROM PET;
SELECT * FROM CHAT_ROOM;
SELECT * FROM CHAT_MEMBER;
SELECT * FROM CHAT;
SELECT * FROM DATA;

-- 모든 데이터 삭제
DROP TABLE CHAT_MEMBER;
DROP TABLE CHAT;
DROP TABLE CHAT_ROOM;
DROP TABLE DATA;
DROP TABLE PET;
DROP TABLE USERS;