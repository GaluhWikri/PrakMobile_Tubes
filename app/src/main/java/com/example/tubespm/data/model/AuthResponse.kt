package com.example.tubespm.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val message: String?,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    val user: User? // Menggunakan model User yang sudah ada. Sesuaikan jika perlu.
    // com.example.tubespm.data.model.User
)