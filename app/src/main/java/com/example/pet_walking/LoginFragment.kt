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
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LoginFragment : Fragment() {

    private lateinit var userIdInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var joinButton: Button

    private val client = OkHttpClient()
    private val serverUrl = "http://10.0.2.2:8080"  // 💡 PC에서 실행 중인 서버: 에뮬레이터 기준 IP

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
                put("userid", userId)
                put("password", password)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$serverUrl/login")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    activity?.runOnUiThread {
                        if (response.isSuccessful && body == "true") {
                            Toast.makeText(requireContext(), "로그인 성공!", Toast.LENGTH_SHORT).show()
                            // ✅ 로컬 저장이 필요하다면 여기에 추가: UserRepository.login(userId, password)
                            findNavController().navigate(R.id.action_loginFragment_to_userFragment)
                        } else {
                            Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        joinButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}