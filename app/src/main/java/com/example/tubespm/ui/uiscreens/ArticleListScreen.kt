package com.example.tubespm.ui.uiscreens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SentimentDissatisfied
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
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToEdit: (String) -> Unit, // Parameter ini ada, pastikan digunakan jika perlu
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val articles by viewModel.searchedArticles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val context = LocalContext.current
    var showLogoutDialogFromHome by remember { mutableStateOf(false) }

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
        viewModel.deleteResult.collectLatest { result ->
            if (result.isFailure) {
                Toast.makeText(context, "Gagal menghapus artikel: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Artikel berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ViewModel.init sudah memanggil refreshAllArticles dengan kategori "All".
    // ViewModel.onCategorySelected juga memanggil refreshAllArticles.
    // LaunchedEffect ini mungkin bisa dihapus jika logika di ViewModel sudah cukup.
    LaunchedEffect(selectedCategory, articles.size, searchQuery) {
        if (articles.isEmpty() && !isLoading && searchQuery.isBlank()) {
            viewModel.refreshAllArticles(selectedCategory)
        }
    }

    val bottomBarHeight = 72.dp
    val fabSize = 64.dp
    val fabOffset = (bottomBarHeight / 2) + (fabSize / 4) // Untuk FAB di tengah

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 8.dp)
            ) {
                Text(
                    "BOOOOOM",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Featured Submissions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search articles...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                CategoryChips(
                    categories = listOf("All", "Nature", "Photography", "Art", "Tech"),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.onCategorySelected(category)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    .offset(y = fabOffset) // Penyesuaian offset untuk FAB yang sedikit naik
                    .zIndex(1f) // Pastikan FAB di atas bottom bar
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Buat Artikel", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomNavigationBar( // Menggunakan implementasi BottomNavigationBar yang ada di file ini
                height = bottomBarHeight,
                selectedItemIndex = 0, // "Home" dipilih secara default
                onHomeClick = {
                    viewModel.onSearchQueryChanged("")
                    viewModel.onCategorySelected("All") // Selalu kembali ke "All" saat home diklik
                },
                onProfileClick = onNavigateToProfile,
                onLogoutClick = { showLogoutDialogFromHome = true } // Parameter ini ada, tapi tidak dipakai oleh item di BottomNavBar ini
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface) // Latar belakang untuk area konten
                .padding(paddingValues) // Menerapkan padding dari Scaffold
                .padding(horizontal = 12.dp), // Padding horizontal untuk grid
            contentPadding = PaddingValues(
                top = 8.dp,
                // Padding bawah untuk memberi ruang bagi FAB yang terangkat dan BottomNavigationBar
                bottom = bottomBarHeight + fabSize / 2 + 16.dp // Sedikit ruang tambahan
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading && articles.isEmpty() && searchQuery.isBlank()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(top=50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (articles.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(top=50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.SentimentDissatisfied, contentDescription = "No articles", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            val message = when {
                                searchQuery.isNotBlank() -> "No articles found for '$searchQuery'"
                                selectedCategory != "All" -> "No articles in '$selectedCategory' category"
                                else -> "Belum ada artikel"
                            }
                            Text(
                                text = message,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
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

    if (showLogoutDialogFromHome) {
        AlertDialog(
            onDismissRequest = { showLogoutDialogFromHome = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.performLogout()
                        showLogoutDialogFromHome = false
                    }
                ) {
                    Text("Ya", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialogFromHome = false }) {
                    Text("Tidak")
                }
            }
        )
    }
}

@Composable
fun BottomNavigationBar(
    height: androidx.compose.ui.unit.Dp,
    selectedItemIndex: Int,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit // Parameter ini ada, tapi tidak terhubung ke item UI di Row bawah
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(elevation = 8.dp, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        color = MaterialTheme.colorScheme.surfaceContainer // Coba ganti ke Color.LightGray jika masih tidak muncul
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = if (selectedItemIndex == 0) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = selectedItemIndex == 0, // "Home" akan terpilih
                onClick = onHomeClick
            )

            Spacer(modifier = Modifier.width(80.dp)) // Spacer untuk FAB

            BottomNavItem(
                icon = if (selectedItemIndex == 1) Icons.Filled.Person else Icons.Outlined.Person,
                label = "Profile",
                isSelected = selectedItemIndex == 1, // "Profile" tidak terpilih awalnya
                onClick = onProfileClick
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
    // Untuk debugging, coba warna yang sangat jelas:
    // val iconColorForTest = if (isSelected) Color.Red else Color.DarkGray
    // val labelColorForTest = if (isSelected) Color.Red else Color.DarkGray


    val animatedScale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f, label = "scaleAnimNavItemFull", animationSpec = tween(durationMillis = 200))
    val animatedAlpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.7f, label = "alphaAnimNavItemFull", animationSpec = tween(durationMillis = 200))

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Tidak ada ripple effect
                onClick = onClick
            )
            .padding(vertical = 8.dp) // Padding vertikal untuk setiap item
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale, alpha = animatedAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor, // Gunakan contentColor atau iconColorForTest untuk debug
            modifier = Modifier.size(if (isSelected) 28.dp else 26.dp)
        )
        // Tampilkan label jika item terpilih
        // AnimatedVisibility bisa jadi penyebab jika ada masalah, untuk tes bisa tampilkan langsung
        if (isSelected) { // Lebih sederhana daripada AnimatedVisibility untuk tes awal
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor, // Gunakan contentColor atau labelColorForTest untuk debug
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Jika ingin mengembalikan AnimatedVisibility:
        /*
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(100, delayMillis = 50)) + slideInVertically(initialOffsetY = {it/2}, animationSpec = tween(200, delayMillis = 50)),
            exit = fadeOut(animationSpec = tween(100))
        ) {
            Column {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        */
    }
}


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
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300), label = "chipBgColorFull"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300), label = "chipTextColorFull"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300), label = "chipBorderColorFull"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        contentColor = textColor
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
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.ImageNotSupported,
                            contentDescription = "No image available",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Author",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = article.authorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = "Tanggal",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = try {
                            SimpleDateFormat("dd MMM, yy", Locale.getDefault()).format(article.createdAt)
                        } catch (e: Exception) {
                            "Invalid date"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}