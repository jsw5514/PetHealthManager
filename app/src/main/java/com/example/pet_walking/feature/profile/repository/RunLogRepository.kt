package com.example.pet_walking.feature.profile.repository

import com.example.pet_walking.feature.Running.model.RunStats

object RunLogRepository {
    private val logs = mutableListOf<RunStats>()

    /** 러닝 종료 시 호출해서 로컬에 기록 */
    fun addLog(stats: RunStats) {
        logs += stats
        // TODO: SharedPreferences나 Room 등을 이용해 영속화하면 앱 재시작 후에도 남습니다.
    }

    /** 저장된 모든 러닝 로그를 반환 */
    fun getAllLogs(): List<RunStats> = logs.toList()
}