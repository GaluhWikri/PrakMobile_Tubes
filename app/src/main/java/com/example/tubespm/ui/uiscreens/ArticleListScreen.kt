package com.example.tubespm.ui.uiscreens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.tubespm.data.model.Article
import com.example.tubespm.ui.viewmodels.ArticleListViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToLogin: () -> Unit, // Callback untuk navigasi ke Login setelah logout
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Article?>(null) }
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) { // Menggunakan Unit agar hanya dijalankan sekali saat komposisi awal
        viewModel.logoutEvent.collectLatest { event ->
            when (event) {
                is ArticleListViewModel.LogoutEvent.Success -> {
                    Toast.makeText(context, "Logout Berhasil", Toast.LENGTH_SHORT).show()
                    onNavigateToLogin()
                }
                is ArticleListViewModel.LogoutEvent.Error -> {
                    Toast.makeText(context, event.message ?: "Logout Gagal", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh() // Pindahkan refresh ke LaunchedEffect terpisah jika hanya ingin load awal
        viewModel.deleteResult.collectLatest { result -> // Gunakan collectLatest juga di sini
            if (result.isFailure) {
                Toast.makeText(context, "Gagal menghapus artikel: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
            // Refresh sudah dihandle di dalam deleteArticle pada ViewModel jika sukses
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BOOOOM BLOG",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary, // Tambahkan jika ada nav icon
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary // Warna untuk action icons
                ),
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            // tint = MaterialTheme.colorScheme.onPrimary // Sudah diatur oleh actionIconContentColor
                        )
                    }
                    // Tombol Logout
                    IconButton(onClick = {
                        // Opsional: Tambahkan dialog konfirmasi sebelum logout
                        viewModel.performLogout()
                    }) {
                        Icon(
                            Icons.Default.Logout, // atau Icons.Filled.ExitToApp
                            contentDescription = "Logout",
                            // tint = MaterialTheme.colorScheme.onPrimary // Sudah diatur oleh actionIconContentColor
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Buat Artikel") },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary // Pastikan kontras
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (isLoading && articles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Memuat artikel...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (articles.isEmpty() && !isLoading) { // Tambahkan !isLoading untuk kondisi empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inbox, // Ganti ikon jika lebih sesuai
                            contentDescription = "Tidak ada artikel",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Belum ada artikel",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Mulai dengan membuat artikel pertama Anda!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(articles, key = { article -> article.id }) { article -> // Tambahkan key untuk performa
                        ModernArticleCard(
                            article = article,
                            onClick = { onNavigateToDetail(article.id) },
                            onEdit = { onNavigateToEdit(article.id) },
                            onDelete = { showDeleteDialog = article }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { article ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Artikel") },
            text = { Text("Apakah Anda yakin ingin menghapus artikel '${article.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArticle(article)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ModernArticleCard Composable (Tidak ada perubahan signifikan, pastikan warna ikon kontras)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernArticleCard(
    article: Article,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Box {
                if (article.imageUrl != null && article.imageUrl.isNotBlank()) { // Cek isNotBlank
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = "Article Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay Gradient
                    Box(
                        modifier = Modifier
                            .matchParentSize() // Sesuaikan dengan ukuran AsyncImage
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 0f, // Mulai dari atas
                                    endY = Float.POSITIVE_INFINITY // Sampai ke bawah
                                )
                            )
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp) // Sesuaikan tinggi jika tidak ada gambar
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.ImageNotSupported, // Ikon jika gambar tidak ada
                            contentDescription = "No image available",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Menu Tombol (tiga titik)
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f), // Sedikit transparan agar tidak terlalu dominan
                                RoundedCornerShape(50) // Bulat sempurna
                            )
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White // Pastikan ikon terlihat di atas background semi-transparan
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
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
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Spasi antar ikon dan teks
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Tanggal",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            // text = article.createdAt.toString(), // Format tanggal jika perlu
                            text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                .format(article.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Tombol "Baca" sederhana
                    TextButton(onClick = onClick) { // Menggunakan onClick dari parameter Card utama
                        Text(
                            text = "Baca Selengkapnya",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            Icons.Default.ArrowForwardIos,
                            contentDescription = "Baca",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}