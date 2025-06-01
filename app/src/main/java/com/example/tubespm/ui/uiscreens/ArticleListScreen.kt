package com.example.tubespm.ui.uiscreens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan // Import yang benar
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
fun ArticleListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
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
        viewModel.refresh()
        viewModel.deleteResult.collectLatest { result ->
            if (result.isFailure) {
                Toast.makeText(context, "Gagal menghapus artikel: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val bottomBarHeight = 72.dp
    val fabSize = 64.dp
    val fabOffset = (bottomBarHeight / 2) + (fabSize / 4)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Discover",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    "New articles",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CategoryChips(
                    categories = listOf("Comedy", "Adventure", "Cosmos", "Winter Sports", "Nature", "Tech"),
                    selectedCategory = "Cosmos",
                    onCategorySelected = { /* TODO: Handle category selection */ }
                )
            }
        },
        floatingActionButton = {
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
                Icon(Icons.Filled.Add, contentDescription = "Buat Artikel", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomNavigationBar(
                height = bottomBarHeight,
                onHomeClick = { /* TODO: Navigasi Home */ },
                onChatClick = { /* TODO: Navigasi Chat */ },
                onNotificationsClick = { /* TODO: Navigasi Notifikasi */ },
                onSettingsClick = { /* TODO: Navigasi Settings */ },
                onLogoutClick = { viewModel.performLogout() }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues) // Menggunakan paddingValues dari Scaffold
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(
                top = 8.dp, // Jarak dari CategoryChips ke item grid pertama
                bottom = (fabSize / 2) + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading && articles.isEmpty()) {
                // *** PERBAIKAN DI SINI ***
                item(span = { GridItemSpan(maxLineSpan) }) { // Menggunakan maxLineSpan dari scope
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (articles.isEmpty() && !isLoading) {
                // *** PERBAIKAN DI SINI ***
                item(span = { GridItemSpan(maxLineSpan) }) { // Menggunakan maxLineSpan dari scope
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada artikel",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(articles, key = { article -> article.id }) { article ->
                    DiscoverArticleCard(
                        article = article,
                        onClick = { onNavigateToDetail(article.id) }
                    )
                }
            }
        }
    }
}

// Composable CategoryChips, Chip, DiscoverArticleCard, BottomNavigationBar, dan BottomNavItem tetap sama
// ... (Kode untuk Composable lain tidak berubah dari respons sebelumnya)
@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            Chip(
                label = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundAlpha = if (isSelected) 1f else 0.1f
    val contentAlpha = if (isSelected) 1f else 0.7f
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = backgroundAlpha),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
        contentColor = textColor.copy(alpha = contentAlpha)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun DiscoverArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (article.imageUrl != null && article.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = article.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.BrokenImage,
                            contentDescription = "No image",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 18.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = SimpleDateFormat("dd MMM, yy", Locale.getDefault()).format(article.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "5 min read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    height: androidx.compose.ui.unit.Dp,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var selectedItem by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(elevation = 8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Filled.Home,
                label = "Home",
                isSelected = selectedItem == 0,
                onClick = { selectedItem = 0; onHomeClick() }
            )
            BottomNavItem(
                icon = Icons.Filled.ChatBubbleOutline,
                label = "Chat",
                isSelected = selectedItem == 1,
                onClick = { selectedItem = 1; onChatClick() }
            )

            Spacer(modifier = Modifier.width(64.dp))

            BottomNavItem(
                icon = Icons.Filled.NotificationsNone,
                label = "Notif",
                isSelected = selectedItem == 2,
                onClick = { selectedItem = 2; onNotificationsClick() }
            )
            BottomNavItem(
                icon = Icons.Filled.Tune,
                label = "Settings",
                isSelected = selectedItem == 3,
                onClick = { selectedItem = 3; onSettingsClick() }
            )
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(26.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontSize = 10.sp
            )
        }
    }
}
