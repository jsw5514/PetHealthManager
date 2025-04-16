-- 테스트 데이터 삽입용
INSERT INTO USERS VALUES("testid","testpw","testnickname"),("user1","user1","user1"),("user2","user2","user2"),("user3","user3","user3"),("user4","user4","user4"),("user5","user5","user5");
INSERT INTO CHAT_ROOM VALUES(); INSERT INTO CHAT_ROOM VALUES(); INSERT INTO CHAT_ROOM VALUES();
INSERT INTO CHAT_MEMBER VALUES(1,"testid");
INSERT INTO CHAT (ROOM_ID, WRITER_ID, CONTENT_TYPE, CONTENT_PATH)
VALUES
(1, 'user1', 'text', '/texts/hello_world.txt'),
(1, 'user2', 'image', '/images/example1.png'),
(2, 'user3', 'video', '/videos/example2.mp4'),
(2, 'user4', 'text', '/texts/test_message.txt'),
(3, 'user5', 'audio', '/audios/example3.mp3');

-- 데이터 출력용
SELECT * FROM USERS;
SELECT * FROM CHAT_ROOM;
SELECT * FROM CHAT_MEMBER;
SELECT * FROM CHAT;

-- 모든 데이터 삭제
DROP TABLE CHAT_MEMBER;
DROP TABLE CHAT;
DROP TABLE CHAT_ROOM;
DROP TABLE DATA;
DROP TABLE USERS;