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
    private val repository: BlogRepository,
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveResult = MutableSharedFlow<Result<Unit>>() // Diubah ke Unit karena kita hanya peduli sukses/gagal navigasi
    val saveResult: SharedFlow<Result<Unit>> = _saveResult.asSharedFlow()

    init {
        if (isEditing && articleId != null) {
            loadArticleForEdit(articleId)
        }
    }

    private fun loadArticleForEdit(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Mengambil data artikel yang ada untuk diedit
            // Ini akan mengisi field judul, konten, dan imageUrl
            // Field lain seperti tanggal, penulis, kategori, authorId perlu di-handle
            // jika ingin ditampilkan atau dikirim kembali saat update.
            val existingArticle = repository.getArticleById(id) // getArticleById sudah diupdate di repository
            existingArticle?.let {
                _title.value = it.title
                _content.value = it.content
                _imageUrl.value = it.imageUrl
                // Jika Anda ingin mempertahankan field lain (misal: kategori),
                // Anda perlu menyimpannya di ViewModel dan memuatnya di sini.
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

    fun saveArticle() {
        val currentTitle = _title.value
        val currentContent = _content.value

        if (currentTitle.isBlank() || currentContent.isBlank()) {
            viewModelScope.launch {
                _saveResult.emit(Result.failure(Exception("Judul dan Konten harus diisi")))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            // TODO: Anda perlu mendapatkan nilai-nilai ini dari input pengguna di CreateEditArticleScreen.kt
            // Untuk sekarang, kita gunakan placeholder.
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val placeholderAuthorName = "Android User" // Ganti dengan nama pengguna yang sebenarnya
            val placeholderCategory = "Teknologi"      // Ganti dengan kategori yang dipilih pengguna
            val placeholderAuthorId = 1              // Ganti dengan ID pengguna yang login (misalnya dari SharedPreferences atau state user)

            val articleRequest = ArticleRequest(
                title = currentTitle,
                content = currentContent,
                imageUrl = _imageUrl.value,
                date = currentDate, // Seharusnya tanggal yang dipilih pengguna atau tanggal artikel jika diedit
                authorName = placeholderAuthorName,
                category = placeholderCategory,
                authorId = placeholderAuthorId
            )

            val result: Result<Article> = if (isEditing && articleId != null) {
                repository.updateArticle(articleId, articleRequest)
            } else {
                repository.createArticle(articleRequest)
            }

            _isLoading.value = false
            // Mengirim Result.success atau Result.failure berdasarkan hasil operasi repository
            // .map { } akan mengubah Result<Article> menjadi Result<Unit>
            _saveResult.emit(result.map { })
        }
    }
}