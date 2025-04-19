package com.example.pet_walking

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.network.ApiClient
import org.json.JSONObject

class RegisterFragment : Fragment() {

    private lateinit var nameInput: EditText
    private lateinit var birthInput: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var userIdInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginText: TextView
    private lateinit var checkIdButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        nameInput = view.findViewById(R.id.editTextName)
        birthInput = view.findViewById(R.id.editTextBirth)
        genderGroup = view.findViewById(R.id.radioGroupGender)
        userIdInput = view.findViewById(R.id.editTextUserId)
        passwordInput = view.findViewById(R.id.editTextPassword)
        registerButton = view.findViewById(R.id.buttonRegister)
        loginText = view.findViewById(R.id.textGoToLogin)
        checkIdButton = view.findViewById(R.id.buttonCheckId)

        checkIdButton.setOnClickListener {
            val userId = userIdInput.text.toString()
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "ID를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 중복 체크는 GET 요청 (직접 요청 유지)
            val url = "http://10.0.2.2:8080/checkDuplicateId?id=$userId"
            ApiClient.get(
                fullUrl = url,
                onSuccess = { response ->
                    requireActivity().runOnUiThread {
                        if (response == "false") {
                            Toast.makeText(requireContext(), "사용 가능한 ID입니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "이미 사용 중인 ID입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        registerButton.setOnClickListener {
            val username = nameInput.text.toString()
            val birth = birthInput.text.toString()
            val gender = when (genderGroup.checkedRadioButtonId) {
                R.id.radioMale -> "남성"
                R.id.radioFemale -> "여성"
                else -> "기타"
            }
            val userId = userIdInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isBlank() || birth.isBlank() || userId.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("id", userId)
                put("password", password)
            }

            ApiClient.post(
                endpoint = "/signIn",
                json = json,
                onSuccess = { result ->
                    if (result == "true") {
                        uploadProfile(userId, username, birth, gender)

                        val userProfile = UserProfile(
                            username = username,
                            birthdate = birth,
                            gender = gender,
                            userId = userId,
                            password = password
                        )
                        UserRepository.registerUser(userProfile)
                        UserRepository.saveToPreferences(requireContext())

                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "회원가입 성공", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "회원가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = { error ->
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        loginText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }

    private fun uploadProfile(userId: String, name: String, birth: String, gender: String) {
        val json = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId", "userProfile")
            put("metaData", "name:$name,birth:$birth,gender:$gender")
            put("data", "")
        }

        ApiClient.post(
            endpoint = "/uploadData",
            json = json,
            onSuccess = { Log.d("RegisterFragment", "프로필 업로드 성공") },
            onFailure = { Log.e("RegisterFragment", "프로필 업로드 실패") }
        )
    }
}