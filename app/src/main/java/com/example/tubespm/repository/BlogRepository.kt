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
import kotlinx.coroutines.flow.emptyFlow
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
    private val apiSimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    private fun parseApiDateFlexible(dateString: String?): Date {
        if (dateString.isNullOrBlank()) {
            Log.w("BlogRepository", "Date string is null or blank, returning current date.")
            return Date()
        }
        return try {
            apiUtcDateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            try {
                apiSimpleDateFormat.parse(dateString) ?: Date()
            } catch (e2: Exception) {
                Log.e("BlogRepository", "Could not parse date: '$dateString'. Error: ${e.message} & ${e2.message}. Returning current date.")
                Date()
            }
        }
    }

    private fun mapResponseToArticleEntity(response: ArticleResponse): Article {
        return Article(
            id = response.id.toString(),
            title = response.title,
            content = response.content,
            imageUrl = response.imageUrl,
            createdAt = parseApiDateFlexible(response.createdAtApi ?: response.date),
            updatedAt = parseApiDateFlexible(response.updatedAtApi ?: response.createdAtApi ?: response.date),
            authorId = response.authorId.toString() // Mengonversi Int ke String
        )
    }

    private fun mapCommentResponseToEntity(response: CommentApiResponse): Comment {
        return Comment(
            id = response.id.toString(),
            articleId = response.articleId.toString(),
            content = response.body,
            authorName = response.user.name, // Dari objek User yang ada di dalam CommentApiResponse
            userId = response.userId.toString(),
            createdAt = parseApiDateFlexible(response.createdAtApi)
        )
    }

    // --- Article Functions ---
    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()

    fun getArticlesByAuthorId(authorId: String): Flow<List<Article>> {
        if (authorId.isBlank()) return emptyFlow()
        return articleDao.getArticlesByAuthorId(authorId)
    }

    suspend fun getArticleById(id: String): Article? {
        var article = articleDao.getArticleById(id)
        try {
            Log.d("BlogRepository", "Fetching article $id from API")
            val response = apiService.getArticle(id)
            if (response.isSuccessful && response.body() != null) {
                val articleEntity = mapResponseToArticleEntity(response.body()!!)
                articleDao.insertArticle(articleEntity)
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
            Log.d("BlogRepository", "Attempting to delete article with ID: ${article.id} from API.")
            val response = apiService.deleteArticle(article.id)

            if (response.isSuccessful) {
                Log.d("BlogRepository", "Article ${article.id} deleted successfully from API. Deleting from local DAO.")
                articleDao.deleteArticle(article)
                Result.success(Unit)
            } else {
                val errorMsg = "Failed to delete article ${article.id} on server: ${response.code()} ${response.message()} - ${response.errorBody()?.string()}"
                Log.e("BlogRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception deleting article ${article.id}", e)
            Result.failure(e)
        }
    }


    // --- User Info from SharedPreferences ---
    fun getCurrentUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    fun getCurrentUserName(): String? {
        return sharedPreferences.getString("user_name", "Nama Pengguna") // Default value jika tidak ditemukan
    }

    fun getCurrentUserEmail(): String? {
        return sharedPreferences.getString("user_email", "email@example.com") // Default value
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
                    val commentsToInsert = commentApiResponses.map { mapCommentResponseToEntity(it) }
                    if (commentsToInsert.isNotEmpty()) {
                        commentDao.insertComments(commentsToInsert)
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
            val requestBody = CreateCommentRequest(body = commentBody)
            val response = apiService.createComment(articleId, requestBody)
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

    suspend fun updateLocalComment(comment: Comment) {
        commentDao.updateComment(comment)
    }


    suspend fun deleteCommentFromApiAndLocal(comment: Comment): Result<Unit> {
        return try {
            Log.d("BlogRepository", "Attempting to delete comment with ID: ${comment.id} from API.")
            val response = apiService.deleteComment(comment.id)

            if (response.isSuccessful) {
                Log.d("BlogRepository", "Comment ${comment.id} deleted successfully from API. Deleting from local DAO.")
                commentDao.deleteComment(comment)
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

    // --- Auth Functions ---
    suspend fun loginUser(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                // LoginViewModel akan menangani penyimpanan detail pengguna ke SharedPreferences
                Log.d("BlogRepository", "Login API call successful for ${loginRequest.email}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown login error"
                Log.e("BlogRepository", "Login API call failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Login failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during login API call", e)
            Result.failure(e)
        }
    }

    suspend fun registerUser(registerRequest: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                // RegisterViewModel/LoginViewModel akan menangani penyimpanan detail pengguna ke SharedPreferences jika diperlukan setelah ini
                Log.d("BlogRepository", "Register API call successful for ${registerRequest.email}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown registration error"
                Log.e("BlogRepository", "Registration API call failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Registration failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during registration API call", e)
            Result.failure(e)
        }
    }

    suspend fun logoutUser(): Result<Unit> {
        try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                Log.d("BlogRepository", "Logout API call successful.")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown logout error"
                Log.e("BlogRepository", "Logout API call failed: ${response.code()} - $errorBody. Proceeding with local cleanup.")
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during logout API call: ${e.message}. Proceeding with local cleanup.", e)
        } finally {
            // Selalu bersihkan data sesi lokal terlepas dari hasil panggilan API
            sharedPreferences.edit().apply {
                remove("auth_token")
                remove("user_id")
                remove("user_name") // Membersihkan nama pengguna
                remove("user_email") // Membersihkan email pengguna
            }.apply()
            Log.d("BlogRepository", "Local token and user details cleared from SharedPreferences.")
        }
        return Result.success(Unit) // Kembalikan success untuk operasi pembersihan lokal
    }
}