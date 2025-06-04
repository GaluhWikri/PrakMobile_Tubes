<?php

namespace App\Http\Controllers;

use App\Models\Article;
use Illuminate\Http\Request;

class ArticleController extends Controller
{

    public function index(Request $request) // Add Request $request
    {
        $query = Article::with('user', 'comments');

        // Filter by category if 'kategori' parameter is present and not 'All' or empty
        if ($request->has('kategori') && $request->kategori != 'All' && $request->kategori != '') {
            $query->where('kategori', $request->kategori);
        }

        $articles = $query->orderBy('created_at', 'desc')->get(); // Added default ordering
        return response()->json($articles);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'judul' => 'required|string|max:255', // Added max length
            'gambar' => 'nullable|string|url', // Consider URL validation if it's a URL
            'tanggal' => 'required|date_format:Y-m-d', // Specify format
            'penulis' => 'required|string|max:255',
            'kategori' => 'required|string|in:Nature,Photography,Art,Tech', // Validate category
            'isi' => 'required|string',
            'author_id' => 'required|exists:users,id', // Ensure author_id is required
        ]);

        $article = Article::create($validated);

        return response()->json($article->load('user', 'comments'), 201); // Return with relations
    }

    public function show(Article $article)
    {
        return $article->load('user', 'comments');
    }

    public function update(Request $request, Article $article)
    {
        // Add validation for update as well, similar to store
        $validated = $request->validate([
            'judul' => 'sometimes|required|string|max:255',
            'gambar' => 'nullable|string|url',
            'tanggal' => 'sometimes|required|date_format:Y-m-d',
            'penulis' => 'sometimes|required|string|max:255',
            'kategori' => 'sometimes|required|string|in:Nature,Photography,Art,Tech',
            'isi' => 'sometimes|required|string',
            // author_id typically shouldn't change on update unless intended
        ]);

        $article->update($validated);
        return $article->load('user', 'comments'); // Return with relations
    }

    public function destroy(Article $article)
    {
        $article->delete();
        return response()->json(['message' => 'Deleted']);
    }
}