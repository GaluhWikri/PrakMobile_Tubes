package com.example.tubespm.ui.viewmodels

import android.content.SharedPreferences
import android.util.Log
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
    private val repository: BlogRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _deleteResult = MutableSharedFlow<Result<Unit>>()
    val deleteResult: SharedFlow<Result<Unit>> = _deleteResult.asSharedFlow()

    // Original flow of all articles from the repository
    private val _allArticles: StateFlow<List<Article>> = repository.getAllArticles()
        .catch { e ->
            Log.e("ArticleListVM", "Error collecting all articles", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // StateFlow for the search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // StateFlow for articles filtered by the search query
    val searchedArticles: StateFlow<List<Article>> = _searchQuery
        .debounce(300L) // Debounce to avoid too frequent filtering
        .combine(_allArticles) { query, articlesList ->
            if (query.isBlank()) {
                articlesList
            } else {
                articlesList.filter { article ->
                    article.title.contains(query, ignoreCase = true) ||
                            article.content.contains(query, ignoreCase = true) // Optionally search in content
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList() // Or _allArticles.value if appropriate and safe
        )

    private val _currentUserArticles = MutableStateFlow<List<Article>>(emptyList())
    val currentUserArticles: StateFlow<List<Article>> = _currentUserArticles.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>("Memuat nama...")
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>("Memuat email...")
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<LogoutEvent>()
    val logoutEvent: SharedFlow<LogoutEvent> = _logoutEvent.asSharedFlow()

    sealed class LogoutEvent {
        data object Success : LogoutEvent()
        data class Error(val message: String?) : LogoutEvent()
    }

    init {
        Log.d("ArticleListVM", "ViewModel initializing...")
        refreshAllArticles() // This will populate _allArticles
        loadUserProfileDataAndArticles()
        Log.d("ArticleListVM", "ViewModel initialization complete.")
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun loadUserProfileDataAndArticles() {
        viewModelScope.launch {
            try {
                Log.d("ArticleListVM", "Attempting to load current user profile and articles.")
                _currentUserName.value = repository.getCurrentUserName()
                _currentUserEmail.value = repository.getCurrentUserEmail()
                val userId = repository.getCurrentUserId()
                _currentUserId.value = userId

                Log.d("ArticleListVM", "Current User ID: $userId, Name: ${_currentUserName.value}, Email: ${_currentUserEmail.value}")

                if (userId != null) {
                    repository.getArticlesByAuthorId(userId)
                        .catch { e ->
                            Log.e("ArticleListVM", "Error collecting user articles for User ID: $userId", e)
                            _currentUserArticles.value = emptyList()
                        }
                        .collect { userArticles ->
                            _currentUserArticles.value = userArticles
                            Log.d("ArticleListVM", "Collected ${userArticles.size} articles for User ID: $userId")
                        }
                } else {
                    _currentUserArticles.value = emptyList()
                    Log.w("ArticleListVM", "User ID not found, cannot fetch user articles.")
                }
            } catch (e: Exception) {
                Log.e("ArticleListVM", "Exception in loadUserProfileDataAndArticles", e)
                _currentUserArticles.value = emptyList()
                _currentUserName.value = "Gagal memuat nama"
                _currentUserEmail.value = "Gagal memuat email"
            }
        }
    }

    fun refreshAllArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("ArticleListVM", "Refreshing all articles...")
            try {
                repository.syncArticles()
                Log.d("ArticleListVM", "Sync articles successful.")
            } catch (e: Exception) {
                Log.e("ArticleListVM", "Failed to sync articles", e)
            } finally {
                _isLoading.value = false
                Log.d("ArticleListVM", "Refresh all articles finished.")
            }
        }
    }

    fun refreshCurrentUserArticles() {
        Log.d("ArticleListVM", "Refreshing current user profile and articles...")
        loadUserProfileDataAndArticles()
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteArticle(article)
            _deleteResult.emit(result)
            if (result.isSuccess) {
                Log.d("ArticleListVM", "Article ${article.id} deleted, flows should update.")
            } else {
                Log.e("ArticleListVM", "Failed to delete article: ${result.exceptionOrNull()?.message}")
            }
            _isLoading.value = false
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("ArticleListVM", "Performing logout...")
            val result = repository.logoutUser()
            if (result.isSuccess) {
                _logoutEvent.emit(LogoutEvent.Success)
                _currentUserId.value = null
                _currentUserName.value = null
                _currentUserEmail.value = null
                _currentUserArticles.value = emptyList()
                Log.d("ArticleListVM", "Logout successful, ViewModel user states cleared.")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Logout gagal"
                _logoutEvent.emit(LogoutEvent.Error(errorMsg))
                Log.e("ArticleListVM", "Logout failed: $errorMsg")
            }
            _isLoading.value = false
        }
    }
}