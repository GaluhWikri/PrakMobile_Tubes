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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.tubespm.ui.theme.BlogTheme
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
    val category by viewModel.category.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val availableCategories = viewModel.availableCategories

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
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
        showSuccessToast = false
    }

    showErrorToast?.let {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        showErrorToast = null
    }

    BlogTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditing) "Edit Artikel" else "Buat Artikel Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        Button( // TOMBOL SIMPAN
                            onClick = { viewModel.saveArticle() },
                            enabled = !isLoading && title.isNotBlank() && content.isNotBlank() && category.isNotBlank(),
                            modifier = Modifier.padding(end = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                // --- PERUBAHAN WARNA TOMBOL SIMPAN ---
                                containerColor = MaterialTheme.colorScheme.primary, // Menggunakan warna primary
                                contentColor = MaterialTheme.colorScheme.onPrimary    // Warna teks/ikon di tombol Simpan
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Filled.Done, contentDescription = "Simpan")
                                Spacer(Modifier.width(6.dp))
                                Text("Simpan")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary // Disesuaikan agar ikon action juga onPrimary
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ModernTextField(
                        value = title,
                        onValueChange = viewModel::updateTitle,
                        label = "Judul Artikel",
                        leadingIcon = Icons.Filled.Title,
                        singleLine = false,
                        maxLines = 3
                    )
                }

                item { // Dropdown Kategori
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            leadingIcon = { Icon(Icons.Filled.Category, contentDescription = "Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                // --- PERUBAHAN WARNA DROPDOWN KATEGORI (FOKUS) ---
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Warna border saat fokus (primary)
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,    // Warna label saat fokus (primary)
                                cursorColor = MaterialTheme.colorScheme.primary,          // Warna kursor (primary)
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary, // Warna ikon leading saat fokus (primary)
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh) // Latar belakang menu dropdown
                        ) {
                            availableCategories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        viewModel.updateCategory(selectionOption)
                                        categoryDropdownExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
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
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .shadow(4.dp, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Preview Gambar Artikel",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                item {
                    ModernTextField(
                        value = content,
                        onValueChange = viewModel::updateContent,
                        label = "Konten Artikel",
                        leadingIcon = Icons.Filled.Notes,
                        modifier = Modifier.defaultMinSize(minHeight = 200.dp),
                        singleLine = false,
                        isContentField = true
                    )
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }
}

// Composable ModernTextField tetap sama, menggunakan primary color untuk fokus
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isContentField: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary // Fokus menggunakan primary
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary, // Fokus menggunakan primary
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            focusedLabelColor = MaterialTheme.colorScheme.primary, // Fokus menggunakan primary
            cursorColor = MaterialTheme.colorScheme.primary, // Fokus menggunakan primary
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary, // Fokus menggunakan primary
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = if (isContentField) 24.sp else MaterialTheme.typography.bodyLarge.lineHeight
        )
    )
}