package com.example.pet_walking.feature.profile.repository

object GoalRepository {

    private val distanceGoals = mutableMapOf<String, Double>()
    private val calorieGoals = mutableMapOf<String, Double>()

    // userId와 petId를 결합한 유일한 키 생성
    private fun getKey(userId: String, petId: String): String = "$userId:$petId"

    /**
     * 목표 설정 (거리, 칼로리)
     */
    fun setGoal(userId: String, petId: String, distance: Double?, calories: Double?) {
        val key = getKey(userId, petId)
        distance?.let { distanceGoals[key] = it }
        calories?.let { calorieGoals[key] = it }
    }

    /**
     * 거리 목표 조회
     */
    fun getDistanceGoal(userId: String, petId: String): Double? {
        return distanceGoals[getKey(userId, petId)]
    }

    /**
     * 칼로리 목표 조회
     */
    fun getCalorieGoal(userId: String, petId: String): Double? {
        return calorieGoals[getKey(userId, petId)]
    }

    /**
     * 현재 운동량이 목표를 달성했는지 여부 반환
     */
    fun isGoalReached(userId: String, petId: String, currentDistance: Double, currentCalories: Double): Boolean {
        val distanceReached = getDistanceGoal(userId, petId)?.let { currentDistance >= it } ?: false
        val calorieReached = getCalorieGoal(userId, petId)?.let { currentCalories >= it } ?: false
        return distanceReached || calorieReached
    }

    /**
     * UI에 표시할 요약 텍스트 생성
     */
    fun getGoalSummary(userId: String, petId: String): String {
        val distance = getDistanceGoal(userId, petId)
        val calorie = getCalorieGoal(userId, petId)

        return when {
            distance != null -> "목표 거리: %.2f km".format(distance)
            calorie != null -> "목표 칼로리: %.2f kcal".format(calorie)
            else -> "설정된 목표가 없습니다."
        }
    }

    /**
     * 모든 목표 초기화 (디버깅/로그아웃 시 활용 가능)
     */
    fun clearAllGoals() {
        distanceGoals.clear()
        calorieGoals.clear()
    }
}