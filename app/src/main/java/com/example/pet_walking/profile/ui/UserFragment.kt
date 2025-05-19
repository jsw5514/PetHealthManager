package com.example.pet_walking.profile.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.pet_walking.R
import com.example.pet_walking.profile.repository.PetRepository
import com.example.pet_walking.profile.repository.UserRepository
import java.util.*

class UserFragment : Fragment() {

    private lateinit var inputForm: View
    private lateinit var createButton: Button
    private lateinit var deleteButton: Button
    private lateinit var petContainer: LinearLayout
    private lateinit var userName: TextView
    private lateinit var userImage: ImageView

    private lateinit var inputHelper: UserInputHelper

    private var selectedPetImageUri: Uri? = null

    private val imagePickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedPetImageUri = it
            inputHelper.setImageUri(it)
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
        createButton = view.findViewById(R.id.createButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        petContainer = view.findViewById(R.id.petContainer)
        inputForm.visibility = View.GONE

        // Input Helper 초기화
        inputHelper =
            UserInputHelper(view, imagePickLauncher)

        createButton.setOnClickListener {
            inputForm.visibility = View.VISIBLE
        }

        inputHelper.saveButton.setOnClickListener {
            val user = UserRepository.getCurrentUser()
            if (user == null) {
                Toast.makeText(requireContext(), "로그인 후 이용해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profile = inputHelper.buildPetProfile() ?: return@setOnClickListener
            val userId = user.userId

            PetRepository.addProfile(profile, userId) { success ->
                requireActivity().runOnUiThread {
                    if (!success) {
                        Toast.makeText(requireContext(), "서버 업로드 실패 (로컬 저장됨)", Toast.LENGTH_SHORT).show()
                    }

                    UserRepository.addPetToCurrentUser(profile.id)
                    UserRepository.saveToPreferences(requireContext())
                    petContainer.addView(PetProfileViewFactory.create(requireContext(), profile))
                    inputForm.visibility = View.GONE
                    inputHelper.clear()
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
                petContainer.addView(PetProfileViewFactory.create(requireContext(), it))
            }
        }

        return view
    }
}