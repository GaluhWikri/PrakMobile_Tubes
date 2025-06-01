package com.example.tubespm.data.model

import com.google.gson.annotations.SerializedName

data class ArticleRequest(
    @SerializedName("judul")
    val title: String,

    @SerializedName("isi")
    val content: String,

    @SerializedName("gambar")
    val imageUrl: String?,

    @SerializedName("tanggal") // Format yang diharapkan: "YYYY-MM-DD"
    val date: String,

    @SerializedName("penulis")
    val authorName: String,

    @SerializedName("kategori")
    val category: String,

    @SerializedName("author_id")
    val authorId: Int // Ini idealnya ID pengguna yang sedang login
)