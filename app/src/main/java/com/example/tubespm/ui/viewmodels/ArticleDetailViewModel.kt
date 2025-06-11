package com.example.tubespm.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Article
import com.example.tubespm.data.model.Comment
import com.example.tubespm.repository.BlogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: BlogRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = savedStateHandle.get<String>("articleId") ?: ""

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _isLoadingArticle = MutableStateFlow(false)
    val isLoadingArticle: StateFlow<Boolean> = _isLoadingArticle.asStateFlow()

    private val _isLoadingComments = MutableStateFlow(false) // State terpisah untuk loading comment
    val isLoadingComments: StateFlow<Boolean> = _isLoadingComments.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    // authorName untuk input field komentar baru sudah tidak diperlukan lagi di ViewModel
    // karena nama akan diambil dari user yang sedang login di backend.
    // Jika Anda tetap ingin mengizinkan input nama manual (misal untuk guest), Anda bisa mempertahankannya.
    // Untuk saat ini, saya akan menghapusnya untuk menyederhanakan, asumsi hanya user login yang bisa komen.

    private val _operationResult = MutableSharedFlow<Result<Any>>() // Untuk create/delete
    val operationResult: SharedFlow<Result<Any>> = _operationResult.asSharedFlow()

    val comments: StateFlow<List<Comment>> = repository.getCommentsByArticleId(articleId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        if (articleId.isNotEmpty()) {
            loadArticleAndComments()
        } else {
            Log.e("ArticleDetailVM", "Article ID is empty!")
            // Handle error, misalnya emit event ke UI
        }
    }

    private fun loadArticleAndComments() {
        viewModelScope.launch {
            _isLoadingArticle.value = true
            _article.value = repository.getArticleById(articleId) // getArticleById juga bisa sinkronisasi
            _isLoadingArticle.value = false
            // Setelah artikel dimuat (atau bersamaan), sinkronkan komentar
            refreshComments()
        }
    }

    fun refreshComments() {
        if (articleId.isEmpty()) return
        viewModelScope.launch {
            _isLoadingComments.value = true
            try {
                repository.syncCommentsForArticle(articleId)
            } catch (e: Exception) {
                Log.e("ArticleDetailVM", "Error syncing comments", e)
                _operationResult.emit(Result.failure(e)) // Atau handle error spesifik
            } finally {
                _isLoadingComments.value = false
            }
        }
    }


    fun updateCommentText(text: String) {
        _commentText.value = text
    }

    fun addComment() {
        if (_commentText.value.isBlank()) {
            viewModelScope.launch { _operationResult.emit(Result.failure(Exception("Komentar tidak boleh kosong."))) }
            return
        }
        if (articleId.isEmpty()) {
            viewModelScope.launch { _operationResult.emit(Result.failure(Exception("ID Artikel tidak valid."))) }
            return
        }

        viewModelScope.launch {
            _isLoadingComments.value = true // Menunjukkan proses sedang berjalan
            val result = repository.createComment(articleId, _commentText.value)
            _operationResult.emit(result) // Emit hasil ke UI untuk Toast atau feedback
            if (result.isSuccess) {
                _commentText.value = "" // Bersihkan field input
                refreshComments()     // Refresh daftar komentar untuk menampilkan yang baru
            }
            _isLoadingComments.value = false
        }
    }

    fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            _isLoadingComments.value = true
            val result = repository.deleteCommentFromApiAndLocal(comment)
            _operationResult.emit(result)
            if (result.isSuccess) {
                refreshComments() // Refresh daftar komentar
            }
            _isLoadingComments.value = false
        }
    }
}