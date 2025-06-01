package com.example.tubespm.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

// User object as returned by the API within a comment
data class CommentUserResponse(
    val id: Int,
    val name: String,
    val email: String? // Email bisa jadi tidak selalu dibutuhkan di UI komentar
)

data class CommentApiResponse(
    val id: Int,
    @SerializedName("body")
    val body: String,
    @SerializedName("article_id")
    val articleId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("created_at")
    val createdAtApi: String, // Format tanggal dari API (misal: "2023-10-27T10:00:00.000000Z")
    @SerializedName("updated_at")
    val updatedAtApi: String,
    val user: CommentUserResponse // Objek user yang berkomentar
)

// Data class untuk request pembuatan comment, jika berbeda dari response
data class CreateCommentRequest(
    val body: String,
    // user_id akan diambil dari user yang sedang login di sisi backend (Laravel)
    // article_id akan menjadi bagian dari path URL
)