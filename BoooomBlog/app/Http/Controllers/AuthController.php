<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use App\Models\User;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    /**
     * Handle an incoming authentication request.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        if (!Auth::attempt($request->only('email', 'password'))) {
            return response()->json([
                'message' => 'Invalid login details'
            ], 401);
        }

        $user = User::where('email', $request['email'])->firstOrFail();

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'message' => 'Login successful',
            'access_token' => $token,
            'token_type' => 'Bearer',
            'user' => $user
        ]);
    }

    /**
     * Handle user registration.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function register(Request $request)
    {
        $validatedData = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'password' => 'required|string|min:8|confirmed', // Requires password_confirmation field
        ]);

        $user = User::create([
            'name' => $validatedData['name'],
            'email' => $validatedData['email'],
            'password' => Hash::make($validatedData['password']),
        ]);

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'message' => 'Registration successful',
            'access_token' => $token,
            'token_type' => 'Bearer',
            'user' => $user
        ], 201);
    }


    /**
     * Log the user out (Invalidate the token).
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\JsonResponse
     */
    
    public function logout(Request $request)
    {
        // Cek apakah user terautentikasi
        if (!$request->user()) {
            \Illuminate\Support\Facades\Log::error('Logout attempt: User not authenticated or not resolved from token.');
            // Anda bisa mengembalikan respons error yang lebih spesifik di sini jika mau
            // return response()->json(['message' => 'Unauthenticated.'], 401);
            // Atau biarkan error 500 terjadi agar terlihat jelas ada masalah autentikasi
        }

        // Untuk melihat apakah user ada sebelum mencoba menghapus token
        // dd($request->user()); // Hentikan eksekusi dan tampilkan user, lalu hapus baris ini

        try {
            $token = $request->user()->currentAccessToken();
            if ($token) {
                $token->delete();
            } else {
                // Jika tidak ada current access token, mungkin token sudah tidak valid atau tidak ada.
                // Anda bisa anggap ini sebagai logout yang berhasil atau log sebagai warning.
                \Illuminate\Support\Facades\Log::warning('Logout attempt for user ID ' . ($request->user() ? $request->user()->id : 'unknown') . ': No current access token found to delete.');
            }

            return response()->json([
                'message' => 'Successfully logged out'
            ]);
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\Log::error('Error during token deletion: ' . $e->getMessage());
            return response()->json(['message' => 'Logout failed due to server error while deleting token.', 'error' => $e->getMessage()], 500);
        }
    }
}
