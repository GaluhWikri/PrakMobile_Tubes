package com.example.tubespm.dimodule

import android.content.Context
import android.content.SharedPreferences
import com.example.tubespm.network.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("blog_app_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sharedPreferences: SharedPreferences): Interceptor {
        return Interceptor { chain ->
            val token = sharedPreferences.getString("auth_token", null)
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/json")

            token?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson { // Provider untuk Gson
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit { // Inject Gson di sini
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/api/") // Pastikan URL API Anda benar
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Gunakan instance Gson
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}