package com.example.pet_walking.feature.Login.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pet_walking.R
import com.example.pet_walking.feature.profile.repository.PetRepository
import com.example.pet_walking.feature.profile.repository.UserRepository

class LoginFragment : Fragment() {

    private lateinit var userIdInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var joinButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("LoginFragment", "[DEBUG] onCreateView called")
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        userIdInput = view.findViewById(R.id.userIdInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        loginButton = view.findViewById(R.id.loginButton)
        joinButton = view.findViewById(R.id.joinButton)

        loginButton.setOnClickListener {
            val userId = userIdInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            Log.d("LoginFragment", "[DEBUG] loginButton clicked: userId='$userId'")

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "아이디와 비밀번호를 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Log.d("LoginFragment", "[DEBUG] calling UserRepository.login(userId='$userId')")
            // 1) UserRepository.login 호출
            UserRepository.login(userId, password) { success, errorMsg ->
                Log.d("LoginFragment", "[DEBUG] login callback: success=$success, errorMsg=$errorMsg")
                requireActivity().runOnUiThread {
                    if (!success) {
                        Toast.makeText(
                            requireContext(),
                            errorMsg ?: "로그인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@runOnUiThread
                    }

                    // 2) 로그인 성공
                    Log.d("LoginFragment", "[DEBUG] login successful for userId='$userId'")
                    Toast.makeText(
                        requireContext(),
                        "로그인 성공!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 3) 캐시 저장
                    UserRepository.saveToPreferences(requireContext())
                    Log.d("LoginFragment", "[DEBUG] Preferences saved. CurrentUser=${UserRepository.getCurrentUser()}")

                    // 4) 펫 프로필 ID 리스트 가져오기
                    val petIds = UserRepository.getCurrentUser()?.petIds.orEmpty()
                    Log.d("LoginFragment", "[DEBUG] Retrieved petIds: $petIds")

                    // 펫이 하나도 없으면 바로 다음 화면으로 이동
                    if (petIds.isEmpty()) {
                        Log.d("LoginFragment", "[DEBUG] No pet profiles, navigating directly to UserFragment")
                        findNavController().navigate(R.id.action_loginFragment_to_userFragment)
                        return@runOnUiThread
                    }

                    // 5) PetRepository에서 프로필을 전부 불러온 뒤 화면 전환
                    Log.d("LoginFragment", "[DEBUG] Loading pet profiles from server for petIds=$petIds")
                    PetRepository.loadProfilesFromServer(
                        userId = userId,
                        petIds = petIds,
                        onComplete = {
                            Log.d("LoginFragment", "[DEBUG] Pet profiles loaded successfully, navigating to UserFragment")
                            requireActivity().runOnUiThread {
                                findNavController()
                                    .navigate(R.id.action_loginFragment_to_userFragment)
                            }
                        },
                        onError = { err ->
                            Log.d("LoginFragment", "[DEBUG] Pet profiles loading error: $err")
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "펫 프로필 로딩 실패: $err",
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController()
                                    .navigate(R.id.action_loginFragment_to_userFragment)
                            }
                        }
                    )
                }
            }
        }

        joinButton.setOnClickListener {
            Log.d("LoginFragment", "[DEBUG] joinButton clicked, navigating to RegisterFragment")
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}