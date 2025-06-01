package com.example.tubespm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Article::class, // Pastikan Article::class merujuk pada entitas Article Room Anda
            parentColumns = ["id"],  // Kolom PrimaryKey di tabel Article
            childColumns = ["articleId"], // Kolom ForeignKey di tabel Comment
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comment(
    @PrimaryKey val id: String, // ID komentar (misalnya dari API)
    val articleId: String,      // ID artikel yang dikomentari
    val content: String,        // Isi komentar (dari 'body' API)
    val authorName: String,     // Nama penulis (dari 'user.name' API)
    val userId: String,         // ID user yang berkomentar (dari 'user.id' API)
    val createdAt: Date         // Tanggal komentar dibuat
)