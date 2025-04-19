package com.example.pet_walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.network.ApiClient
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
                put("id", userId) // 서버가 요구하는 key는 "id"
                put("password", password)
            }

            ApiClient.post(
                endpoint = "/login",
                json = json,
                onSuccess = { result ->
                    activity?.runOnUiThread {
                        if (result == "true") {
                            Toast.makeText(requireContext(), "로그인 성공!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_userFragment)
                        } else {
                            Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = { error ->
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
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