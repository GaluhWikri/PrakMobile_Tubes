<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('articles', function (Blueprint $table) {
            $table->id();
            $table->string('judul');               // Judul artikel
            $table->string('gambar')->nullable();  // Gambar opsional
            $table->date('tanggal');               // Tanggal artikel
            $table->string('penulis');             // Nama penulis (bisa nama tampilan)
            $table->string('kategori');            // Kategori artikel
            $table->text('isi');                   // Isi artikel
            $table->foreignId('author_id')         // Foreign key ke users
                ->constrained('users')
                ->onDelete('cascade');
            $table->timestamps();                  // created_at dan updated_at
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('articles');
    }
};
