package com.example.geocaching.Screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.geocaching.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ObjectsTableScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    var currentUsername by remember { mutableStateOf("") }
    val firestore = Firebase.firestore
    var mapObjects by remember { mutableStateOf<List<MapObject>>(emptyList()) }
    var selectedObject by remember { mutableStateOf<MapObject?>(null) }
    var showTxtHintDialog by remember { mutableStateOf(false) }
    var showImgHintDialog by remember { mutableStateOf(false) }
    var showAlreadyLoggedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        fetchMapObjects(firestore)  { objects ->
            mapObjects = objects
        }
        currentUser?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    currentUsername = document.getString("username") ?: ""
                }
                .addOnFailureListener { exception ->
                    Log.e("MapsScreen", "Error fetching username", exception)
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Caches", fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)) {
                         items(mapObjects) { mapObject ->
                             Card(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(8.dp)
                                     .clickable { selectedObject = mapObject },
                                 shape = RoundedCornerShape(12.dp),
                                 colors = CardDefaults.cardColors(
                                     containerColor = Color(0xFF90EE90) // Light green color
                                 ),
                                 elevation = CardDefaults.elevatedCardElevation(4.dp)
                             ) {
                                 Column(
                                     modifier = Modifier
                                         .padding(12.dp)
                                         .fillMaxWidth()
                                 ) {
                                     Text(
                                         text = mapObject.name,
                                         style = MaterialTheme.typography.titleLarge,
                                         color = Color.Black
                                     )
                                     Spacer(modifier = Modifier.height(5.dp))
                                     Text(
                                         text = "Created by: ${mapObject.owner}",
                                         style = MaterialTheme.typography.bodyMedium,
                                         color = Color.Gray
                                     )
                                     Spacer(modifier = Modifier.height(10.dp))
                                     Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.SpaceBetween
                                     ) {
                                         Text(
                                             text = "Difficulty: ${mapObject.difficulty}",
                                             style = MaterialTheme.typography.bodyMedium,
                                             color = Color.Black
                                         )
                                         Spacer(modifier = Modifier.width(16.dp))
                                         Text(
                                             text = "Terrain: ${mapObject.terrain}",
                                             style = MaterialTheme.typography.bodyMedium,
                                             color = Color.Black
                                         )
                                         Spacer(modifier = Modifier.width(16.dp))
                                         Text(
                                             text = "Logged: ${mapObject.numLogs} times",
                                             style = MaterialTheme.typography.bodyMedium,
                                             color = Color.Black,
                                             modifier = Modifier.align(Alignment.CenterVertically)
                                         )
                                     }
                                 }
                             }
                        }
                    }

                    selectedObject?.let { obj ->
                        var isLogged by remember { mutableStateOf(false) }

                        // Asynchronously check if the cache is logged
                        LaunchedEffect(obj) {
                            checkIfUserLogged(context, obj, "TableScreen") { logged ->
                                isLogged = logged
                            }
                        }

                        CacheDetails(
                            mapObject = obj,
                            onDismissRequest = { selectedObject = null },
                            onLog = {
                                if (isLogged) {
                                    Toast.makeText(context, "This cache has already been logged!", Toast.LENGTH_SHORT).show()
                                } else {
                                    markAsLogged(context, obj, "TableScreen")
                                    Toast.makeText(context, "Cache logged successfully!", Toast.LENGTH_SHORT).show()
                                    selectedObject = null
                                }
                            },
                            onTextHintUsed = {
                                markTextHintUsed(context, obj, "TableScreen")
                                showTxtHintDialog = true
                                Toast.makeText(context, "Text hint used!", Toast.LENGTH_SHORT).show()
                            },
                            onImageHintUsed = {
                                markImageHintUsed(context, obj, "TableScreen")
                                showImgHintDialog = true
                                Toast.makeText(context, "Image hint used!", Toast.LENGTH_SHORT).show()
                            },
                            isLogged = isLogged
                        )

                        if (showTxtHintDialog) {
                            TxtHintDialog(mapObject = obj, onDismissRequest = { showTxtHintDialog = false })
                        }

                        if (showImgHintDialog) {
                            ImgHintDialog(mapObject = obj, onDismissRequest = { showImgHintDialog = false })
                        }
                    }

                    if (showAlreadyLoggedDialog) {
                        AlreadyLoggedDialog(onDismissRequest = { showAlreadyLoggedDialog = false })
                    }
                }
            }
        }
    }

    
}

private fun fetchMapObjects(firestore: FirebaseFirestore, onObjectsFetched: (List<MapObject>) -> Unit) {
    Log.d("TableScreen", "Fetching map objects from Firestore")
    firestore.collection("objects")
        .orderBy("name", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { documents ->
            val objects = documents.mapNotNull { document ->
                val name = document.getString("name")
                val description = document.getString("description")
                val difficulty = document.getDouble("difficulty") ?: 1.0
                val terrain = document.getDouble("terrain") ?: 1.0
                val hint = document.getString("hint")
                val imageUrl = document.getString("image_url")
                val owner = document.getString("owner")
                val numLogs = document.getLong("number_logs")?.toInt() ?: 0
                val latitude = document.getDouble("latitude")
                val longitude = document.getDouble("longitude")
                val timestamp = document.getLong("created_at") ?: 0L

                Log.d("TableScreen", "Document data: ${document.data}")

                if (name != null && description != null && hint != null && imageUrl != null &&
                    owner != null && latitude != null && longitude != null) {
                    MapObject(name, description, difficulty, terrain, hint, imageUrl, owner, numLogs, latitude, longitude, timestamp)
                } else {
                    Log.d("TableScreen", "Invalid object data: $document")
                    null
                }
            }
            Log.d("TableScreen", "Objects fetched: $objects")
            onObjectsFetched(objects)
        }
        .addOnFailureListener { exception ->
            Log.e("TableScreen", "Error fetching objects", exception)
        }
}
