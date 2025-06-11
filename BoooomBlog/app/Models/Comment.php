<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory; // Import HasFactory

class Comment extends Model
{
    use HasFactory; // Add this if you plan to use factories for Comments

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'body',
        'article_id',
        'user_id',
        // 'name' // If you want to store the commenter's name directly and not just rely on user_id->name
    ];

    public function article()
    {
        return $this->belongsTo(Article::class);
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}