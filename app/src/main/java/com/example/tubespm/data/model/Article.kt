// File: app/src/main/java/com/example/tubespm/data/model/Article.kt
package com.example.tubespm.data.model

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
    val updatedAt: Date,
    val authorId: String,
    val authorName: String,
    val category: String
)