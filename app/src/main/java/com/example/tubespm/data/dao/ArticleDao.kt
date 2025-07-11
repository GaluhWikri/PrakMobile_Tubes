package com.example.tubespm.data.dao

import androidx.room.*
import com.example.tubespm.data.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): Article?

    // New query to get articles by authorId
    @Query("SELECT * FROM articles WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getArticlesByAuthorId(authorId: String): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Update
    suspend fun updateArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)
}