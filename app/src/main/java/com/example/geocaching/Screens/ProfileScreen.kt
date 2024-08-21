package com.example.geocaching.Screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.geocaching.R
import com.example.geocaching.service.LocationService
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID

enum class EditableField {
    FIRST_NAME, LAST_NAME, PHONE_NUMBER
}

data class FullUser(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val points: Long = 0,
    val photoUrl: String = "",
)

@Composable
fun ProfileScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentField by remember { mutableStateOf<EditableField?>(null) }
    var tempInput by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val storage = Firebase.storage
    val userId = currentUser?.uid
    var fullUser by remember { mutableStateOf(FullUser()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                photoUri = it
                updateProfilePicture(context, userId!!, it, firestore, storage) { newPhotoUrl ->
                    fullUser = fullUser.copy(photoUrl = newPhotoUrl) // Update the user state with the new photo URL
                }
            }
        }
    )

    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username")
                    val points = document.getLong("points")
                    val photoUrl = document.getString("photoUrl")
                    val email = document.getString("email")
                    val phoneNumber = document.getString("phoneNumber")
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")

                    Log.d("ProfileScreen", "Fetched user: $username")

                    if (username != null && points != null && photoUrl != null && email != null
                        && phoneNumber != null && firstName != null && lastName != null) {
                        fullUser = FullUser(firstName, lastName, email, username, phoneNumber, points, photoUrl)
                    } else {
                        Log.d("ProfileScreen", "Invalid user data: $document")
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.green_satelite_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        modifier = Modifier
                            .padding(3.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = rememberImagePainter(data = fullUser.photoUrl),
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(135.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    BorderStroke(1.dp, Color.Black),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = fullUser.username,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(text = "Points:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "${fullUser.points}", color = Color.White, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(text = "Email:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = fullUser.email, color = Color.White, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "First Name:",
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.alignByBaseline(),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fullUser.firstName,
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit First Name",
                            modifier = Modifier
                                .size(35.dp)
                                .clickable {
                                    currentField = EditableField.FIRST_NAME
                                    tempInput = fullUser.firstName
                                    showDialog = true
                                },
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Last Name:",
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.alignByBaseline(),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fullUser.lastName,
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit Last Name",
                            modifier = Modifier
                                .size(35.dp)
                                .clickable {
                                    currentField = EditableField.LAST_NAME
                                    tempInput = fullUser.lastName
                                    showDialog = true
                                },
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Phone Number:",
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.alignByBaseline(),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fullUser.phoneNumber,
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit Phone Number",
                            modifier = Modifier
                                .size(35.dp)
                                .clickable {
                                    currentField = EditableField.PHONE_NUMBER
                                    tempInput = fullUser.phoneNumber
                                    showDialog = true
                                },
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(text = "Change profile picture")
                        }
                        Spacer(modifier = Modifier.width(15.dp))

                        if (showChangePasswordDialog) {
                            ChangePasswordDialog(
                                onDismiss = { showChangePasswordDialog = false },
                                onSuccess = { showChangePasswordDialog = false }
                            )
                        }

                        Button(
                            onClick = {
                                showChangePasswordDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(text = "Change password")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, LocationService::class.java)
                                context.startService(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(text = "Start Service")
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Button(
                            onClick = {
                                val intent = Intent(context, LocationService::class.java)
                                context.stopService(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(text = "Stop Service")
                        }
                    }
                    Row(modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(Color(255, 49, 49)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(text = "LOG OUT")
                        }


                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                title = {
                                    Text(
                                        text = when (currentField) {
                                            EditableField.FIRST_NAME -> "Edit First Name"
                                            EditableField.LAST_NAME -> "Edit Last Name"
                                            EditableField.PHONE_NUMBER -> "Edit Phone Number"
                                            else -> ""
                                        }
                                    )
                                },
                                text = {
                                    Column {
                                        Text(text = "Enter your new ${currentField?.name?.toLowerCase()?.replace("_", " ")}:")
                                        TextField(
                                            value = tempInput,
                                            onValueChange = { tempInput = it },
                                            singleLine = true
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            when (currentField) {
                                                EditableField.FIRST_NAME -> {
                                                    fullUser = fullUser.copy(firstName = tempInput)
                                                    updateUserFieldInFirestore(context, userId!!, "firstName", tempInput)
                                                }
                                                EditableField.LAST_NAME -> {
                                                    fullUser = fullUser.copy(lastName = tempInput)
                                                    updateUserFieldInFirestore(context, userId!!, "lastName", tempInput)
                                                }
                                                EditableField.PHONE_NUMBER -> {
                                                    fullUser = fullUser.copy(phoneNumber = tempInput)
                                                    updateUserFieldInFirestore(context, userId!!, "phoneNumber", tempInput)
                                                }
                                                else -> {}
                                            }
                                            showDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                                        shape = RoundedCornerShape(25.dp)
                                    ) {
                                        Text("Save")
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = { showDialog = false },
                                        colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                                        shape = RoundedCornerShape(25.dp)
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Change Password") },
        text = {
            Column {
                TextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                errorMessage?.let {
                    Text(text = it, color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newPassword != confirmPassword) {
                    errorMessage = "Passwords do not match"
                } else {
                    val user = FirebaseAuth.getInstance().currentUser
                    val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
                    user?.reauthenticate(credential)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                            onSuccess()
                                        } else {
                                            errorMessage = "Failed to update password: ${updateTask.exception?.message}"
                                        }
                                    }
                            } else {
                                errorMessage = "Re-authentication failed: ${task.exception?.message}"
                            }
                        }
                }
            }) {
                Text("Change Password")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

fun updateProfilePicture(context: Context, userId: String, uri: Uri, firestore: FirebaseFirestore, storage: FirebaseStorage, onProfilePictureUpdated: (String) -> Unit) {
    val storageRef = storage.reference.child("profile_photos/${UUID.randomUUID()}.jpg")
    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                firestore.collection("users").document(userId)
                    .update("photoUrl", downloadUrl.toString())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile picture updated successfully.", Toast.LENGTH_SHORT).show()
                        onProfilePictureUpdated(downloadUrl.toString())
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Failed to update photo URL in database: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Photo upload failed: ${exception.message}", Toast.LENGTH_LONG).show()
        }
}

fun updateUserFieldInFirestore(context: Context, userId: String, fieldName: String, fieldValue: String) {
    val firestore = Firebase.firestore
    val userRef = firestore.collection("users").document(userId)

    userRef.update(fieldName, fieldValue)
        .addOnSuccessListener {
            Toast.makeText(context, "$fieldName updated successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to update $fieldName: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}