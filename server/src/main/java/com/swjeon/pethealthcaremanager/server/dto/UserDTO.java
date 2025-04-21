package com.swjeon.pethealthcaremanager.server.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UserDTO {
    @NonNull
    private String id;
    @NonNull
    private String password;
}
