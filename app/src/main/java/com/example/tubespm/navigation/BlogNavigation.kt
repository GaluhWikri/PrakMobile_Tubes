package com.example.tubespm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tubespm.ui.uiscreens.ArticleDetailScreen
import com.example.tubespm.ui.uiscreens.ArticleListScreen
import com.example.tubespm.ui.uiscreens.CreateEditArticleScreen
import com.example.tubespm.ui.uiscreens.LoginScreen
import com.example.tubespm.ui.uiscreens.RegisterScreen
import com.example.tubespm.ui.uiscreens.ProfileScreen

@Composable
fun BlogNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
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
                        launchSingleTop = true
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
                    navController.popBackStack(navController.graph.id, inclusive = true)
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate("article_list") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack(navController.graph.id, inclusive = true)
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                },
                onNavigateToDetail = { articleId ->
                    navController.navigate("article_detail/$articleId")
                },
                onNavigateToEdit = { articleId ->
                    navController.navigate("edit_article/$articleId")
                },
                onNavigateToCreate = {
                    navController.navigate("create_article")
                }
            )
        }

        composable("article_detail/{articleId}") { backStackEntry ->
            // val articleId = backStackEntry.arguments?.getString("articleId") ?: "" // ViewModel akan mengambil dari SavedStateHandle
            ArticleDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
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

        composable("edit_article/{articleId}") { backStackEntry ->
            // val articleId = backStackEntry.arguments?.getString("articleId") // ViewModel akan mengambil dari SavedStateHandle
            CreateEditArticleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isEditing = true
            )
        }
    }
}