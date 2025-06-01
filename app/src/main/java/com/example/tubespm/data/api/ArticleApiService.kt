package com.example.tubespm.data.api

import com.example.tubespm.data.model.ArticleResponse
import retrofit2.Call
import retrofit2.http.GET

interface ArticleApiService {
    @GET("api/articles") // sesuaikan endpoint API-mu
    fun getArticles(): Call<List<ArticleResponse>>
}
