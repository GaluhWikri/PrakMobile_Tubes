package com.example.tubespm.ui.uiscreens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tubespm.ui.viewmodels.RegisterViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit, // Callback untuk navigasi setelah registrasi berhasil
    onNavigateBackToLogin: () -> Unit // Callback untuk navigasi kembali ke halaman login
) {
    val name by registerViewModel.name.collectAsState()
    val email by registerViewModel.email.collectAsState()
    val password by registerViewModel.password.collectAsState()
    val passwordConfirmation by registerViewModel.passwordConfirmation.collectAsState()
    val isLoading by registerViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        registerViewModel.registrationResult.collectLatest { result ->
            result.fold(
                onSuccess = { authResponse ->
                    // TODO: Simpan token (authResponse.accessToken) dan data user (authResponse.user)
                    // Mirip dengan di LoginScreen
                    Toast.makeText(context, authResponse.message ?: "Registrasi Berhasil!", Toast.LENGTH_LONG).show()
                    onRegisterSuccess() // Panggil callback untuk navigasi
                },
                onFailure = { error ->
                    Toast.makeText(context, "Registrasi Gagal: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Buat Akun Baru",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Daftar untuk bergabung dengan Boooom Blog",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { registerViewModel.onNameChange(it) },
                label = { Text("Nama Lengkap") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nama Icon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { registerViewModel.onEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { registerViewModel.onPasswordChange(it) },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = passwordConfirmation,
                onValueChange = { registerViewModel.onPasswordConfirmationChange(it) },
                label = { Text("Konfirmasi Password") },
                leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = "Konfirmasi Password Icon") }, // Ganti ikon jika perlu
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordConfirmationVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordConfirmationVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordConfirmationVisible = !passwordConfirmationVisible }) {
                        Icon(imageVector = image, if (passwordConfirmationVisible) "Hide password" else "Show password")
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { registerViewModel.registerUser() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("DAFTAR", fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sudah punya akun?")
                TextButton(onClick = onNavigateBackToLogin) {
                    Text("Login di sini", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}