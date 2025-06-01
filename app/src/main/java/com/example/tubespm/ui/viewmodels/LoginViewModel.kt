package com.example.tubespm.ui.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.AuthResponse
import com.example.tubespm.data.model.LoginRequest
import com.example.tubespm.repository.BlogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: BlogRepository, // BlogRepository might not be strictly needed here if all SharedPreferences logic is in ViewModel
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    // ... (StateFlows untuk email, password, isLoading tetap sama) ...
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private val _loginResult = MutableSharedFlow<Result<AuthResponse>>()
    val loginResult: SharedFlow<Result<AuthResponse>> = _loginResult.asSharedFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun loginUser() {
        viewModelScope.launch {
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _loginResult.emit(Result.failure(Exception("Email dan password harus diisi.")))
                return@launch
            }

            _isLoading.value = true
            val loginRequest = LoginRequest(_email.value, _password.value)
            val result = repository.loginUser(loginRequest) // loginUser di repo TIDAK menyimpan token

            result.onSuccess { authResponse ->
                val editor = sharedPreferences.edit()
                var commitFailed = false

                // Save token
                authResponse.accessToken?.let { token ->
                    editor.putString("auth_token", token)
                    Log.d("LoginViewModel", "Token to be saved: $token")
                } ?: run {
                    Log.e("LoginViewModel", "Access token is null in successful auth response.")
                    _loginResult.emit(Result.failure(Exception("Login berhasil tetapi token tidak diterima.")))
                    commitFailed = true
                }

                if (commitFailed) {
                    _isLoading.value = false
                    return@onSuccess
                }

                // Save user details
                authResponse.user?.let { user ->
                    user.id.let { userId -> // Assuming User.id is String and not nullable
                        editor.putString("user_id", userId)
                        Log.d("LoginViewModel", "User ID to be saved: $userId")
                    }
                    user.name.let { userName -> // Assuming User.name is String and not nullable
                        editor.putString("user_name", userName)
                        Log.d("LoginViewModel", "User Name to be saved: $userName")
                    }
                    user.email.let { userEmail -> // Assuming User.email is String and not nullable
                        editor.putString("user_email", userEmail)
                        Log.d("LoginViewModel", "User Email to be saved: $userEmail")
                    }
                } ?: run {
                    Log.e("LoginViewModel", "User object is null in successful auth response.")
                    // Consider if this is fatal. For ProfileScreen, name/email would be missing.
                }

                editor.apply() // Apply all changes
                Log.d("LoginViewModel", "SharedPreferences applied successfully.")

            }.onFailure {
                Log.e("LoginViewModel", "Login failed: ${it.message}")
            }

            _loginResult.emit(result)
            _isLoading.value = false
        }
    }
}