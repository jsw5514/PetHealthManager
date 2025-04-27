package com.swjeon.pethealthcaremanager.server.Entity.IdClass;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

//JPA에서의 복합 키 사용을 위한 클래스
@Getter
@Setter
public class ChatMemberIdClass implements Serializable {
    private int roomId;
    private String memberId;

    public ChatMemberIdClass(){}
    public ChatMemberIdClass(int roomId, String memberId) {
        this.roomId = roomId;
        this.memberId = memberId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChatMemberIdClass that = (ChatMemberIdClass) o;
        return roomId == that.roomId && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, memberId);
    }
}
