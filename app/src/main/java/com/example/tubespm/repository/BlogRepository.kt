package com.example.tubespm.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.tubespm.data.dao.ArticleDao
import com.example.tubespm.data.dao.CommentDao
import com.example.tubespm.data.dao.UserDao
import com.example.tubespm.data.model.Article
import com.example.tubespm.data.model.ArticleRequest
import com.example.tubespm.data.model.ArticleResponse
import com.example.tubespm.data.model.AuthResponse
import com.example.tubespm.data.model.Comment // Entitas lokal
import com.example.tubespm.data.model.CommentApiResponse // Respons dari API
import com.example.tubespm.data.model.CreateCommentRequest // Request untuk membuat komen
import com.example.tubespm.data.model.LoginRequest
import com.example.tubespm.data.model.RegisterRequest
import com.example.tubespm.network.ApiService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlogRepository @Inject constructor(
    private val apiService: ApiService,
    private val articleDao: ArticleDao,
    private val commentDao: CommentDao,
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) {

    private val apiUtcDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val apiSimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) // Untuk tanggal saja

    private fun parseApiDateFlexible(dateString: String?): Date {
        if (dateString.isNullOrBlank()) {
            Log.w("BlogRepository", "Date string is null or blank, returning current date.")
            return Date()
        }
        return try {
            apiUtcDateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            try {
                // Fallback untuk format YYYY-MM-DD jika parsing UTC gagal atau string adalah tanggal saja
                apiSimpleDateFormat.parse(dateString) ?: Date()
            } catch (e2: Exception) {
                Log.e("BlogRepository", "Could not parse date: '$dateString'. Error: ${e.message} & ${e2.message}. Returning current date.")
                Date() // Default ke waktu sekarang jika semua parsing gagal
            }
        }
    }

    private fun mapResponseToArticleEntity(response: ArticleResponse): Article {
        return Article(
            id = response.id.toString(),
            title = response.title,
            content = response.content,
            imageUrl = response.imageUrl,
            createdAt = parseApiDateFlexible(response.createdAtApi ?: response.date), // Prioritaskan createdAtApi
            updatedAt = parseApiDateFlexible(response.updatedAtApi ?: response.createdAtApi ?: response.date)
        )
    }

    private fun mapCommentResponseToEntity(response: CommentApiResponse): Comment {
        return Comment(
            id = response.id.toString(),
            articleId = response.articleId.toString(),
            content = response.body,
            authorName = response.user.name,
            userId = response.userId.toString(),
            createdAt = parseApiDateFlexible(response.createdAtApi)
        )
    }

    // --- Article Functions (tetap sama) ---
    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()

    suspend fun getArticleById(id: String): Article? {
        var article = articleDao.getArticleById(id)
        // Selalu coba sinkronisasi dari API untuk detail artikel,
        // karena mungkin ada update atau komentar baru.
        // Atau, Anda bisa memiliki tombol refresh manual di UI.
        try {
            Log.d("BlogRepository", "Fetching article $id from API")
            val response = apiService.getArticle(id)
            if (response.isSuccessful && response.body() != null) {
                val articleEntity = mapResponseToArticleEntity(response.body()!!)
                articleDao.insertArticle(articleEntity) // Insert or Replace
                Log.d("BlogRepository", "Article $id synced from API.")
                return articleEntity
            } else if (article != null) {
                Log.w("BlogRepository", "Failed to fetch article $id from API (${response.code()}), returning cached version.")
                return article // Return cached if API fails but cache exists
            } else {
                Log.e("BlogRepository", "getArticleById - API Error ${response.code()}: ${response.message()} and no cache for $id")
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "getArticleById - Exception fetching article $id", e)
            if (article != null) {
                Log.w("BlogRepository", "Exception fetching article $id from API, returning cached version.")
                return article // Return cached if API fails due to exception
            }
        }
        return null // Return null if API fails and no cache
    }


    suspend fun syncArticles() {
        try {
            Log.d("BlogRepository", "Attempting to sync articles...")
            val response = apiService.getArticles()
            if (response.isSuccessful) {
                response.body()?.let { articleResponses ->
                    if (articleResponses.isEmpty()) {
                        Log.d("BlogRepository", "No articles received from API to sync.")
                    } else {
                        val articlesToInsert = articleResponses.map { mapResponseToArticleEntity(it) }
                        articlesToInsert.forEach { articleDao.insertArticle(it) }
                        Log.d("BlogRepository", "Articles synced successfully: ${articlesToInsert.size} articles.")
                    }
                } ?: Log.d("BlogRepository", "SyncArticles: Response body is null.")
            } else {
                Log.e("BlogRepository", "SyncArticles API call failed: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Error during article synchronization", e)
        }
    }

    suspend fun createArticle(request: ArticleRequest): Result<Article> {
        return try {
            val response = apiService.createArticle(request)
            if (response.isSuccessful && response.body() != null) {
                val articleResponse = response.body()!!
                val articleEntity = mapResponseToArticleEntity(articleResponse)
                articleDao.insertArticle(articleEntity)
                Result.success(articleEntity)
            } else {
                val errorMsg = "Failed to create article on server: ${response.code()} ${response.message()} - ${response.errorBody()?.string()}"
                Log.e("BlogRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception creating article", e)
            Result.failure(e)
        }
    }

    suspend fun updateArticle(articleServerId: String, request: ArticleRequest): Result<Article> {
        return try {
            val response = apiService.updateArticle(articleServerId, request)
            if (response.isSuccessful && response.body() != null) {
                val articleResponse = response.body()!!
                val articleEntity = mapResponseToArticleEntity(articleResponse)
                // Pastikan ID yang digunakan untuk update DAO adalah ID yang sama
                // Mungkin perlu getArticleById dulu jika ID lokal dan server bisa berbeda setelah create
                articleDao.updateArticle(articleEntity)
                Result.success(articleEntity)
            } else {
                val errorMsg = "Failed to update article: ${response.code()} ${response.message()} - ${response.errorBody()?.string()}"
                Log.e("BlogRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception updating article", e)
            Result.failure(e)
        }
    }


    suspend fun deleteArticle(article: Article): Result<Unit> {
        return try {
            val response = apiService.deleteArticle(article.id) // article.id adalah ID server
            if (response.isSuccessful) {
                articleDao.deleteArticle(article)
                Result.success(Unit)
            } else {
                Log.e("BlogRepository", "Failed to delete article on server: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to delete article on server"))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception deleting article", e)
            Result.failure(e)
        }
    }


    // --- Comment Functions ---
    fun getCommentsByArticleId(articleId: String): Flow<List<Comment>> =
        commentDao.getCommentsByArticleId(articleId)

    suspend fun syncCommentsForArticle(articleId: String) {
        try {
            Log.d("BlogRepository", "Attempting to sync comments for article $articleId...")
            val response = apiService.getCommentsForArticle(articleId)
            if (response.isSuccessful) {
                response.body()?.let { commentApiResponses ->
                    // Opsi 1: Hapus semua komen lama lalu insert yang baru (jika API adalah source of truth)
                    // commentDao.deleteAllCommentsByArticleId(articleId)
                    // Log.d("BlogRepository", "Old comments deleted for article $articleId before sync.")

                    val commentsToInsert = commentApiResponses.map { mapCommentResponseToEntity(it) }
                    if (commentsToInsert.isNotEmpty()) {
                        commentDao.insertComments(commentsToInsert) // Gunakan batch insert
                        Log.d("BlogRepository", "Comments for article $articleId synced: ${commentsToInsert.size} comments.")
                    } else {
                        Log.d("BlogRepository", "No comments received from API for article $articleId.")
                    }
                } ?: Log.d("BlogRepository", "SyncCommentsForArticle: Response body is null for article $articleId.")
            } else {
                Log.e("BlogRepository", "SyncCommentsForArticle API call failed for article $articleId: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Error during comment synchronization for article $articleId", e)
        }
    }

    suspend fun createComment(articleId: String, commentBody: String): Result<Comment> {
        return try {
            val request = CreateCommentRequest(body = commentBody)
            val response = apiService.createComment(articleId, request)
            if (response.isSuccessful && response.body() != null) {
                val commentEntity = mapCommentResponseToEntity(response.body()!!)
                commentDao.insertComment(commentEntity)
                Result.success(commentEntity)
            } else {
                val errorMsg = "Failed to create comment: ${response.code()} ${response.message()} - ${response.errorBody()?.string()}"
                Log.e("BlogRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception creating comment", e)
            Result.failure(e)
        }
    }

    suspend fun updateLocalComment(comment: Comment) { // Untuk update dari UI ke local db
        commentDao.updateComment(comment)
    }


    suspend fun deleteCommentFromApiAndLocal(comment: Comment): Result<Unit> {
        return try {
            val response = apiService.deleteComment(comment.id) // Asumsi comment.id adalah ID server
            if (response.isSuccessful) {
                commentDao.deleteComment(comment) // Hapus dari lokal jika sukses di API
                Log.d("BlogRepository", "Comment ${comment.id} deleted successfully from API and local DB.")
                Result.success(Unit)
            } else {
                val errorMsg = "Failed to delete comment ${comment.id} on server: ${response.code()} ${response.message()} - ${response.errorBody()?.string()}"
                Log.e("BlogRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception deleting comment ${comment.id}", e)
            Result.failure(e)
        }
    }


    // --- Auth Functions (tetap sama) ---
    suspend fun loginUser(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown login error"
                Log.e("BlogRepository", "Login failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Login failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during login", e)
            Result.failure(e)
        }
    }

    suspend fun registerUser(registerRequest: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown registration error"
                Log.e("BlogRepository", "Registration failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Registration failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during registration", e)
            Result.failure(e)
        }
    }

    suspend fun logoutUser(): Result<Unit> {
        return try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                sharedPreferences.edit().apply {
                    remove("auth_token")
                    // remove("user_id") // Jika ada user_id yang disimpan
                }.apply()
                Log.d("BlogRepository", "Logout API call successful. Local token and session cleared.")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown logout error"
                Log.e("BlogRepository", "Logout API call failed: ${response.code()} - $errorBody")
                // Tetap hapus token lokal meskipun API gagal, agar user bisa coba login lagi
                sharedPreferences.edit().remove("auth_token").apply()
                Log.w("BlogRepository", "Logout API failed, but local token cleared to allow re-login.")
                Result.failure(Exception("Logout API call failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during logout: ${e.message}", e)
            sharedPreferences.edit().remove("auth_token").apply()
            Log.w("BlogRepository", "Exception during logout, local token cleared to allow re-login.")
            Result.failure(Exception("Exception during logout: ${e.message}", e))
        }
    }
}