<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log; // Tambahkan untuk logging jika diperlukan

class UserController extends Controller
{
    /**
     * Display a listing of all users.
     *
     * @return \Illuminate\Http\JsonResponse
     */
    public function index()
    {
        // Endpoint ini akan mengembalikan semua pengguna.
        // Dalam aplikasi nyata, Anda HARUS memproteksi endpoint ini,
        // misalnya hanya untuk admin.
        try {
            $users = User::all();
            // Anda mungkin ingin memilih field tertentu untuk dikembalikan,
            // daripada mengembalikan semua data pengguna (termasuk password hash jika tidak di-hidden di model).
            // Contoh: $users = User::select('id', 'name', 'email', 'created_at')->get();
            return response()->json($users);
        } catch (\Exception $e) {
            Log::error('Error fetching all users: ' . $e->getMessage());
            return response()->json(['message' => 'Could not retrieve users', 'error' => $e->getMessage()], 500);
        }
    }

    /**
     * Display the specified user.
     *
     * @param  \App\Models\User  $user (Route Model Binding)
     * @return \Illuminate\Http\JsonResponse
     */
    public function show(User $user)
    {
        // Laravel's Route Model Binding akan secara otomatis melakukan findOrFail.
        // Jika user tidak ditemukan, Laravel akan mengembalikan 404.
        // Endpoint ini akan mengembalikan detail user spesifik.
        // Dalam aplikasi nyata, pertimbangkan otorisasi:
        // - Apakah pengguna hanya boleh melihat profilnya sendiri?
        // - Apakah admin boleh melihat profil siapa saja?
        try {
            // Model User secara default akan menyembunyikan field 'password' dan 'remember_token'
            // berdasarkan properti $hidden di model User.
            return response()->json($user);
        } catch (\Exception $e) {
            // Ini seharusnya tidak terjadi jika Route Model Binding berhasil,
            // tapi sebagai tindakan pencegahan.
            Log::error("Error fetching user with ID {$user->id}: " . $e->getMessage());
            return response()->json(['message' => 'Could not retrieve user data', 'error' => $e->getMessage()], 500);
        }
    }

    // Jika Anda ingin menambahkan fungsi update atau delete user di masa mendatang,
    // pastikan untuk menambahkan validasi dan otorisasi yang tepat.
    // Contoh (belum diimplementasikan sepenuhnya):
    // public function update(Request $request, User $user)
    // {
    //     // Otorisasi: Pastikan pengguna yang login boleh mengupdate user ini
    //     // if (auth()->user()->id !== $user->id && !auth()->user()->isAdmin()) {
    //     //     return response()->json(['message' => 'Unauthorized'], 403);
    //     // }

    //     $validatedData = $request->validate([
    //         'name' => 'sometimes|required|string|max:255',
    //         'email' => 'sometimes|required|string|email|max:255|unique:users,email,' . $user->id,
    //         // Jangan update password di sini kecuali ada endpoint khusus atau validasi yang kuat
    //     ]);

    //     $user->update($validatedData);
    //     return response()->json($user);
    // }
}
