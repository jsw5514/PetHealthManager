package com.example.pet_walking

object GoalRepository {
    var weeklyDistanceGoal: Double? = null
    var weeklyCalorieGoal: Double? = null

    fun isGoalReached(currentDistance: Double, currentCalories: Double): Boolean {
        val distanceReached = weeklyDistanceGoal?.let { currentDistance >= it } ?: false
        val calorieReached = weeklyCalorieGoal?.let { currentCalories >= it } ?: false
        return distanceReached || calorieReached
    }

    fun getGoalSummary(): String {
        return when {
            weeklyDistanceGoal != null -> "목표 거리: %.2f km".format(weeklyDistanceGoal)
            weeklyCalorieGoal != null -> "목표 칼로리: %.2f kcal".format(weeklyCalorieGoal)
            else -> "설정된 목표가 없습니다."
        }
    }
}