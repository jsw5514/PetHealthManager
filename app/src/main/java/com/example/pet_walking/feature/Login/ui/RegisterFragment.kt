package com.example.pet_walking.feature.Login.ui
/*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.profile.data.UserProfile
import com.example.pet_walking.feature.profile.repository.UserRepository
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

        // ID 중복 확인 (GET 요청)
        checkIdButton.setOnClickListener {
            val userId = userIdInput.text.toString()
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "ID를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val endpoint = "/checkDuplicateId?id=$userId"
            ApiClient.getByEndpoint(
                endpoint = endpoint,
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

        // 회원가입 요청
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
                put("nickname", username)
            }

            ApiClient.post(
                endpoint = "/signIn",
                json = json,
                onSuccess = { result ->
                    requireActivity().runOnUiThread {
                        if (result == "true") {
                            // 서버에 추가적인 사용자 정보 업로드
                            uploadProfile(userId, username, birth, gender)

                            // 로컬에 저장할 유저 프로필 생성
                            val userProfile = UserProfile(
                                username = username,
                                userId = userId,
                                password = password
                            )
                            UserRepository.registerUser(userProfile)
                            UserRepository.saveToPreferences(requireContext())

                            Toast.makeText(requireContext(), "회원가입 성공", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        } else {
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

        // 로그인 화면으로 이동
        loginText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }

    // 추가 사용자 정보 업로드 함수
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
}*/
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.feature.profile.data.UserProfile
import com.example.pet_walking.feature.profile.repository.UserRepository
import org.json.JSONObject
import java.net.URLEncoder

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

        /* 1) ID 중복 확인 : GET /user/check-id?id=...  */
        checkIdButton.setOnClickListener {
            val userId = userIdInput.text.toString().trim()
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "ID를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val encodedId = URLEncoder.encode(userId, "UTF-8")
            val endpoint = "/user/check-id?id=$encodedId"

            ApiClient.getByEndpoint(
                endpoint = endpoint,
                onSuccess = { response ->
                    requireActivity().runOnUiThread {
                        // 서버 응답 형태가 문자열/JSON 둘 다 올 수 있으니 관대하게 처리
                        val raw = response.trim().trim('"')
                        var available = false
                        if (raw.startsWith("{")) {
                            try {
                                val obj = JSONObject(raw)
                                available = when {
                                    obj.has("available") -> obj.optBoolean("available", false)
                                    obj.has("duplicate") -> !obj.optBoolean("duplicate", false)
                                    obj.has("exists")    -> !obj.optBoolean("exists", true)
                                    else -> false
                                }
                            } catch (_: Exception) { /* ignore */ }
                        } else {
                            // 과거: "false" = 사용 가능
                            available = raw.equals("false", true) || raw.equals("available", true)
                        }

                        if (available) {
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

        /* 2) 회원가입 : POST /user/sign-in */
        registerButton.setOnClickListener {
            val username = nameInput.text.toString().trim()
            val birth    = birthInput.text.toString().trim()
            val gender   = when (genderGroup.checkedRadioButtonId) {
                R.id.radioMale   -> "남성"
                R.id.radioFemale -> "여성"
                else             -> "기타"
            }
            val userId   = userIdInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isBlank() || birth.isBlank() || userId.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("id", userId)
                put("password", password)
                put("nickname", username)
            }

            ApiClient.post(
                endpoint = "/user/sign-in",
                json = json,
                onSuccess = { result ->
                    requireActivity().runOnUiThread {
                        val ok = try {
                            if (result.trim().startsWith("{")) {
                                JSONObject(result).optBoolean("success", false)
                            } else {
                                result.trim().trim('"').equals("true", ignoreCase = true)
                            }
                        } catch (_: Exception) { false }

                        if (ok) {
                            // (선택) 사용자 추가 정보 서버 보관하고 싶다면 /data 사용
                            uploadProfileOptional(userId, username, birth, gender)

                            // 로컬 저장
                            val userProfile = UserProfile(
                                username = username,
                                userId   = userId,
                                password = password
                            )
                            UserRepository.registerUser(userProfile)
                            UserRepository.saveToPreferences(requireContext())

                            Toast.makeText(requireContext(), "회원가입 성공", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        } else {
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

        // 로그인 화면으로 이동
        loginText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }

    /**
     * (선택) 사용자 부가정보를 서버에 저장하고 싶을 때:
     * DataController 신규 규격 → POST /data
     * metaData는 임의 태그("user_profile")로, data는 JSON 문자열로 보냄.
     * 서버에서 필요 없다면 이 함수 호출 자체를 삭제해도 됩니다.
     */
    private fun uploadProfileOptional(userId: String, name: String, birth: String, gender: String) {
        val payload = JSONObject().apply {
            put("uploaderId", userId)
            put("dataId",     "user:$userId")
            put("metaData",   "user_profile")
            put("data", JSONObject().apply {
                put("nickname", name)
                put("birth",    birth)
                put("gender",   gender)
            }.toString())
        }

        ApiClient.post(
            endpoint = "/data",
            json = payload,
            onSuccess = { Log.d("RegisterFragment", "부가 프로필 업로드 성공") },
            onFailure = { Log.e("RegisterFragment", "부가 프로필 업로드 실패: $it") }
        )
    }
}