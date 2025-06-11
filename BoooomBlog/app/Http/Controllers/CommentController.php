<?php

namespace App\Http\Controllers;

use App\Models\Article; // Import Article model
use App\Models\Comment;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth; // Import Auth facade

class CommentController extends Controller
{
    /**
     * Display a listing of the comments for a specific article.
     */
    public function indexForArticle(Article $article)
    {
        // Eager load the user relationship for each comment
        $comments = $article->comments()->with('user:id,name,email')->latest()->get();
        return response()->json($comments);
    }

    /**
     * Store a newly created comment in storage.
     */
    public function store(Request $request, Article $article)
    {
        $validated = $request->validate([
            'body' => 'required|string',
            // 'name' => 'nullable|string|max:255', // If you want to allow non-authenticated users to provide a name
        ]);

        $comment = $article->comments()->create([
            'body' => $validated['body'],
            'user_id' => Auth::id(), // Associate with the logged-in user
            // 'name' => $request->input('name', Auth::check() ? Auth::user()->name : 'Anonymous'), // Example if allowing guest names
        ]);

        // Load the user relationship before returning the response
        $comment->load('user:id,name,email');

        return response()->json($comment, 201);
    }

    /**
     * Display the specified comment.
     */
    public function show(Comment $comment)
    {
        // Load the user and article relationships
        return $comment->load('user:id,name,email', 'article:id,judul');
    }

    /**
     * Update the specified comment in storage.
     */
    public function update(Request $request, Comment $comment)
    {
        // Authorization: Ensure the authenticated user owns the comment
        if ($request->user()->id !== $comment->user_id && !$request->user()->isAdmin()) { // Assuming you might have an isAdmin check
            return response()->json(['message' => 'Unauthorized. You can only edit your own comments.'], 403);
        }

        $validated = $request->validate([
            'body' => 'required|string',
        ]);

        $comment->update($validated);
        $comment->load('user:id,name,email'); // Reload user information

        return response()->json($comment);
    }

    /**
     * Remove the specified comment from storage.
     */
    public function destroy(Request $request, Comment $comment) // Added Request for user access
    {
        // Authorization: Ensure the authenticated user owns the comment or is an admin
        // You might want to add an admin check here as well, e.g., if ($request->user()->isAdmin())
        if ($request->user()->id !== $comment->user_id && !$request->user()->isAdmin()) { // Assuming you might have an isAdmin check
            return response()->json(['message' => 'Unauthorized. You can only delete your own comments.'], 403);
        }

        $comment->delete();

        return response()->json(['message' => 'Comment deleted successfully']);
    }
}