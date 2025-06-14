# PetHealthManager

### 서버 설정시 주의사항
application.properties에 db계정을 수동으로 추가해야 합니다.
ex)
spring.datasource.username= your_user_name
spring.datasource.password=your_pass_word

### 서버 todo list
1. 채팅 구현
-미구현 함수 목록
ChatController.java (다수의 미구현 함수가 @RequestParam을 사용하고 있음. @RequestBody로 변경 필요)
downLoadChat -> 현재 정상작동하지 않음, 수정 필요
createChatRoom
inviteChatMember
leaveChatRoom
getChatMember

2. 데이터 저장 구조 수정, 데이터 시각화
meda data를 기준으로 데이터를 구분하여 서로 다른 테이블에 저장하도록 db구조 및 코드 수정
+구분된 데이터를 읽어서 시각화하는 코드 추가(Chart.js, matplotlib 등을 적절히 사용)

3. user service 개선
보안: 비밀번호 db 저장시 해시함수 적용

4. 그외
요청 처리에 실패했을 경우 null을 반환하면 클라이언트가 제대로 처리하지 못할 확률이 높음
->null 대신 명백한 responseEntity로 구현(테스팅 후 결정)