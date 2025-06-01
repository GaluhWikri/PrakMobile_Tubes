package com.example.tubespm.data.dao

import androidx.room.*
import com.example.tubespm.data.model.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE articleId = :articleId ORDER BY createdAt DESC")
    fun getCommentsByArticleId(articleId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Ganti ke REPLACE
    suspend fun insertComment(comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Tambahkan untuk multiple inserts
    suspend fun insertComments(comments: List<Comment>)

    @Update
    suspend fun updateComment(comment: Comment) // Tambahkan fungsi update

    @Delete
    suspend fun deleteComment(comment: Comment)

    @Query("DELETE FROM comments WHERE id = :commentId") // Tambahkan hapus by ID
    suspend fun deleteCommentById(commentId: String)

    @Query("DELETE FROM comments WHERE articleId = :articleId") // Untuk menghapus semua komen artikel tertentu saat sinkronisasi
    suspend fun deleteAllCommentsByArticleId(articleId: String)
}