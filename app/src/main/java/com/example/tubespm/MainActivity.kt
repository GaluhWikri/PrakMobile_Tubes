package com.example.tubespm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.tubespm.navigation.BlogNavigation
import com.example.tubespm.ui.theme.BlogTheme
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.tubespm.data.api.RetrofitClient
import com.example.tubespm.data.api.ArticleApiService
import com.example.tubespm.data.model.ArticleResponse

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Panggil API Retrofit disini
        fetchArticlesFromApi()

        setContent {
            BlogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlogNavigation()
                }
            }
        }
    }

    private fun fetchArticlesFromApi() {
        val api = RetrofitClient.instance.create(ArticleApiService::class.java)
        val call = api.getArticles()

        call.enqueue(object : Callback<List<ArticleResponse>> {
            override fun onResponse(
                call: Call<List<ArticleResponse>>,
                response: Response<List<ArticleResponse>>
            ) {
                if (response.isSuccessful) {
                    val articles = response.body()
                    articles?.forEach {
                        Log.d("API", "Judul: ${it.title}, Penulis: ${it.authorName}")
                    }
                } else {
                    Log.e("API", "Response gagal dengan kode: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ArticleResponse>>, t: Throwable) {
                Log.e("API", "Gagal koneksi: ${t.message}")
            }
        })
    }
}
