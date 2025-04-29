package com.example.pet_walking

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.pet_walking.util.ImageStorageManager
import java.util.*

class UserFragment : Fragment() {

    private lateinit var inputForm: View
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var weightInput: EditText
    private lateinit var saveButton: Button
    private lateinit var createButton: Button
    private lateinit var deleteButton: Button
    private lateinit var petContainer: LinearLayout
    private lateinit var userName: TextView
    private lateinit var userImage: ImageView
    private lateinit var selectImageView: ImageView

    private var selectedPetImageUri: Uri? = null

    private val imagePickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedPetImageUri = it
            selectImageView.
            setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.user_fragment, container, false)

        userName = view.findViewById(R.id.userName)
        userImage = view.findViewById(R.id.userImage)

        val user = UserRepository.getCurrentUser()
        userName.text = if (user != null) "👤 ${user.username} (${user.userId})" else "로그인 정보 없음"

        inputForm = view.findViewById(R.id.inputForm)
        nameInput = view.findViewById(R.id.nameInput)
        ageInput = view.findViewById(R.id.ageInput)
        genderGroup = view.findViewById(R.id.genderGroup)
        weightInput = view.findViewById(R.id.weightInput)
        saveButton = view.findViewById(R.id.saveButton)

        selectImageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(300, 300)
            setImageResource(R.drawable.ic_profile_placeholder)
            setPadding(8)
            setOnClickListener {
                imagePickLauncher.launch("image/*")
            }
        }
        (inputForm as LinearLayout).addView(selectImageView, 0)

        createButton = view.findViewById(R.id.createButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        petContainer = view.findViewById(R.id.petContainer)
        inputForm.visibility = View.GONE

        createButton.setOnClickListener {
            inputForm.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val age = ageInput.text.toString()
            val gender = when (genderGroup.checkedRadioButtonId) {
                R.id.male -> "수컷"
                R.id.female -> "암컷"
                else -> "미정"
            }
            val weight = weightInput.text.toString().toDoubleOrNull() ?: 10.0
            val uuid = UUID.randomUUID()
            val userId = UserRepository.getCurrentUser()?.userId ?: return@setOnClickListener

            var savedUri: Uri? = null
            selectedPetImageUri?.let { uri ->
                val bitmap = ImageStorageManager.decodeUriToBitmap(requireContext(), uri)
                bitmap?.let {
                    savedUri = ImageStorageManager.saveBitmapToInternalStorage(requireContext(), it, "pet_$uuid")
                }
            }

            val profile = PetProfile(
                id = uuid,
                name = name,
                age = age,
                gender = gender,
                weight = weight,
                imageUri = savedUri?.toString(),
                totalDistance = 0.0,
                totalCalories = 0.0
            )

            Log.d("UserFragment", "✅ 저장할 프로필: $profile")

            PetRepository.addProfile(profile, userId) { success ->
                requireActivity().runOnUiThread {
                    if (success) {
                        UserRepository.addPetToCurrentUser(profile.id)
                        UserRepository.saveToPreferences(requireContext())
                        petContainer.addView(createPetProfileView(profile))
                        inputForm.visibility = View.GONE
                        clearInputs()
                        Toast.makeText(requireContext(), "프로필 저장 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "프로필 저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        deleteButton.setOnClickListener {
            val toDelete = mutableListOf<UUID>()
            for (i in 0 until petContainer.childCount) {
                val view = petContainer.getChildAt(i)
                val checkBox = view.findViewWithTag<CheckBox>("deleteCheckBox")
                val uuid = view.getTag(R.id.profile_uuid) as? UUID
                if (checkBox?.isChecked == true && uuid != null) {
                    toDelete.add(uuid)
                }
            }

            val currentUser = UserRepository.getCurrentUser()
            toDelete.forEach { uuid ->
                for (i in 0 until petContainer.childCount) {
                    val view = petContainer.getChildAt(i)
                    val id = view.getTag(R.id.profile_uuid) as? UUID
                    if (id == uuid) {
                        petContainer.removeView(view)
                        break
                    }
                }
                PetRepository.removeProfile(uuid)
                currentUser?.petIds?.remove(uuid)
            }

            UserRepository.saveToPreferences(requireContext())
        }

        user?.petIds?.forEach { id ->
            PetRepository.getProfile(id)?.let {
                petContainer.addView(createPetProfileView(it))
            }
        }

        return view
    }

    private fun createPetProfileView(profile: PetProfile): View {
        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16)
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

        val line1 = TextView(context).apply {
            text = "이름: ${profile.name}    나이: ${profile.age}"
            textSize = 16f
        }

        val line2 = TextView(context).apply {
            text = "성별: ${profile.gender}  몸무게: ${profile.weight} kg"
            textSize = 16f
        }

        val stats = TextView(context).apply {
            text = "📊 거리: ${profile.totalDistance}km, 칼로리: ${profile.totalCalories}kcal"
            textSize = 14f
        }

        val checkBox = CheckBox(context).apply {
            tag = "deleteCheckBox"
            text = "삭제 선택"
        }

        profileRow.setOnClickListener {
            PetRepository.setCurrentPet(profile.id)
            Toast.makeText(context, "${profile.name} 프로필 선택됨", Toast.LENGTH_SHORT).show()
        }

        textColumn.addView(line1)
        textColumn.addView(line2)
        textColumn.addView(stats)
        profileRow.addView(petImage)
        profileRow.addView(textColumn)
        container.addView(profileRow)
        container.addView(checkBox)

        return container
    }

    private fun clearInputs() {
        nameInput.text.clear()
        ageInput.text.clear()
        weightInput.text.clear()
        genderGroup.clearCheck()
        selectedPetImageUri = null
        selectImageView.setImageResource(R.drawable.ic_profile_placeholder)
    }
}