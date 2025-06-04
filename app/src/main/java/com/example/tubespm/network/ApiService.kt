package com.example.tubespm.network

import com.example.tubespm.data.model.ArticleRequest
import com.example.tubespm.data.model.ArticleResponse
import com.example.tubespm.data.model.AuthResponse
import com.example.tubespm.data.model.CommentApiResponse
import com.example.tubespm.data.model.CreateCommentRequest
import com.example.tubespm.data.model.LoginRequest
import com.example.tubespm.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("articles")
    suspend fun getArticles(@Query("kategori") category: String? = null): Response<List<ArticleResponse>> // Modified

    @GET("articles/{id}")
    suspend fun getArticle(@Path("id") id: String): Response<ArticleResponse>

    @POST("articles")
    suspend fun createArticle(@Body articleRequest: ArticleRequest): Response<ArticleResponse>

    @PUT("articles/{id}")
    suspend fun updateArticle(@Path("id") id: String, @Body articleRequest: ArticleRequest): Response<ArticleResponse>

    @DELETE("articles/{id}")
    suspend fun deleteArticle(@Path("id") id: String): Response<Unit>

    @GET("articles/{article_id}/comments")
    suspend fun getCommentsForArticle(@Path("article_id") articleId: String): Response<List<CommentApiResponse>>

    @POST("articles/{article_id}/comments")
    suspend fun createComment(
        @Path("article_id") articleId: String,
        @Body commentRequest: CreateCommentRequest
    ): Response<CommentApiResponse>

    @PUT("comments/{comment_id}")
    suspend fun updateComment(
        @Path("comment_id") commentId: String,
        @Body commentRequest: CreateCommentRequest
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