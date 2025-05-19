package com.example.pet_walking.Login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.network.ApiClient
import com.example.pet_walking.profile.repository.UserRepository
import org.json.JSONObject

class LoginFragment : Fragment() {

    private lateinit var userIdInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var joinButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        userIdInput = view.findViewById(R.id.userIdInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        loginButton = view.findViewById(R.id.loginButton)
        joinButton = view.findViewById(R.id.joinButton)

        loginButton.setOnClickListener {
            val userId = userIdInput.text.toString()
            val password = passwordInput.text.toString()

            if (userId.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("id", userId)
                put("password", password)
            }

            ApiClient.post(
                endpoint = "/login",
                json = json,
                onSuccess = { result ->
                    requireActivity().runOnUiThread {
                        if (result == "true") {
                            Toast.makeText(requireContext(), "로그인 성공!", Toast.LENGTH_SHORT).show()

                            // 로그인 후 UserRepository 상태 갱신
                            val success = UserRepository.login(userId, password)
                            if (success) {
                                UserRepository.saveToPreferences(requireContext())
                            }

                            // 다음 화면으로 이동
                            findNavController().navigate(R.id.action_loginFragment_to_userFragment)
                        } else {
                            Toast.makeText(requireContext(), "로그인 실패: 아이디 또는 비밀번호 오류", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = { error ->
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "서버 오류: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        joinButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}