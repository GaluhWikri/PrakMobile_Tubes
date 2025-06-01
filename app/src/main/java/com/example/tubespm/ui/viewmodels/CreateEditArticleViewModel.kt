package com.example.tubespm.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Article // Untuk hasil Result jika repository mengembalikan Article
import com.example.tubespm.data.model.ArticleRequest
import com.example.tubespm.repository.BlogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CreateEditArticleViewModel @Inject constructor(
    private val repository: BlogRepository, // Inject BlogRepository
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String? = savedStateHandle.get<String>("articleId")
    val isEditing = articleId != null

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    // StateFlow untuk kategori, bisa diinisialisasi dari input pengguna nantinya
    private val _category = MutableStateFlow("Teknologi") // Contoh default
    val category: StateFlow<String> = _category.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveResult = MutableSharedFlow<Result<Unit>>()
    val saveResult: SharedFlow<Result<Unit>> = _saveResult.asSharedFlow()

    init {
        if (isEditing && articleId != null) {
            loadArticleForEdit(articleId)
        }
    }

    private fun loadArticleForEdit(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val existingArticle = repository.getArticleById(id)
            existingArticle?.let {
                _title.value = it.title
                _content.value = it.content
                _imageUrl.value = it.imageUrl
                // Jika 'category' disimpan di 'Article' entity, muat juga di sini
                // _category.value = it.category // Contoh
            }
            _isLoading.value = false
        }
    }


    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun updateImageUrl(newImageUrl: String?) {
        _imageUrl.value = newImageUrl
    }

    fun updateCategory(newCategory: String) { // Fungsi untuk update kategori
        _category.value = newCategory
    }

    fun saveArticle() {
        val currentTitle = _title.value
        val currentContent = _content.value
        val currentCategory = _category.value // Ambil kategori saat ini

        if (currentTitle.isBlank() || currentContent.isBlank() || currentCategory.isBlank()) {
            viewModelScope.launch {
                _saveResult.emit(Result.failure(Exception("Judul, Konten, dan Kategori harus diisi")))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            // --- Mengambil data pengguna yang sedang login ---
            val currentUserIdString = repository.getCurrentUserId()
            val currentUserName = repository.getCurrentUserName() ?: "Pengguna Anonim" // Fallback jika nama null

            if (currentUserIdString == null) {
                _saveResult.emit(Result.failure(Exception("Gagal mendapatkan ID pengguna. Silakan coba login ulang.")))
                _isLoading.value = false
                return@launch
            }

            val currentAuthorId: Int
            try {
                currentAuthorId = currentUserIdString.toInt()
            } catch (e: NumberFormatException) {
                _saveResult.emit(Result.failure(Exception("Format ID pengguna tidak valid.")))
                _isLoading.value = false
                return@launch
            }
            // --- Selesai mengambil data pengguna ---

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            // Kategori diambil dari _category.value
            // Tanggal bisa juga dibuat input field jika diinginkan

            val articleRequest = ArticleRequest(
                title = currentTitle,
                content = currentContent,
                imageUrl = _imageUrl.value,
                date = currentDate,
                authorName = currentUserName, // Gunakan nama pengguna yang login
                category = currentCategory,   // Gunakan kategori dari state
                authorId = currentAuthorId    // Gunakan ID pengguna yang login
            )

            val result: Result<Article> = if (isEditing && articleId != null) {
                repository.updateArticle(articleId, articleRequest)
            } else {
                repository.createArticle(articleRequest)
            }

            _isLoading.value = false
            _saveResult.emit(result.map { })
        }
    }
}