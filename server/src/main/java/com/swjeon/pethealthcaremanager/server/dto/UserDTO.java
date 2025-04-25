package com.swjeon.pethealthcaremanager.server.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/*UserDTO 구조
 * 업로드 시에만 필요(NonNull,JsonIgnore 적용)
 * 없음
 * -----------------------------
 * 다운로드 시에만 필요(어노테이션 미적용)
 * 없음
 * -----------------------------
 * 업로드/다운로드 시 모두 필요(NonNull 적용)
 * String id = 유저 id
 * String password = 비밀번호
 */
@Getter
@Setter
@RequiredArgsConstructor
public class UserDTO {
    @NonNull
    private String id;
    @NonNull
    private String password;
}
