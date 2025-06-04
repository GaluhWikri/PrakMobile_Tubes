package com.example.tubespm.ui.uiscreens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.tubespm.data.model.Article
import com.example.tubespm.data.model.Comment
import com.example.tubespm.ui.viewmodels.ArticleDetailViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val article by viewModel.article.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val isLoadingArticle by viewModel.isLoadingArticle.collectAsState()
    val isLoadingComments by viewModel.isLoadingComments.collectAsState()
    var showDeleteCommentDialog by remember { mutableStateOf<Comment?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.operationResult.collectLatest { result ->
            if (result.isFailure) {
                Toast.makeText(context, "Operasi gagal: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
            // Sukses biasanya sudah dihandle dengan refresh data, toast mungkin tidak perlu jika UI update
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Judul di header gambar, bukan di TopAppBar */ },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement bookmark action */ }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                    }
                    IconButton(onClick = { /* TODO: Implement share action */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // Membuat TopAppBar transparan agar gambar header terlihat
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = Color.White, // Warna ikon di atas gambar header
                    actionIconContentColor = Color.White      // Warna ikon di atas gambar header
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
            // Hapus padding atas dari Scaffold karena TopAppBar transparan dan konten akan di bawahnya
            // .padding(paddingValues) -> ini akan memberi padding untuk TopAppBar yang solid
        ) {
            if (isLoadingArticle) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                article?.let { art ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // Tidak perlu contentPadding untuk TopAppBar jika sudah dihandle oleh item pertama (ArticleHeaderImage)
                        // contentPadding = paddingValues // Ini akan menambahkan padding lagi jika TopAppBar solid
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            ArticleHeaderImage(article = art, topPadding = paddingValues.calculateTopPadding())
                        }

                        item {
                            ArticleContentCard(article = art, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Komentar",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            ModernCommentInputSection(
                                commentText = commentText,
                                onCommentTextChanged = viewModel::updateCommentText,
                                onSendComment = {
                                    if (commentText.isNotBlank()) {
                                        viewModel.addComment()
                                    } else {
                                        Toast.makeText(context, "Komentar tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                isSending = isLoadingComments,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (isLoadingComments && comments.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        } else if (comments.isEmpty()) {
                            item {
                                Text(
                                    "Belum ada komentar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp).fillMaxWidth()
                                )
                            }
                        } else {
                            items(comments, key = { it.id }) { comment ->
                                ModernCommentItem(
                                    comment = comment,
                                    onDelete = { showDeleteCommentDialog = comment },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                        item {
                            // Padding bawah untuk konten terakhir agar tidak terlalu mepet
                            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 16.dp))
                        }
                    }
                } ?: Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues), // Beri padding jika artikel tidak ada
                    contentAlignment = Alignment.Center
                ) {
                    Text("Artikel tidak ditemukan atau gagal dimuat.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    showDeleteCommentDialog?.let { commentToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteCommentDialog = null },
            title = { Text("Hapus Komentar") },
            text = { Text("Apakah Anda yakin ingin menghapus komentar ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteComment(commentToDelete)
                        showDeleteCommentDialog = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCommentDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ArticleHeaderImage(article: Article, topPadding: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp) // Sedikit lebih tinggi untuk memberi ruang pada TopAppBar transparan
        // .padding(top = topPadding) // Padding untuk TopAppBar transparan jika diperlukan, atau handle dengan Z-index
    ) {
        AsyncImage(
            model = article.imageUrl ?: "", // Beri fallback jika null untuk menghindari error Coil
            contentDescription = "Gambar Artikel",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Gradient overlay untuk membuat teks lebih terbaca
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 350.dp.value * 0.4f // Mulai gradient dari 40% tinggi
                    )
                )
        )
        // Konten teks di atas gambar
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp), // Padding untuk konten teks
            verticalArrangement = Arrangement.spacedBy(6.dp) // Jarak antar elemen teks
        ) {
            // --- TAMPILAN KATEGORI ---
            if (article.category.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f), // Warna tag kategori
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp) // Jarak bawah tag ke judul
                ) {
                    Text(
                        text = article.category.uppercase(Locale.getDefault()), // Kategori dalam huruf besar
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            // --- AKHIR TAMPILAN KATEGORI ---

            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 3, // Izinkan hingga 3 baris untuk judul panjang
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle, // Atau ikon avatar jika ada URL
                    contentDescription = "Author Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp) // Ukuran ikon author sedikit lebih kecil
                )
                Text(
                    text = article.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = formatRelativeTime(article.createdAt), // Menggunakan fungsi format waktu relatif
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ArticleContentCard(article: Article, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = (-20).dp), // Tarik kartu sedikit ke atas agar menutupi bagian bawah header
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp), // Hanya sudut atas yang rounded
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background // Atau surface, sesuaikan dengan desain
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Hilangkan shadow jika ditarik ke atas
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp) // Padding konten dalam kartu
        ) {
            // Judul bisa dihilangkan dari sini jika sudah sangat jelas di header
            // Text(
            //     text = article.title,
            //     style = MaterialTheme.typography.headlineSmall, // Ukuran lebih kecil jika diulang
            //     fontWeight = FontWeight.Bold,
            //     color = MaterialTheme.colorScheme.onBackground
            // )
            // Spacer(modifier = Modifier.height(4.dp))
            // Row(verticalAlignment = Alignment.CenterVertically){ // Info tambahan jika perlu
            //    Icon(Icons.Default.CalendarToday, contentDescription = "Tanggal", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            //    Spacer(Modifier.width(4.dp))
            //    Text(
            //        text = "Dipublikasikan: ${SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(article.createdAt)}",
            //        style = MaterialTheme.typography.bodySmall,
            //        color = MaterialTheme.colorScheme.onSurfaceVariant
            //    )
            // }
            // Spacer(modifier = Modifier.height(16.dp))
            // HorizontalDivider()
            // Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp), // Line height lebih besar untuk kenyamanan membaca
                color = MaterialTheme.colorScheme.onBackground // Atau onSurface
            )
        }
    }
}

// Helper function untuk format waktu relatif (disederhanakan)
fun formatRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "Baru saja"
        minutes < 60 -> "$minutes menit lalu"
        hours < 24 -> "$hours jam lalu"
        days < 7 -> "$days hari lalu"
        else -> SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(date)
    }
}

// Composable ModernCommentInputSection dan ModernCommentItem tetap sama
@Composable
fun ModernCommentInputSection(
    commentText: String,
    onCommentTextChanged: (String) -> Unit,
    onSendComment: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChanged,
                label = { Text("Tulis komentar...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSendComment,
                enabled = commentText.isNotBlank() && !isSending,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim")
                }
            }
        }
    }
}

@Composable
fun ModernCommentItem(
    comment: Comment,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Atau surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.authorName.take(1).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = comment.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) // Format lebih lengkap
                                .format(comment.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box { // Untuk menu (titik tiga)
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu Komentar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                        // Anda bisa menambahkan aksi lain di sini jika perlu
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4 // Sedikit lebih lega
            )
        }
    }
}