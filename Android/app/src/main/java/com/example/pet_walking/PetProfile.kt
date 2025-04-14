package com.example.pet_walking

import android.net.Uri
import java.util.UUID

data class PetProfile(
    val id: UUID,
    val name: String,
    val age: String,
    val gender: String,
    val weight: Double,
    val imageUri: String?,
    var totalDistance: Double =0.0,
    var totalCalories: Double = 0.0
)