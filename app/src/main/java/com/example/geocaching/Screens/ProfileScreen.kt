package com.example.geocaching.Screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(navController: NavController) {

    val currentUser = Firebase.auth.currentUser
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val userId = currentUser?.uid
    var username by remember { mutableStateOf("") }

    if(userId != null) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                   username = document.getString("username").toString()
            }
            .addOnFailureListener { e ->
                Log.e("LeaderboardScreen", "Error with fetching users", e)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Hello, ${username} 💀💀", fontSize = 30.sp)

    }
}