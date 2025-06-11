<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Article extends Model
{
    // Izinkan mass-assignment untuk kolom berikut
    protected $fillable = [
        'judul',
        'gambar',
        'tanggal',
        'penulis',
        'kategori',
        'isi',
        'author_id',
    ];

    // Relasi ke user (penulis)
    public function user()
    {
        return $this->belongsTo(User::class, 'author_id');
    }

    // Relasi ke komentar
    public function comments()
    {
        return $this->hasMany(Comment::class);
    }
}
