package com.example.tubespm.data.model

import com.google.gson.annotations.SerializedName

data class ArticleResponse(
    val id: Int,
    @SerializedName("judul")
    val title: String,
    @SerializedName("gambar")
    val imageUrl: String?,
    @SerializedName("tanggal")
    val date: String, // Maps to "tanggal" from API (e.g., "2025-05-30")
    @SerializedName("penulis")
    val authorName: String,
    @SerializedName("kategori")
    val category: String,
    @SerializedName("isi")
    val content: String,
    @SerializedName("author_id")
    val authorId: Int,
    @SerializedName("created_at") // Added to map "created_at" from API (e.g., "2025-05-30T00:09:59.000000Z")
    val createdAtApi: String?,
    @SerializedName("updated_at") // Added to map "updated_at" from API
    val updatedAtApi: String?
    // The 'user' object from the API response (visible in Postman) is not explicitly mapped here.
    // If 'authorName' should come from 'user.name', this model would need a nested UserResponse class.
    // Currently, 'authorName' is assumed to be directly mapped from the 'penulis' field.
)