package com.example.tubespm.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Article
import com.example.tubespm.repository.BlogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val repository: BlogRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _deleteResult = MutableSharedFlow<Result<Unit>>()
    val deleteResult: SharedFlow<Result<Unit>> = _deleteResult.asSharedFlow()

    val articles: StateFlow<List<Article>> = repository.getAllArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Tambahkan L untuk Long
            initialValue = emptyList()
        )

    // Untuk hasil logout
    private val _logoutEvent = MutableSharedFlow<LogoutEvent>() // Menggunakan sealed class/interface untuk event yang lebih kaya
    val logoutEvent: SharedFlow<LogoutEvent> = _logoutEvent.asSharedFlow()

    sealed class LogoutEvent {
        data object Success : LogoutEvent()
        data class Error(val message: String?) : LogoutEvent()
    }

    init {
        refresh() // Initial load
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.syncArticles()
            } catch (e: Exception) {
                _logoutEvent.emit(LogoutEvent.Error("Gagal memuat artikel: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            val result = repository.deleteArticle(article)
            _deleteResult.emit(result)
            if (result.isSuccess) {
                refresh()
            }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.logoutUser()
            if (result.isSuccess) {
                // BlogRepository seharusnya sudah menghapus token lokal
                _logoutEvent.emit(LogoutEvent.Success)
            } else {
                _logoutEvent.emit(LogoutEvent.Error(result.exceptionOrNull()?.message ?: "Logout gagal"))
            }
            _isLoading.value = false
        }
    }
}