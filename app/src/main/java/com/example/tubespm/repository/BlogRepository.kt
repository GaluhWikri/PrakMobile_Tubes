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
import com.example.tubespm.data.model.Comment
import com.example.tubespm.data.model.CommentApiResponse
import com.example.tubespm.data.model.CreateCommentRequest
import com.example.tubespm.data.model.LoginRequest
import com.example.tubespm.data.model.RegisterRequest
import com.example.tubespm.network.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

data class ApiErrorResponse(val message: String?, val errors: Map<String, List<String>>? = null) // Updated to include validation errors

@Singleton
class BlogRepository @Inject constructor(
    private val apiService: ApiService,
    private val articleDao: ArticleDao,
    private val commentDao: CommentDao,
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson // Gson injected
) {

    private val apiUtcDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val apiSimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    private fun parseApiDateFlexible(dateString: String?): Date {
        if (dateString.isNullOrBlank()) {
            return Date()
        }
        return try {
            apiUtcDateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            try {
                apiSimpleDateFormat.parse(dateString) ?: Date()
            } catch (e2: Exception) {
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
            authorId = response.authorId.toString(),
            authorName = response.authorName,
            category = response.category // <--- PASTIKAN BARIS INI ADA DAN BENAR
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

    private fun extractErrorMessage(errorBodyString: String?, defaultMessage: String): String {
        var finalErrorMessage = defaultMessage
        if (!errorBodyString.isNullOrBlank()) {
            try {
                val apiError = gson.fromJson(errorBodyString, ApiErrorResponse::class.java)
                if (!apiError.message.isNullOrBlank()) {
                    finalErrorMessage = apiError.message
                } else if (apiError.errors != null && apiError.errors.isNotEmpty()) {
                    // Handle validation errors (e.g., take the first message from the first field)
                    finalErrorMessage = apiError.errors.entries.firstOrNull()?.value?.firstOrNull() ?: defaultMessage
                }
            } catch (e: Exception) {
                Log.w("BlogRepository", "Failed to parse error body JSON: $errorBodyString", e)
                // If parsing fails but errorBodyString is not blank, maybe use it? Or stick to default.
                // For simplicity, stick to default if parsing fails.
            }
        }
        return finalErrorMessage
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
            val response = apiService.getArticle(id)
            if (response.isSuccessful && response.body() != null) {
                val articleEntity = mapResponseToArticleEntity(response.body()!!)
                articleDao.insertArticle(articleEntity)
                return articleEntity
            } else if (article != null) {
                return article
            }
        } catch (e: Exception) {
            if (article != null) return article
        }
        return null
    }

    suspend fun syncArticles(categoryQuery: String? = null) { // Modified to accept category
        try {
            // If categoryQuery is "All", send null to API to fetch all articles
            val effectiveCategory = if (categoryQuery == "All") null else categoryQuery
            val response = apiService.getArticles(effectiveCategory) // Pass category to API
            if (response.isSuccessful) {
                response.body()?.let { articleResponses ->
                    val articlesToInsert = articleResponses.map { mapResponseToArticleEntity(it) }
                    // If filtering, we might want to clear old articles of *other* categories,
                    // or clear all and re-insert. For simplicity with "All", clear and insert.
                    // If a specific category is chosen, you might only update/insert those.
                    // For now, let's assume sync replaces relevant articles.
                    // If effectiveCategory is null (meaning "All"), perhaps clear all local articles first.
                    // This part needs careful consideration based on desired offline behavior.
                    // A simple approach for now:
                    articlesToInsert.forEach { articleDao.insertArticle(it) }
                }
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Error during article synchronization with category: $categoryQuery", e)
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
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBodyString, "Gagal membuat artikel.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server."))
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
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBodyString, "Gagal memperbarui artikel.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server."))
        }
    }

    suspend fun deleteArticle(article: Article): Result<Unit> {
        return try {
            val response = apiService.deleteArticle(article.id)
            if (response.isSuccessful) {
                articleDao.deleteArticle(article)
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBodyString, "Gagal menghapus artikel.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server."))
        }
    }

    // --- User Info from SharedPreferences ---
    fun getCurrentUserId(): String? = sharedPreferences.getString("user_id", null)
    fun getCurrentUserName(): String? = sharedPreferences.getString("user_name", "Nama Pengguna")
    fun getCurrentUserEmail(): String? = sharedPreferences.getString("user_email", "email@example.com")

    // --- Comment Functions ---
    fun getCommentsByArticleId(articleId: String): Flow<List<Comment>> =
        commentDao.getCommentsByArticleId(articleId)

    suspend fun syncCommentsForArticle(articleId: String) {
        try {
            val response = apiService.getCommentsForArticle(articleId)
            if (response.isSuccessful) {
                response.body()?.let { commentApiResponses ->
                    val commentsToInsert = commentApiResponses.map { mapCommentResponseToEntity(it) }
                    if (commentsToInsert.isNotEmpty()) {
                        commentDao.insertComments(commentsToInsert)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Error syncing comments for article $articleId", e)
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
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBodyString, "Gagal mengirim komentar.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server."))
        }
    }

    suspend fun deleteCommentFromApiAndLocal(comment: Comment): Result<Unit> {
        return try {
            val response = apiService.deleteComment(comment.id)
            if (response.isSuccessful) {
                commentDao.deleteComment(comment)
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBodyString, "Gagal menghapus komentar.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server."))
        }
    }

    // --- Auth Functions ---
    suspend fun loginUser(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBodyString, "Login gagal. Periksa kembali email dan password Anda.")
                Log.e("BlogRepository", "Login API call failed: ${response.code()} - Body: $errorBodyString - ParsedMsg: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during login API call", e)
            Result.failure(Exception("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        }
    }

    suspend fun registerUser(registerRequest: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBodyString, "Registrasi gagal. Silakan coba lagi.")
                Log.e("BlogRepository", "Registration API call failed: ${response.code()} - Body: $errorBodyString - ParsedMsg: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during registration API call", e)
            Result.failure(Exception("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        }
    }

    suspend fun logoutUser(): Result<Unit> {
        try {
            apiService.logout()
        } catch (e: Exception) {
            Log.e("BlogRepository", "Exception during logout API call: ${e.message}. Proceeding with local cleanup.", e)
        } finally {
            sharedPreferences.edit().apply {
                remove("auth_token")
                remove("user_id")
                remove("user_name")
                remove("user_email")
            }.apply()
            Log.d("BlogRepository", "Local token and user details cleared.")
        }
        return Result.success(Unit)
    }
}