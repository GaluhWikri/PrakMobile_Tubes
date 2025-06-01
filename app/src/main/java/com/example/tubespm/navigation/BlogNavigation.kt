package com.example.tubespm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.uiscreens.ArticleDetailScreen
import com.example.tubespm.ui.uiscreens.ArticleListScreen
import com.example.tubespm.ui.uiscreens.CreateEditArticleScreen
import com.example.tubespm.ui.uiscreens.LoginScreen
import com.example.tubespm.ui.uiscreens.RegisterScreen

@Composable
fun BlogNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login" // Pastikan ini adalah titik awal yang benar
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("article_list") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }


        composable("article_list") {
            ArticleListScreen(
                onNavigateToDetail = { articleId ->
                    navController.navigate("article_detail/$articleId")
                },
                onNavigateToCreate = {
                    navController.navigate("create_article")
                },
                onNavigateToEdit = { articleId ->
                    navController.navigate("edit_article/$articleId")
                },
                onNavigateToLogin = {
                    // Navigasi ke login dan bersihkan semua backstack
                    // sehingga login menjadi halaman root baru.
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true // Hindari membuat instance login baru jika sudah ada di atas stack (meskipun popUpTo seharusnya menangani ini)
                    }
                }
            )
        }

        composable("article_detail/{articleId}") {
            ArticleDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { articleId ->
                    navController.navigate("edit_article/$articleId")
                }
            )
        }

        composable("create_article") {
            CreateEditArticleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isEditing = false
            )
        }

        composable("edit_article/{articleId}") {
            CreateEditArticleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isEditing = true
            )
        }
    }
}