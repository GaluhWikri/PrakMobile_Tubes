package com.example.tubespm.network

import com.example.tubespm.data.model.ArticleRequest
import com.example.tubespm.data.model.ArticleResponse
import com.example.tubespm.data.model.AuthResponse
// import com.example.tubespm.data.model.Comment // Hapus import ini jika tidak digunakan langsung
import com.example.tubespm.data.model.CommentApiResponse // Import baru
import com.example.tubespm.data.model.CreateCommentRequest // Import baru
import com.example.tubespm.data.model.LoginRequest
import com.example.tubespm.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ... (Endpoint Artikel, Login, Register, Logout tetap sama) ...
    @GET("articles")
    suspend fun getArticles(): Response<List<ArticleResponse>>

    @GET("articles/{id}")
    suspend fun getArticle(@Path("id") id: String): Response<ArticleResponse>

    @POST("articles")
    suspend fun createArticle(@Body articleRequest: ArticleRequest): Response<ArticleResponse>

    @PUT("articles/{id}")
    suspend fun updateArticle(@Path("id") id: String, @Body articleRequest: ArticleRequest): Response<ArticleResponse>

    @DELETE("articles/{id}")
    suspend fun deleteArticle(@Path("id") id: String): Response<Unit>


    // Endpoint Komentar
    @GET("articles/{article_id}/comments") // Path parameter API adalah {article} bukan {id}
    suspend fun getCommentsForArticle(@Path("article_id") articleId: String): Response<List<CommentApiResponse>>

    @POST("articles/{article_id}/comments")
    suspend fun createComment(
        @Path("article_id") articleId: String,
        @Body commentRequest: CreateCommentRequest
    ): Response<CommentApiResponse> // API Laravel mengembalikan comment yang baru dibuat

    @PUT("comments/{comment_id}")
    suspend fun updateComment(
        @Path("comment_id") commentId: String,
        @Body commentRequest: CreateCommentRequest // Asumsi request update sama dengan create
    ): Response<CommentApiResponse>

    @DELETE("comments/{comment_id}")
    suspend fun deleteComment(@Path("comment_id") commentId: String): Response<Unit>

    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>
}