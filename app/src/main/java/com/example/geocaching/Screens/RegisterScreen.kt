package com.example.geocaching.Screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.geocaching.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

@Composable
fun RegisterScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        RegisterHeaderImage()
        RegisterForm(navController = navController)
    }
}

@Composable
fun RegisterForm(navController: NavController) {
    val auth: FirebaseAuth = Firebase.auth
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val db = Firebase.firestore
    val storage = Firebase.storage //cemu ovo sluzi?

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            photoUri = uri
        }
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .padding(top = 15.dp)
            .padding(bottom = 0.dp),
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
            Text(text = "REGISTER", fontSize = 30.sp, color = Color(80, 141, 78))
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField( // email input
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email", color = Color(80, 141, 78)) },
                placeholder = { Text(text = "example@gmail.com", color = Color(80, 141, 78)) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(80, 141, 78),
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField( // username input
                value = username,
                onValueChange = { username = it },
                label = { Text(text = "Username", color = Color(80, 141, 78)) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(80, 141, 78),
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField( // firstname input
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(text = "First Name", color = Color(80, 141, 78)) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(80, 141, 78),
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField( // lastname input
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(text = "Last Name", color = Color(80, 141, 78)) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(80, 141, 78),
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField( // phone number input
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(text = "Phone Number", color = Color(80, 141, 78)) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(80, 141, 78),
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField( // password input
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password", color = Color(80, 141, 78)) },
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
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(80, 141, 78),
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField( // confirm password input
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(text = "Confirm Password", color = Color(80, 141, 78)) },
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
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(80, 141, 78),
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "Pick Profile Photo", color = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                      if(password == confirmPassword) {
                          auth.createUserWithEmailAndPassword(email, password)
                      } else {
                          Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                      }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "REGISTER", color = Color.White)
            }
            TextButton(onClick = { navController.navigate("login") }) {
                Text(text = "Already have an account? Log in", color = Color(80, 141, 78))
            }
        }
    }
}

@Composable
fun RegisterHeaderImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
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