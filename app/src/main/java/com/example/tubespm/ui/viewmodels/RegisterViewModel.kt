package com.example.tubespm.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.AuthResponse
import com.example.tubespm.data.model.RegisterRequest
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
class RegisterViewModel @Inject constructor(
    private val repository: BlogRepository
    // Pertimbangkan untuk inject SharedPreferences/DataStoreHelper jika ingin menyimpan token di sini
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordConfirmation = MutableStateFlow("")
    val passwordConfirmation: StateFlow<String> = _passwordConfirmation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _registrationResult = MutableSharedFlow<Result<AuthResponse>>()
    val registrationResult: SharedFlow<Result<AuthResponse>> = _registrationResult.asSharedFlow()

    fun onNameChange(name: String) {
        _name.value = name
    }

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onPasswordConfirmationChange(passwordConfirmation: String) {
        _passwordConfirmation.value = passwordConfirmation
    }

    fun registerUser() {
        viewModelScope.launch {
            if (_name.value.isBlank() || _email.value.isBlank() || _password.value.isBlank() || _passwordConfirmation.value.isBlank()) {
                _registrationResult.emit(Result.failure(Exception("Semua field harus diisi.")))
                return@launch
            }

            if (_password.value != _passwordConfirmation.value) {
                _registrationResult.emit(Result.failure(Exception("Password dan konfirmasi password tidak cocok.")))
                return@launch
            }

            _isLoading.value = true
            val registerRequest = RegisterRequest(
                name = _name.value,
                email = _email.value,
                password = _password.value,
                password_confirmation = _passwordConfirmation.value
            )
            val result = repository.registerUser(registerRequest)
            _registrationResult.emit(result)
            _isLoading.value = false

            // Jika registrasi sukses, Anda mungkin ingin menyimpan token & data pengguna
            // atau menavigasi pengguna. Ini sering ditangani di UI berdasarkan _registrationResult.
        }
    }
}
