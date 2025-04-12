CREATE TABLE USER(
ID VARCHAR(20) NOT NULL PRIMARY KEY,
PW VARCHAR(20) NOT NULL,
NICKNAME VARCHAR(20)
);
CREATE TABLE CHAT(
ROOM_ID INT NOT NULL,
WRITER_ID VARCHAR(20) NOT NULL,
WRITE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
CONTENT JSON NULL,
PRIMARY KEY(ROOM_ID,WRITER_ID,WRITE_TIME)
);