package com.example.pet_walking

import java.util.*

data class UserProfile(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val birthdate: String,
    val gender: String,
    val userId: String,       // 로그인용 ID
    val password: String,     // 실제 서비스에선 암호화 필요
    val petIds: MutableList<UUID> = mutableListOf()
)