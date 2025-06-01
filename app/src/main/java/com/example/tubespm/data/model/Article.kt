package com.example.tubespm.data.model

// Data Models
// Article.kt
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val createdAt: Date,
    val updatedAt: Date
)