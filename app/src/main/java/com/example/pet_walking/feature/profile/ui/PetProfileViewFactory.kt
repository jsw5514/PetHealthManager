package com.example.pet_walking.feature.profile.ui

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.data.PetProfile
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.util.ImageStorageManager

object PetProfileViewFactory {
    fun create(context: Context, profile: PetProfile): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setTag(R.id.profile_uuid, profile.id)
        }

        val profileRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val petImage = ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(200, 200)
            if (!profile.imageUri.isNullOrEmpty()) {
                val bitmap = ImageStorageManager.decodeUriToBitmap(context, Uri.parse(profile.imageUri))
                bitmap?.let { setImageBitmap(it) } ?: setImageResource(R.drawable.ic_profile_placeholder)
            } else {
                setImageResource(R.drawable.ic_profile_placeholder)
            }
        }

        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 0, 0, 0)
        }

        textColumn.addView(TextView(context).apply {
            text = "이름: ${profile.name}    나이: ${profile.age}"
            textSize = 16f
        })

        textColumn.addView(TextView(context).apply {
            text = "성별: ${profile.gender}  몸무게: ${profile.weight} kg"
            textSize = 16f
        })

        textColumn.addView(TextView(context).apply {
            text = "📊 거리: ${profile.totalDistance}km, 칼로리: ${profile.totalCalories}kcal"
            textSize = 14f
        })

        val checkBox = CheckBox(context).apply {
            tag = "deleteCheckBox"
            text = "삭제 선택"
        }

        profileRow.setOnClickListener {
            PetRepository.setCurrentPet(profile.id)
            Toast.makeText(context, "${profile.name} 프로필 선택됨", Toast.LENGTH_SHORT).show()
        }

        profileRow.addView(petImage)
        profileRow.addView(textColumn)
        container.addView(profileRow)
        container.addView(checkBox)

        return container
    }
}