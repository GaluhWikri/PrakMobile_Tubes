
<?php
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\ArticleController;
use App\Http\Controllers\CommentController;

Route::apiResource('articles', ArticleController::class);
Route::apiResource('comments', CommentController::class);
Route::get('/users', function() {
    return \App\Models\User::all();
});
