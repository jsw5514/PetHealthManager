package com.swjeon.pethealthcaremanager.server.Entity.IdClass;

import java.io.Serializable;
import java.util.Objects;

//JPA에서의 복합 키 사용을 위한 클래스
public class ChatMemberIdClass implements Serializable {
    private long roomId;
    private String memberId;

    ChatMemberIdClass(){}
    ChatMemberIdClass(long roomId, String memberId) {
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

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
