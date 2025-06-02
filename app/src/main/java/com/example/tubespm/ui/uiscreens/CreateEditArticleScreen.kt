package com.example.tubespm.ui.uiscreens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.tubespm.ui.theme.BlogTheme // Assuming you have a BlogTheme or similar
import com.example.tubespm.ui.viewmodels.CreateEditArticleViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditArticleScreen(
    onNavigateBack: () -> Unit,
    isEditing: Boolean = false,
    viewModel: CreateEditArticleViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val category by viewModel.category.collectAsState() // Assuming you'll add category selection
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var showSuccessToast by remember { mutableStateOf(false) }
    var showErrorToast by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        viewModel.saveResult.collectLatest { result ->
            if (result.isSuccess) {
                showSuccessToast = true
                onNavigateBack()
            } else {
                showErrorToast = result.exceptionOrNull()?.message ?: "Gagal menyimpan artikel."
            }
        }
    }

    if (showSuccessToast) {
        Toast.makeText(context, "Artikel berhasil disimpan!", Toast.LENGTH_SHORT).show()
        showSuccessToast = false // Reset toast state
    }

    showErrorToast?.let {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        showErrorToast = null // Reset toast state
    }

    BlogTheme { // Apply your consistent theme
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditing) "Edit Artikel" else "Buat Artikel Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp // Consistent with other screens
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBackIosNew, // Modern back icon
                                contentDescription = "Kembali",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = { viewModel.saveArticle() },
                            enabled = !isLoading && title.isNotBlank() && content.isNotBlank(),
                            modifier = Modifier.padding(end = 12.dp), // Adjusted padding
                            shape = RoundedCornerShape(8.dp), // Consistent rounding
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary, // Or another accent color
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp), // Slightly larger for visibility
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            } else {
                                Icon(Icons.Filled.Done, contentDescription = "Simpan")
                                Spacer(Modifier.width(6.dp))
                                Text("Simpan")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Consistent TopAppBar color
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background) // Consistent background
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 20.dp), // Added more vertical padding
                verticalArrangement = Arrangement.spacedBy(18.dp) // Increased spacing between elements
            ) {
                item {
                    ModernTextField(
                        value = title,
                        onValueChange = viewModel::updateTitle,
                        label = "Judul Artikel",
                        leadingIcon = Icons.Filled.Title,
                        singleLine = false, // Allow multiline for longer titles if needed
                        maxLines = 3
                    )
                }

                item {
                    ModernTextField(
                        value = imageUrl ?: "",
                        onValueChange = { viewModel.updateImageUrl(it.takeIf { url -> url.isNotBlank() }) },
                        label = "URL Gambar (Opsional)",
                        leadingIcon = Icons.Filled.Image,
                        singleLine = true
                    )
                }

                item {
                    AnimatedVisibility(visible = !imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp) // Slightly taller image preview
                                .clip(RoundedCornerShape(12.dp)) // Consistent rounding
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .shadow(4.dp, RoundedCornerShape(12.dp)), // Subtle shadow
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Preview Gambar Artikel",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop // Crop to fill bounds
                            )
                        }
                    }
                }
                // Placeholder for Category selection if you plan to add it
                // For now, it's hardcoded in the ViewModel
                // item {
                //     // You can add a DropdownMenu or a set of Chips for category selection here
                //     Text("Kategori: ${category}", style = MaterialTheme.typography.titleMedium)
                // }


                item {
                    ModernTextField(
                        value = content,
                        onValueChange = viewModel::updateContent,
                        label = "Konten Artikel",
                        leadingIcon = Icons.Filled.Notes, // More appropriate icon
                        modifier = Modifier.height(350.dp), // Taller content field
                        singleLine = false,
                        isContentField = true
                    )
                }

                item { Spacer(modifier = Modifier.height(60.dp)) } // Space for FAB if it overlaps
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isContentField: Boolean = false // To allow different alignment for content
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp), // Consistent rounding
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = if (isContentField) 24.sp else MaterialTheme.typography.bodyLarge.lineHeight
        ) // Better line height for content
    )
}