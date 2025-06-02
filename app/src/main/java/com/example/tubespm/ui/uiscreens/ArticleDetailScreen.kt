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
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Title removed as per new design */ },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implement bookmark action */ }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                    }
                    IconButton(onClick = { /* TODO: Implement share action */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Or transparent if image goes under
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Changed to a single background color
                .padding(paddingValues)
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
                        verticalArrangement = Arrangement.spacedBy(0.dp) // Adjusted spacing
                    ) {
                        item {
                            ArticleHeaderImage(article = art)
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
                            Spacer(modifier = Modifier.height(16.dp)) // Add some padding at the bottom
                        }
                    }
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
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
fun ArticleHeaderImage(article: Article) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Adjust height as needed
    ) {
        AsyncImage(
            model = article.imageUrl ?: "", // Provide a fallback drawable if needed
            contentDescription = "Article Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Scrim overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 0.6f * 300.dp.value // Start scrim from 60% of height
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Tag
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    // Placeholder: Use article.category if available, otherwise a placeholder
                    text = "Technology", // article.categoryName ?: "Technology",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Prominent Title on Image (Using article.title for this example, could be a different field)
            Text(
                text = article.title, // Or a specific bannerTitle field from Article
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder for author avatar
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Author Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    // Placeholder: Use article.authorName if available
                    text = "Author Name", // article.authorName ?: "Unknown Author",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatRelativeTime(article.createdAt),
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
            .shadow(
                elevation = 0.dp, // Image has no card shadow, this is the content card
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) // Rounded top corners
            )
            .offset(y = (-16).dp), // Pull card up to overlap slightly or sit under rounded image corners
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp) // More top padding
        ) {
            Text(
                text = article.title, // This is the "How Artificial Intelligence Will Shape the Next Decade" title
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp)) // Increased space

            // If you want to show a formatted date here as well (optional)
            // Row(
            //     verticalAlignment = Alignment.CenterVertically,
            //     horizontalArrangement = Arrangement.spacedBy(6.dp)
            // ) {
            //     Icon(
            //         Icons.Default.CalendarToday,
            //         contentDescription = "Tanggal publikasi",
            //         modifier = Modifier.size(16.dp),
            //         tint = MaterialTheme.colorScheme.onSurfaceVariant
            //     )
            //     Text(
            //         text = "Dipublikasikan: ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(article.createdAt)}",
            //         style = MaterialTheme.typography.bodySmall,
            //         color = MaterialTheme.colorScheme.onSurfaceVariant
            //     )
            // }
            // Spacer(modifier = Modifier.height(12.dp))
            // HorizontalDivider(
            //     thickness = 1.dp,
            //     color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            // )
            // Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// Helper function for relative time (simplified)
fun formatRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(date)
    }
}


// ModernCommentInputSection, ModernCommentItem remain the same as in your existing code.
// Ensure they are available in this file or imported correctly.

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
            .padding(vertical = 4.dp) // Keep some vertical padding between comment cards
            .shadow(
                elevation = 1.dp, // Reduced shadow
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Or surfaceContainerLow
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
                            text = comment.authorName.take(1).uppercase(),
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
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                .format(comment.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box {
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            )
        }
    }
}