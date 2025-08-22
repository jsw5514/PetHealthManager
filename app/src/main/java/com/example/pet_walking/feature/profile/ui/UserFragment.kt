/*
/**
 * 8월 22알 수정전
 */
package com.example.pet_walking.feature.profile.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.feature.profile.repository.UserRepository
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
}*/
package com.example.pet_walking.feature.profile.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.feature.profile.repository.UserRepository
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

    private val imagePickLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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

        // 입력 도우미
        inputHelper = UserInputHelper(view, imagePickLauncher)

        /* 생성 */
        createButton.setOnClickListener {
            inputForm.visibility = View.VISIBLE
        }

        inputHelper.saveButton.setOnClickListener {
            val currentUser = UserRepository.getCurrentUser()
            if (currentUser == null) {
                Toast.makeText(requireContext(), "로그인 후 이용해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profile = inputHelper.buildPetProfile() ?: return@setOnClickListener
            val userId = currentUser.userId

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

        /* 삭제 */
        deleteButton.setOnClickListener {
            val currentUser = UserRepository.getCurrentUser()
            if (currentUser == null) {
                Toast.makeText(requireContext(), "로그인 후 이용해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 체크된 항목 수집
            val toDelete = mutableListOf<UUID>()
            for (i in 0 until petContainer.childCount) {
                val child = petContainer.getChildAt(i)
                val checkBox = child.findViewWithTag<CheckBox>("deleteCheckBox")
                val uuid = child.getTag(R.id.profile_uuid) as? UUID
                if (checkBox?.isChecked == true && uuid != null) {
                    toDelete.add(uuid)
                }
            }

            if (toDelete.isEmpty()) {
                Toast.makeText(requireContext(), "삭제할 프로필을 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            deleteButton.isEnabled = false
            var completed = 0
            var successCount = 0

            fun finishOne() {
                completed++
                if (completed == toDelete.size) {
                    // 삭제 후 보정: currentPetId가 비었고 캐시에 남은 펫이 있으면 하나 선택
                    if (PetRepository.getCurrentPet() == null && PetRepository.getAllProfiles().isNotEmpty()) {
                        PetRepository.setCurrentPet(PetRepository.getAllProfiles().first().id)
                    }
                    UserRepository.saveToPreferences(requireContext())
                    deleteButton.isEnabled = true

                    val msg = if (successCount == toDelete.size) "삭제 완료" else "일부 삭제 실패"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }

            // 하나씩 서버 삭제 요청 → 성공 시 UI/캐시/로컬유저 링크 제거
            toDelete.forEach { petId ->
                PetRepository.deleteProfile(
                    userId = currentUser.userId,
                    petId  = petId
                ) { success ->
                    requireActivity().runOnUiThread {
                        if (success) {
                            // UI에서 카드 제거
                            for (i in 0 until petContainer.childCount) {
                                val child = petContainer.getChildAt(i)
                                val idTag = child.getTag(R.id.profile_uuid) as? UUID
                                if (idTag == petId) {
                                    petContainer.removeView(child)
                                    break
                                }
                            }
                            // 유저 캐시 링크 제거
                            currentUser.petIds.remove(petId)
                            successCount++
                        } else {
                            Toast.makeText(requireContext(), "삭제 실패: $petId", Toast.LENGTH_SHORT).show()
                        }
                        finishOne()
                    }
                }
            }
        }

        // 기존 펫 로드
        user?.petIds?.forEach { id ->
            PetRepository.getProfile(id)?.let {
                petContainer.addView(PetProfileViewFactory.create(requireContext(), it))
            }
        }

        return view
    }
}