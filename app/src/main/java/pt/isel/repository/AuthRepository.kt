package pt.isel.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    fun ensureAuth(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            onSuccess()
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d("AuthRepository", "Anonymous sign-in success: ${it.user?.uid}")
                    onSuccess()
                }
                .addOnFailureListener {
                    Log.e("AuthRepository", "Anonymous sign-in failed", it)
                    onFailure(it)
                }
        }
    }
}
