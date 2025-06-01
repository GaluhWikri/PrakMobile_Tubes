<?php

namespace App\Http\Controllers;

use App\Models\Article;
use Illuminate\Http\Request;

class ArticleController extends Controller
{

    public function index()
    {
        $articles = Article::with('user', 'comments')->get();
        return response()->json($articles);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'judul' => 'required|string',
            'gambar' => 'nullable|string',
            'tanggal' => 'required|date',
            'penulis' => 'required|string',
            'kategori' => 'required|string',
            'isi' => 'required|string',
            'author_id' => 'exists:users,id',
        ]);

        $article = Article::create($validated);

        return response()->json($article, 201);
    }

    public function show(Article $article)
    {
        return $article->load('user', 'comments');
    }

    public function update(Request $request, Article $article)
    {
        $article->update($request->all());
        return $article;
    }

    public function destroy(Article $article)
    {
        $article->delete();
        return response()->json(['message' => 'Deleted']);
    }
}