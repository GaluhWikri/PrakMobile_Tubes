package com.example.tubespm.ui.viewmodels

import android.content.SharedPreferences // <-- Import SharedPreferences
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
    private val repository: BlogRepository,
    private val sharedPreferences: SharedPreferences // <-- Inject SharedPreferences
) : ViewModel() {

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

            // Tangani penyimpanan token di sini setelah repository mengembalikan hasil sukses
            result.onSuccess { authResponse ->
                authResponse.accessToken?.let { token ->
                    sharedPreferences.edit().putString("auth_token", token).apply()
                    Log.d("LoginViewModel", "Token saved successfully: $token")
                    // Simpan data user lain jika perlu
                    // authResponse.user?.id?.let { userId ->
                    //     sharedPreferences.edit().putString("user_id", userId.toString()).apply()
                    // }
                } ?: run {
                    // Ini seharusnya tidak terjadi jika API mengembalikan token pada sukses
                    Log.e("LoginViewModel", "Access token is null in successful auth response.")
                    // Emit kegagalan lagi karena token tidak ada
                    _loginResult.emit(Result.failure(Exception("Login berhasil tetapi token tidak diterima.")))
                    _isLoading.value = false
                    return@launch
                }
            }.onFailure {
                Log.e("LoginViewModel", "Login failed: ${it.message}")
            }

            _loginResult.emit(result) // Emit hasil asli ke UI
            _isLoading.value = false
        }
    }
}