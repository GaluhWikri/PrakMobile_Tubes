package com.example.tubespm.ui.uiscreens

import android.util.Log
import android.widget.Toast
// Import untuk animasi dan komponen UI lainnya tetap ada
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.tubespm.data.model.Article
import com.example.tubespm.ui.viewmodels.ArticleListViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userArticles by viewModel.currentUserArticles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val currentUserId by viewModel.currentUserId.collectAsState()
    val userName by viewModel.currentUserName.collectAsState()
    val userEmail by viewModel.currentUserEmail.collectAsState()

    var showDeleteArticleDialog by remember { mutableStateOf<Article?>(null) }
    var showLogoutConfirmationDialog by remember { mutableStateOf(false) }
    var isLoggedOut by remember { mutableStateOf(currentUserId == null) }

    LaunchedEffect(Unit) {
        if (viewModel.currentUserId.value == null && !isLoggedOut) {
            Log.d("ProfileScreen", "Initial check: User ID is null, marking as logged out.")
            isLoggedOut = true
        }
        if (currentUserId != null) { // Hanya refresh jika user login
            viewModel.refreshCurrentUserArticles()
        }

        viewModel.deleteResult.collectLatest { result ->
            if (result.isFailure) {
                Toast.makeText(context, "Gagal menghapus artikel: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            } else if (result.isSuccess) {
                Toast.makeText(context, "Artikel berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.logoutEvent.collectLatest { event ->
            when (event) {
                is ArticleListViewModel.LogoutEvent.Success -> {
                    Toast.makeText(context, "Logout Berhasil", Toast.LENGTH_SHORT).show()
                    if (!isLoggedOut) isLoggedOut = true
                }
                is ArticleListViewModel.LogoutEvent.Error -> {
                    Toast.makeText(context, event.message ?: "Logout Gagal", Toast.LENGTH_LONG).show()
                    if (isLoggedOut) isLoggedOut = false
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        Log.d("ProfileScreen", "currentUserId changed to: $currentUserId, current isLoggedOut: $isLoggedOut")
        if (currentUserId == null && !isLoggedOut) {
            isLoggedOut = true
            Log.d("ProfileScreen", "currentUserId became null, marked as logged out.")
        }
    }


    val bottomBarHeight = 72.dp
    val fabSize = 64.dp
    val fabOffset = (bottomBarHeight / 2) + (fabSize / 4)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLoggedOut) "Anda Telah Logout" else "Profil Saya", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (!isLoggedOut) {
                        IconButton(onClick = { showLogoutConfirmationDialog = true }) {
                            Icon(Icons.Filled.Logout, "Logout", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = {
                            if (currentUserId != null) viewModel.refreshCurrentUserArticles()
                        }) {
                            Icon(Icons.Filled.Refresh, "Refresh Data", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isLoggedOut) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(fabSize)
                        .offset(y = fabOffset)
                        .zIndex(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Buat Artikel Baru", modifier = Modifier.size(32.dp))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            if (!isLoggedOut) {
                // Memanggil BottomNavigationBar yang didefinisikan di ArticleListScreen.kt
                // atau file komponen bersama lainnya dalam package yang sama.
                BottomNavigationBar(
                    height = bottomBarHeight,
                    selectedItemIndex = 1, // Profile (0: Home, 1: Profile)
                    onHomeClick = onNavigateToHome,
                    onProfileClick = { /* Already on Profile */ },
                    onLogoutClick = { showLogoutConfirmationDialog = true }
                )
            } else {
                Surface(modifier = Modifier.fillMaxWidth().height(bottomBarHeight), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Button(onClick = onNavigateToLogin, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.Login, contentDescription = "Login")
                            Spacer(Modifier.width(8.dp))
                            Text("Login untuk Melanjutkan")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if(isLoggedOut) Arrangement.Center else Arrangement.Top
        ) {
            if (isLoggedOut) {
                Spacer(modifier = Modifier.height(32.dp))
                Icon(
                    imageVector = Icons.Filled.NotInterested,
                    contentDescription = "Logged Out",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Anda telah berhasil logout.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Silakan login kembali untuk melanjutkan.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Login, contentDescription = "Login kembali")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOGIN KEMBALI", fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile Icon",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userName ?: "Memuat nama...",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userEmail ?: "Memuat email...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLoading && userArticles.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (userArticles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Anda belum membuat artikel.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = if (!isLoggedOut) bottomBarHeight + fabSize / 2 + 8.dp else 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userArticles, key = { it.id }) { article ->
                            UserArticleCard(
                                article = article,
                                onCardClick = { onNavigateToDetail(article.id) },
                                onEditClick = { onNavigateToEdit(article.id) },
                                onDeleteClick = { showDeleteArticleDialog = article }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmationDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.performLogout()
                        showLogoutConfirmationDialog = false
                    }
                ) {
                    Text("Ya", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmationDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }

    showDeleteArticleDialog?.let { articleToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteArticleDialog = null },
            title = { Text("Hapus Artikel") },
            text = { Text("Apakah Anda yakin ingin menghapus artikel '${articleToDelete.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArticle(articleToDelete)
                        showDeleteArticleDialog = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteArticleDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun UserArticleCard(
    article: Article,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            article.imageUrl?.takeIf { it.isNotBlank() }?.let {
                AsyncImage(
                    model = it,
                    contentDescription = article.title,
                    modifier = Modifier
                        .size(90.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(90.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.BrokenImage, "No Image", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = try {
                        "Dibuat: ${SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(article.createdAt)}"
                    } catch (e: Exception) {
                        "Tanggal tidak valid"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Artikel", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus Artikel", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

