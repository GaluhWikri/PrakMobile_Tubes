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

    // Original flow of all articles from the repository (local Room DB)
    private val _allArticles: StateFlow<List<Article>> = repository.getAllArticles()
        .catch { e ->
            Log.e("ArticleListVM", "Error collecting all articles from Room", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All") // Default to "All"
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Articles filtered by BOTH search query AND selected category
    val searchedArticles: StateFlow<List<Article>> =
        combine(
            _searchQuery.debounce(300L), // Debounce search query
            _selectedCategory,
            _allArticles
        ) { query, category, articlesList ->
            // Filter by category first
            val categoryFilteredArticles = if (category == "All") {
                articlesList
            } else {
                articlesList.filter { it.category.equals(category, ignoreCase = true) }
            }

            // Then filter by search query
            if (query.isBlank()) {
                categoryFilteredArticles
            } else {
                categoryFilteredArticles.filter { article ->
                    article.title.contains(query, ignoreCase = true) ||
                            article.authorName.contains(query, ignoreCase = true) ||
                            article.content.contains(query, ignoreCase = true)
                }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
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
        refreshAllArticles(_selectedCategory.value) // Initial load with "All" or current category
        loadUserProfileDataAndArticles()
        Log.d("ArticleListVM", "ViewModel initialization complete.")
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // No need to call refreshAllArticles here;
        // the `searchedArticles` Flow will combine the new query with existing articles.
    }

    fun onCategorySelected(category: String) {
        if (_selectedCategory.value != category) { // Only refresh if category actually changes
            _selectedCategory.value = category
            // Fetch articles for the new category from the server.
            // This will update Room, which in turn updates `_allArticles`,
            // and then `searchedArticles` will recompute.
            refreshAllArticles(category)
        }
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
                            _currentUserArticles.value = emptyList() // Emit empty list on error
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

    // Refreshes articles from the remote server and updates the local database
    fun refreshAllArticles(category: String? = _selectedCategory.value) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("ArticleListVM", "Refreshing articles for category: $category")
            try {
                val effectiveCategory = if (category == "All") null else category
                repository.syncArticles(effectiveCategory) // Pass category to repository for API call
                Log.d("ArticleListVM", "Sync articles successful for category: $category.")
            } catch (e: Exception) {
                Log.e("ArticleListVM", "Failed to sync articles for category: $category", e)
                // Consider emitting an error event/state to the UI here
            } finally {
                _isLoading.value = false
                Log.d("ArticleListVM", "Refresh articles finished for category: $category.")
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
            val result = repository.deleteArticle(article) // Deletes from API and then Room
            _deleteResult.emit(result)
            if (result.isSuccess) {
                Log.d("ArticleListVM", "Article ${article.id} deleted. Room should notify _allArticles.")
                // No explicit refreshAllArticles needed here if Room Flow updates promptly
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
            val result = repository.logoutUser() // Clears SharedPreferences and calls API
            if (result.isSuccess) {
                _logoutEvent.emit(LogoutEvent.Success)
                // Clear local user-specific states
                _currentUserId.value = null
                _currentUserName.value = null
                _currentUserEmail.value = null
                _currentUserArticles.value = emptyList()
                _selectedCategory.value = "All" // Reset category on logout
                _searchQuery.value = ""       // Reset search query
                // Data in _allArticles (Room) will persist unless explicitly cleared,
                // which is generally fine as it's not user-specific.
                // Or you could trigger a refresh for "All" to clear filters.
                // refreshAllArticles("All")
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