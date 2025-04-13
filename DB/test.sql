-- 테스트 데이터 삽입용
INSERT INTO USERS VALUES("testid","testpw","testnickname"),("user1","user1","user1"),("user2","user2","user2"),("user3","user3","user3"),("user4","user4","user4");
INSERT INTO CHAT_ROOM VALUES();
INSERT INTO CHAT_ROOM VALUES();
INSERT INTO CHAT_MEMBER VALUES(1,"testid");
INSERT INTO CHAT (ROOM_ID, WRITER_ID, CONTENT) 
VALUES 
(1, 'user1', '{"text": "Hello, world!"}'),
(1, 'user2', '{"text": "How are you?"}'),
(2, 'user3', '{"text": "Welcome to room 2"}'),
(2, 'user4', '{"text": "Hi there!"}');

-- 모든 데이터 삭제
DROP TABLE CHAT_MEMBER;
DROP TABLE CHAT;
DROP TABLE CHAT_ROOM;
DROP TABLE USERS;