package com.example.geocaching.Screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.geocaching.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        LoginHeaderImage()
        LoginForm(navController = navController)
    }
}

@Composable
fun LoginForm(navController: NavController) {
    val auth: FirebaseAuth = Firebase.auth
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val emailOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val passwordOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }

    suspend fun shake(animatable: androidx.compose.animation.core.Animatable<Float, *>) {
        animatable.animateTo(
            targetValue = 10f,
            animationSpec = tween(durationMillis = 50, easing = LinearEasing)
        )
        animatable.animateTo(
            targetValue = -10f,
            animationSpec = tween(durationMillis = 50, easing = LinearEasing)
        )
        animatable.animateTo(
            targetValue = 10f,
            animationSpec = tween(durationMillis = 50, easing = LinearEasing)
        )
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 50, easing = LinearEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .padding(top = 15.dp)
            .padding(bottom = 250.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "LOGIN", fontSize = 30.sp, color = Color(80, 141, 78))
            Spacer(modifier = Modifier.height(25.dp))
            OutlinedTextField( // email input
                value = email,
                onValueChange = {
                    email = it
                    emailError = it.isEmpty() // Mark as error if empty
                },
                label = { Text(text = "Email", color = if (emailError) Color.Red else Color(80, 141, 78)) },
                placeholder = { Text(text = "example@gmail.com", color = if (emailError) Color.Red else Color(80, 141, 78)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(emailOffsetX.value.toInt(), 0) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (emailError) Color.Red else Color(80, 141, 78),
                    unfocusedTextColor = if (emailError) Color.Red else Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = if (emailError) Color.Red else Color(80, 141, 78),
                    unfocusedBorderColor = if (emailError) Color.Red else Color.Gray,
                    focusedLabelColor = if (emailError) Color.Red else Color(80, 141, 78),
                    unfocusedLabelColor = if (emailError) Color.Red else Color.Gray,
                ),
                isError = emailError,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(25.dp))
            OutlinedTextField( // password input
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.isEmpty()
                },
                label = { Text(text = "Password", color = if (passwordError) Color.Red else Color(80, 141, 78)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, "")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(passwordOffsetX.value.toInt(), 0) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (passwordError) Color.Red else Color(80, 141, 78),
                    unfocusedTextColor = if (passwordError) Color.Red else Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = if (passwordError) Color.Red else Color(80, 141, 78),
                    unfocusedBorderColor = if (passwordError) Color.Red else Color.Gray,
                    focusedLabelColor = if (passwordError) Color.Red else Color(80, 141, 78),
                    unfocusedLabelColor = if (passwordError) Color.Red else Color.Gray,
                ),
                isError = passwordError,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(25.dp))
            Button(
                onClick = {
                    var isValid = true
                    if (email.isBlank()) {
                        emailError = true
                        isValid = false
                        coroutineScope.launch { shake(emailOffsetX) }
                    }
                    if (password.isBlank()) {
                        passwordError = true
                        isValid = false
                        coroutineScope.launch { shake(passwordOffsetX) }
                    }

                    if (isValid) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("main") // navigate to main screen
                                } else {
                                    Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    println("Login failed: ${task.exception?.message}")
                                }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "LOGIN", color = Color.White)
            }
            Spacer(modifier = Modifier.height(5.dp))
            TextButton(onClick = { navController.navigate("register") }) {
                Text(text = "Don't have an account? Register here", color = Color(80, 141, 78))
            }
        }
    }
}

@Composable
fun LoginHeaderImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFF00796B)), // Darker green background
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with your header image resource
            contentDescription = "Header Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        )
        Text(text = "GEOCACHING", fontFamily = FontFamily.Monospace, fontSize = 36.sp, color = Color.White)
    }
}