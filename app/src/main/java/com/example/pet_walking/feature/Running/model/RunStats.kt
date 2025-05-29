package com.example.pet_walking.feature.Running.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RunStats(
    val distance: Double,
    val calories: Double,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable