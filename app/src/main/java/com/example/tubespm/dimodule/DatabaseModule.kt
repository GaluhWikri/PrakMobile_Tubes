package com.example.tubespm.dimodule

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import androidx.room.Room
import com.example.tubespm.data.AppDatabase
import com.example.tubespm.data.dao.ArticleDao
import com.example.tubespm.data.dao.CommentDao
import com.example.tubespm.data.dao.UserDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "blog_database"
        )
            .fallbackToDestructiveMigration() // <--- TAMBAHKAN BARIS INI
            .build()
    }

    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao = database.articleDao()

    @Provides
    fun provideCommentDao(database: AppDatabase): CommentDao = database.commentDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}