package com.example.tubespm.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String // Pastikan nama field ini sesuai dengan yang diharapkan oleh backend Laravel
)