<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\ArticleController;
use App\Http\Controllers\CommentController; // Make sure this is imported
use App\Http\Controllers\AuthController;
use App\Http\Controllers\UserController;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider and all of them will
| be assigned to the "api" middleware group. Make something great!
|
*/

// Rute yang tidak memerlukan autentikasi
Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

// Article Routes
Route::get('/articles', [ArticleController::class, 'index']);
Route::get('/articles/{article}', [ArticleController::class, 'show']);

// Comment Routes (Publicly viewable comments for an article)
Route::get('/articles/{article}/comments', [CommentController::class, 'indexForArticle']); // List comments for a specific article


// Rute yang memerlukan otentikasi (Dilindungi Sanctum)
Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);

    // Endpoint untuk mendapatkan data user yang sedang login
    Route::get('/user', function (Request $request) {
        return $request->user();
    });

    // Endpoint untuk User (DIPROTEKSI)
    Route::get('/users', [UserController::class, 'index'])->name('users.index');
    Route::get('/users/{user}', [UserController::class, 'show'])->name('users.show');

    // Endpoint untuk membuat, mengupdate, menghapus artikel (DIPROTEKSI)
    Route::post('/articles', [ArticleController::class, 'store'])->name('articles.store');
    Route::put('/articles/{article}', [ArticleController::class, 'update'])->name('articles.update');
    Route::patch('/articles/{article}', [ArticleController::class, 'update']);
    Route::delete('/articles/{article}', [ArticleController::class, 'destroy'])->name('articles.destroy');

    // Comment Routes (Authenticated actions)
    Route::post('/articles/{article}/comments', [CommentController::class, 'store']); // Create a comment for an article
    Route::get('/comments/{comment}', [CommentController::class, 'show']);          // Show a specific comment (can be protected or public based on needs)
    Route::put('/comments/{comment}', [CommentController::class, 'update']);        // Update a comment
    Route::patch('/comments/{comment}', [CommentController::class, 'update']);      // Update a comment (alternative method)
    Route::delete('/comments/{comment}', [CommentController::class, 'destroy']);    // Delete a comment
});