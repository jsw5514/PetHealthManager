package com.example.pet_walking.feature.profile.ui

import android.net.Uri
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.setPadding
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.data.PetProfile
import com.example.pet_walking.util.ImageStorageManager
import java.util.*

class UserInputHelper(
    rootView: View,
    private val imagePicker: ActivityResultLauncher<String>
) {
    private val nameInput: EditText = rootView.findViewById(R.id.nameInput)
    private val ageInput: EditText = rootView.findViewById(R.id.ageInput)
    private val genderGroup: RadioGroup = rootView.findViewById(R.id.genderGroup)
    private val weightInput: EditText = rootView.findViewById(R.id.weightInput)
    val saveButton: Button = rootView.findViewById(R.id.saveButton)

    private val selectImageView: ImageView
    private var selectedImageUri: Uri? = null

    init {
        selectImageView = ImageView(rootView.context).apply {
            layoutParams = LinearLayout.LayoutParams(300, 300)
            setImageResource(R.drawable.ic_profile_placeholder)
            setPadding(8)
            setOnClickListener {
                imagePicker.launch("image/*")
            }
        }
        val inputForm = rootView.findViewById<LinearLayout>(R.id.inputForm)
        inputForm.addView(selectImageView, 0)
    }

    fun setImageUri(uri: Uri) {
        selectedImageUri = uri
        selectImageView.setImageURI(uri)
    }

    fun buildPetProfile(): PetProfile? {
        val name = nameInput.text.toString()
        val age = ageInput.text.toString()
        val gender = when (genderGroup.checkedRadioButtonId) {
            R.id.male -> "수컷"
            R.id.female -> "암컷"
            else -> "미정"
        }
        val weight = weightInput.text.toString().toDoubleOrNull() ?: return null
        val uuid = UUID.randomUUID()

        val context = selectImageView.context
        var savedUri: Uri? = null

        selectedImageUri?.let { uri ->
            val bitmap = ImageStorageManager.decodeUriToBitmap(context, uri)
            bitmap?.let {
                savedUri = ImageStorageManager.saveBitmapToInternalStorage(context, it, "pet_$uuid")
            }
        }

        return PetProfile(
            id = uuid,
            name = name,
            age = age,
            gender = gender,
            weight = weight,
            imageUri = savedUri?.toString(),
            totalDistance = 0.0,
            totalCalories = 0.0
        )
    }

    fun clear() {
        nameInput.text.clear()
        ageInput.text.clear()
        weightInput.text.clear()
        genderGroup.clearCheck()
        selectedImageUri = null
        selectImageView.setImageResource(R.drawable.ic_profile_placeholder)
    }
}