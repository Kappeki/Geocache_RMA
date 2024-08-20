package com.example.geocaching.Screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.geocaching.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay

//TODO proveriti zasto nakon dodavanja objekta vraca na login i location service i notifikacije

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddObjectScreen(navController: NavController) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var difficulty by remember { mutableDoubleStateOf(1.0) }
    var terrain by remember { mutableDoubleStateOf(1.0) }
    var hint by remember {mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val stepValues = listOf(1f, 1.5f, 2f, 2.5f, 3f, 3.5f, 4f, 4.5f, 5f)

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val auth = Firebase.auth

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageBitmap = bitmap.asImageBitmap()
                imageUri = uri.toString()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.hasPermission) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.green_satelite_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Adding geocache... Please wait",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
//                LaunchedEffect(Unit) {
//                    delay(3000L) // 3-second delay
//                    navController.popBackStack()
//                }
            } else {
                Text(
                    text = "Add Geocache",
                    fontSize = 45.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(90.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Name", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Description", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Difficulty: ${"%.1f".format(difficulty)}", color = Color.White)
                Slider(
                    value = difficulty.toFloat(),
                    onValueChange = { value ->
                        val closestValue = stepValues.minByOrNull { kotlin.math.abs(it - value) } ?: value
                        difficulty = closestValue.toDouble() },
                    valueRange = 1f..5f,
                    steps = stepValues.size - 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00796B),
                        activeTrackColor = Color(0xFF00796B)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Terrain: ${"%.1f".format(terrain)}", color = Color.White)
                Slider(
                    value = terrain.toFloat(),
                    onValueChange = { value ->
                        val closestValue = stepValues.minByOrNull { kotlin.math.abs(it - value) } ?: value
                        terrain = closestValue.toDouble() },
                    valueRange = 1f..5f,
                    steps = stepValues.size - 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00796B),
                        activeTrackColor = Color(0xFF00796B)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text(text = "Hint", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(text = "Select Hint Picture", color = Color.White)
                }
                imageBitmap?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = it,
                        contentDescription = "Picked Hint Image",
                        modifier = Modifier.size(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val currentUser = auth.currentUser
                        val userId = currentUser?.uid
                        if (currentUser != null && name.isNotEmpty() && description.isNotEmpty() && hint.isNotEmpty()) {
                            isLoading = true
                            val firestore = FirebaseFirestore.getInstance()
                            firestore.collection("users").document(currentUser.uid).get()
                                .addOnSuccessListener { document ->
                                    val username = document.getString("username") ?: "Unknown"
                                    addObjectToFirestore(
                                        context = context,
                                        name = name,
                                        description = description,
                                        difficulty = difficulty,
                                        terrain = terrain,
                                        hint = hint,
                                        imageUri = imageUri,
                                        username = username,
                                        userId = userId!!,
                                        onComplete = {
                                            isLoading = false
                                            Toast.makeText(context, "Object added successfully", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        },
                                        onError = {
                                            isLoading = false
                                            Toast.makeText(context, "Failed to add object: $it", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Failed to retrieve user information", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(text = "Add Object", color = Color.White)
                }
            }
        }
    }
}

fun addObjectToFirestore(context: Context, name: String, description: String, difficulty: Double, terrain: Double, hint: String,
    imageUri: String?, username: String, userId: String, onComplete: () -> Unit, onError: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onError("Location permissions are not granted")
        return
    }

    locationProviderClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val timestamp = System.currentTimeMillis()
            val objectId = name.replace(" ", "_")

            val objectData = hashMapOf(
                "name" to name,
                "description" to description,
                "difficulty" to difficulty,
                "terrain" to terrain,
                "hint" to hint,
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "image_url" to "",
                "owner" to username,
                "created_at" to timestamp,
                "number_logs" to 1,
            )

            if (imageUri != null) {
                val imageRef = storage.reference.child("object_images/$objectId.jpg")
                val uploadTask = imageRef.putFile(android.net.Uri.parse(imageUri))
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        objectData["image_url"] = downloadUri.toString()
                        saveObjectDataAndAddPoints(firestore, userId, objectId, objectData, onComplete, onError)
                    } else {
                        onError("Failed to upload image")
                    }
                }
            } else {
                saveObjectDataAndAddPoints(firestore, userId, objectId, objectData, onComplete, onError)
            }
        } else {
            onError("Failed to get current location")
        }
    }.addOnFailureListener { e ->
        onError("Failed to get current location: ${e.message}")
    }
}

fun saveObjectDataAndAddPoints(firestore: FirebaseFirestore, userId: String, objectId: String, objectData: Map<String, Any>, onComplete: () -> Unit, onError: (String) -> Unit) {
    firestore.collection("objects").document(objectId).set(objectData)
        .addOnSuccessListener {
            onComplete()
            updateUserPoints(firestore, userId, objectId,30, onComplete, onError)
        }
        .addOnFailureListener { e ->
            onError("Failed to save object data: ${e.message}")
        }
}

fun updateUserPoints(firestore: FirebaseFirestore, userId: String, objectId: String, pointsToAdd: Int, onComplete: () -> Unit, onError: (String) -> Unit) {
    val userDocRef = firestore.collection("users").document(userId)

    firestore.runTransaction { transaction ->
        val snapshot = transaction.get(userDocRef)

        val userActivities = snapshot.get("userActivities") as? MutableMap<String, MutableMap<String, Boolean>> ?: mutableMapOf()
        val objectActivity = userActivities[objectId] ?: mutableMapOf()
        objectActivity["logged"] = true
        objectActivity["usedImageHint"] = true
        objectActivity["usedTextHint"] = true
        userActivities[objectId] = objectActivity
        transaction.update(userDocRef, "userActivities", userActivities)

        val currentPoints = snapshot.getLong("points") ?: 0
        val newPoints = currentPoints + pointsToAdd
        transaction.update(userDocRef, "points", newPoints)
    }
    .addOnSuccessListener {
        onComplete()
    }
    .addOnFailureListener { e ->
        onError("Failed to update user points: ${e.message}")
    }
}