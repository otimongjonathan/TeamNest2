package com.example.teamnest

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = mutableStateOf(auth.currentUser)
    val currentUser: State<FirebaseUser?> = _currentUser

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        _currentUser.value?.uid?.let { 
            fetchUserProfile(it)
            updateFcmToken()
        }
    }

    private fun fetchUserProfile(uid: String) {
        db.collection("users").document(uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("AuthViewModel", "Listen failed", e)
                return@addSnapshotListener
            }
            _userProfile.value = snapshot?.toObject(UserProfile::class.java)
        }
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("AuthViewModel", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
            db.collection("users").document(uid).update("fcmToken", token)
                .addOnSuccessListener { Log.d("AuthViewModel", "FCM Token updated") }
                .addOnFailureListener { Log.e("AuthViewModel", "Failed to update FCM Token", it) }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter email and password")
            return
        }
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email.trim().lowercase(), password.trim())
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    _currentUser.value?.uid?.let { 
                        fetchUserProfile(it)
                        updateFcmToken()
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String, onResult: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.replace("\\s".toRegex(), "").lowercase()
        val trimmedPassword = password.trim()
        val trimmedUsername = username.trim()

        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            onResult(false, "Please enter a valid email address")
            return
        }
        if (trimmedPassword != confirmPassword.trim()) {
            onResult(false, "Passwords do not match")
            return
        }
        if (trimmedPassword.length < 6) {
            onResult(false, "Password should be at least 6 characters")
            return
        }

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val profile = UserProfile(uid, trimmedUsername, trimmedEmail)
                    db.collection("users").document(uid).set(profile)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _currentUser.value = auth.currentUser
                            _currentUser.value?.uid?.let { 
                                fetchUserProfile(it)
                                updateFcmToken()
                            }
                            onResult(true, null)
                        }
                        .addOnFailureListener { e ->
                            _isLoading.value = false
                            onResult(false, "Database Error: ${e.message}")
                        }
                } else {
                    _isLoading.value = false
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
    }
}
