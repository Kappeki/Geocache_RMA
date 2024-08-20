package com.example.geocaching.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.Location.distanceBetween
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.geocaching.R
import com.example.geocaching.service.LocationService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MapObject(
    val name: String,
    val description: String,
    val difficulty: Double,
    val terrain: Double,
    val hint: String, //text kao hint
    val imageUrl: String, //image kao hint
    val owner: String,
    val numLogs: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long // created when
)

data class VisitInfo(
    val visited: Boolean = false,
    val usedTextHint: Boolean = false,
    val usedImageHint: Boolean = false
)

//obican marker za nesto sto nije ni found ni not found
//poseban marker za found

//Poeni -> Postavljanje kesa - 30, logiranje bez hintova - 20, sa txt hint - 15, sa img hint - 10, sa txt i img hint - 5

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(0.0, 0.0), 15f, 0f, 0f)
    }
    var selectedObject by remember { mutableStateOf<MapObject?>(null) }
    var showTxtHintDialog by remember { mutableStateOf(false) }
    var showImgHintDialog by remember { mutableStateOf(false) }
    var showAlreadyLoggedDialog by remember { mutableStateOf(false) }
    var currentUsername by remember { mutableStateOf("") }
    val firestore = Firebase.firestore
    var mapObjects by remember { mutableStateOf<List<MapObject>>(emptyList()) }

    val currentUser = Firebase.auth.currentUser

    //states za filtriranje
    var filterName by remember { mutableStateOf("") }
    var filterOwner by remember { mutableStateOf("") }
    var filterDifficulty by remember { mutableDoubleStateOf(1.0) }
    var filterTerrain by remember { mutableDoubleStateOf(1.0) }
    var filterStartDate by remember { mutableStateOf<Long?>(null) }
    var filterEndDate by remember { mutableStateOf<Long?>(null) }
    var filterRadius by remember { mutableStateOf<Float?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    //pribavljamo current usera i smestamo u currentUsername, ovo LaunchedEffect se pokrece svaki put kad se current user promeni
    LaunchedEffect(currentUser) {
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

    //svaki put se ovo pokrece kad se locationPermissionState promeni
    LaunchedEffect(locationPermissionState.hasPermission) {
        if (locationPermissionState.hasPermission) {
            Log.d("MapsScreen", "Location permission granted")
            requestLocationUpdates(context, fusedLocationClient) { location ->
                if (location != null) {
                    currentLocation = location
                    Log.d("MapsScreen", "Location received: $location")
                } else {
                    Log.e("MapsScreen", "Location is null")
                }
            }
            fetchMapObjects(firestore) { objects ->
                mapObjects = objects
                Log.d("MapsScreen", "Fetched objects: $objects")
            }
        } else {
            Log.d("MapsScreen", "Requesting location permission")
            locationPermissionState.launchPermissionRequest()
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
            Column {
                Row(modifier = Modifier.padding(10.dp)) {
                    Button(
                        onClick = {
                            showFilters = !showFilters
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(text = if (showFilters) "Hide Filters" else "Show Filters", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            navController.navigate("add_object")
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

                if (showFilters) {
                    FilterSection(
                        filterName = filterName,
                        onNameChange = { filterName = it },
                        filterOwner = filterOwner,
                        onOwnerChange = { filterOwner = it },
                        filterDifficulty = filterDifficulty,
                        onDifficultyChange = { filterDifficulty = it },
                        filterTerrain = filterTerrain,
                        onTerrainChange = { filterTerrain = it },
                        filterStartDate = filterStartDate,
                        onStartDateChange = { filterStartDate = it },
                        filterEndDate = filterEndDate,
                        onEndDateChange = { filterEndDate = it },
                        filterRadius = filterRadius,
                        onRadiusChange = { filterRadius = it }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = locationPermissionState.hasPermission,
                            mapType = MapType.NORMAL
                        ),
                    ) {
                        val cachePin = resizeBitmap(context, R.drawable.cache, 100, 100)
                        val foundPin = resizeBitmap(context, R.drawable.found, 100, 100)

                        val filteredObjects = mapObjects.filter {
                            val isNameMatch = filterName.isEmpty() || it.name.contains(filterName, ignoreCase = true)
                            val isOwnerMatch = filterOwner.isEmpty() || it.owner.contains(filterOwner, ignoreCase = true)
                            val isDifficultyMatch = filterDifficulty == 0.0 || it.difficulty >= filterDifficulty
                            val isTerrainMatch = filterTerrain == 0.0 || it.terrain >= filterTerrain
                            val isStartDateMatch = filterStartDate == null || it.timestamp >= filterStartDate!!
                            val isEndDateMatch = filterEndDate == null || it.timestamp <= filterEndDate!!
                            val isRadiusMatch = filterRadius == null || (currentLocation != null && distanceBetween(
                                currentLocation!!.latitude,
                                currentLocation!!.longitude,
                                it.latitude,
                                it.longitude
                                //ovaj isRadiusMatch ce biti true ili ako je filterRadius null, ili ako je distanceBetween<= filter radius,
                                //tj ako smo u tom radiusu
                            ) <= filterRadius!!)
                            isNameMatch && isOwnerMatch && isDifficultyMatch && isTerrainMatch &&
                                    isStartDateMatch && isEndDateMatch && isRadiusMatch
                        }

                        //za svaki objekat unutar FilteredObjects stavljamo marker na mapi
                        filteredObjects.forEach { obj ->
                            var markerIcon: Bitmap = cachePin

                            LaunchedEffect(obj) {
                                checkIfUserLogged(context, obj, "MapScreen") { logged ->
                                    markerIcon = if (logged) foundPin else cachePin
                                }
                            }

                            val markerState = rememberMarkerState(position = LatLng(obj.latitude, obj.longitude))
                            Marker(
                                state = markerState,
                                title = obj.name,
                                snippet = obj.description,
                                icon = BitmapDescriptorFactory.fromBitmap(markerIcon),
                                onClick = {
                                    selectedObject = obj
                                    true
                                }
                            )
                        }
                    }

                    currentLocation?.let {
                        LaunchedEffect(it) {
                            cameraPositionState.position = CameraPosition(it, 15f, 0f, 0f)
                            Log.d("MapsScreen", "Camera position updated: $it")
                        }
                    }

                    //prikaz selektovanog objekta koji izaberemo na mapi
                    selectedObject?.let { obj ->
                        var isLogged by remember { mutableStateOf(false) }

                        // Asynchronously check if the cache is logged
                        LaunchedEffect(obj) {
                            checkIfUserLogged(context, obj, "MapScreen") { logged ->
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
                                    markAsLogged(context, obj, "MapScreen") {
                                        fetchMapObjects(firestore) { objects ->
                                            mapObjects = objects
                                        }
                                        selectedObject = null
                                    }
                                    Toast.makeText(context, "Cache logged successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onTextHintUsed = {
                                markTextHintUsed(context, obj, "MapScreen")
                                showTxtHintDialog = true
                                Toast.makeText(context, "Text hint used!", Toast.LENGTH_SHORT).show()
                            },
                            onImageHintUsed = {
                                markImageHintUsed(context, obj, "MapScreen")
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

@Composable
fun AlreadyLoggedDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Already Logged") },
        text = { Text("You have already logged this cache!") },
        confirmButton = {
            androidx.compose.material3.Button(onClick = onDismissRequest) {
                Text("OK")
            }
        }
    )
}

private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0]
}

private fun requestLocationUpdates(context: Context, fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (LatLng?) -> Unit) {
    val locationRequest = LocationRequest.create().apply {
        interval = 10000 // 10 seconds
        fastestInterval = 5000 // 5 seconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val latLng = LatLng(location.latitude, location.longitude)
                onLocationReceived(latLng)
                Log.d("MapsScreen", "Location received in callback: $latLng")
                fusedLocationClient.removeLocationUpdates(this) // Remove updates after getting the first location
                return
            }
            onLocationReceived(null)
        }
    }

    try {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            Log.d("MapsScreen", "Location updates requested")
        } else {
            Log.e("MapsScreen", "Location permission not granted")
            onLocationReceived(null)
        }
    } catch (e: SecurityException) {
        Log.e("MapsScreen", "Location permission denied", e)
        onLocationReceived(null)
    }
}

@Composable
fun FilterSection(
    filterName: String, onNameChange: (String) -> Unit,
    //ovo string znaci da prima string, a unit nam kaze da ne vraca nista, i poziva se ovo kad se promeni u polju
    //za unos teksta vrednost
    //ovo on name change PROSLEDIMO kad pozivamo FilterSection, i to je ustvari callback funkcija, i uvek kad se promeni
    //name u filter, poziva se ova funkcija on name change, u nasem slucaju to se nalazi u 137. liniji koda,
    //on name change nam sluzi da filterName = it, tj da se u filter name stavi novo ime
    filterOwner: String, onOwnerChange: (String) -> Unit, filterDifficulty: Double, onDifficultyChange: (Double) -> Unit,
    filterTerrain: Double, onTerrainChange: (Double) -> Unit, filterStartDate: Long?, onStartDateChange: (Long?) -> Unit,
    filterEndDate: Long?, onEndDateChange: (Long?) -> Unit, filterRadius: Float?, onRadiusChange: (Float?) -> Unit) {
    val stepValues = listOf(1f, 1.5f, 2f, 2.5f, 3f, 3.5f, 4f, 4.5f, 5f)

    Column(modifier = Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = filterName,
            onValueChange = onNameChange,
            label = { Text("Filter by Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = filterOwner,
            onValueChange = onOwnerChange,
            label = { Text("Filter by Owner") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Filter by Difficulty: ${if (filterDifficulty > 1) filterDifficulty else "Any"}")
        Slider(
            value = filterDifficulty.toFloat(),
            onValueChange = { value ->
                val closestValue = stepValues.minByOrNull { kotlin.math.abs(it - value) } ?: value
                onDifficultyChange(closestValue.toDouble()) },
            valueRange = 1f..5f,
            steps = stepValues.size - 2,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00796B),
                activeTrackColor = Color(0xFF00796B),
                activeTickColor = Color(0xFF00796B),
                inactiveTickColor = Color(0xFF00796B)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Filter by Terrain: ${if (filterTerrain > 1) filterTerrain else "Any"}")
        Slider(
            value = filterTerrain.toFloat(),
            onValueChange = { value ->
                val closestValue = stepValues.minByOrNull { kotlin.math.abs(it - value) } ?: value
                onTerrainChange(closestValue.toDouble()) },
            valueRange = 1f..5f,
            steps = stepValues.size - 2,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00796B),
                activeTrackColor = Color(0xFF00796B),
                activeTickColor = Color(0xFF00796B),
                inactiveTickColor = Color(0xFF00796B)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            DatePicker(
                label = "Start Date",
                widthNumber = 0.5f,
                selectedDate = filterStartDate,
                onDateChange = onStartDateChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            DatePicker(
                label = "End Date",
                widthNumber = 1f,
                selectedDate = filterEndDate,
                onDateChange = onEndDateChange
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = filterRadius?.toString() ?: "",
            onValueChange = { onRadiusChange(it.toFloatOrNull()) },
            label = { Text("Filter by Radius (meters)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CacheDetails(mapObject: MapObject, onDismissRequest: () -> Unit, onLog: () -> Unit, onTextHintUsed: () -> Unit, onImageHintUsed: () -> Unit, isLogged: Boolean) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(mapObject.name) },
        text = {
            Column {
                Text(text = "Created by: ${mapObject.owner}")
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Description: ${mapObject.description}")
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Difficulty: ${mapObject.difficulty}")
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Terrain: ${mapObject.terrain}")
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Logged: ${mapObject.numLogs} times")
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Button(
                        onClick = {
                            onTextHintUsed()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(bottom = 4.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Text Hint", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onImageHintUsed()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Picture Hint", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        onLog()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLogged,
                    colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(if (isLogged) "Already Logged" else "Log Cache", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun TxtHintDialog(mapObject: MapObject, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Hint for ${mapObject.name}") },
        text = {
            Text(text = "${mapObject.hint}")
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "Got it!", color = Color.White)
            }
        }
    )
}

@Composable
fun ImgHintDialog(mapObject: MapObject, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Picture hint for ${mapObject.name}") },
        text = {
            Image(
                painter = rememberImagePainter(mapObject.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "Got it!", color = Color.White)
            }
        }
    )
}

//{
//  "userActivities": {
//    "objectId1": {
//      "logged": true,
//      "usedTextHint": false,
//      "usedImageHint": true
//    },
//    "objectId2": {
//      "logged": false,
//      "usedTextHint": true,
//      "usedImageHint": false
//    }
//  }
//}

fun checkIfUserLogged(context: Context, mapObject: MapObject, screen: String, onResult: (Boolean) -> Unit) {
    val firestore = Firebase.firestore
    val currentUser = Firebase.auth.currentUser

    firestore.collection("users").document(currentUser?.uid ?: "").get()
        .addOnSuccessListener { document ->
            val userActivities = document.get("userActivities") as? Map<String, Any>
            val objectId = mapObject.name.replace(" ", "_")
            val hasLogged = userActivities?.get(objectId)?.let { it as Map<String, Boolean> }?.get("logged") ?: false
            onResult(hasLogged)
        }
        .addOnFailureListener { exception ->
            Log.e(screen, "Error checking logged objects", exception)
            onResult(false)
        }
}

fun markAsLogged(context: Context, mapObject: MapObject, screen: String, onLogged: () -> Unit) {
    val firestore = Firebase.firestore
    val currentUser = Firebase.auth.currentUser

    currentUser?.let { user ->
        val objectId = mapObject.name.replace(" ", "_")
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val userActivities = document.get("userActivities") as? Map<String, Any>
                val hasLogged = userActivities?.get(objectId)?.let { it as Map<String, Boolean> }?.get("logged") ?: false
                val usedTextHint = userActivities?.get(objectId)?.let { it as Map<String, Boolean> }?.get("usedTextHint") ?: false
                val usedImageHint = userActivities?.get(objectId)?.let { it as Map<String, Boolean> }?.get("usedImageHint") ?: false
                if (userActivities != null && hasLogged) {
                    Toast.makeText(context, "You have already logged this object.", Toast.LENGTH_SHORT).show()
                } else {
                    firestore.collection("users").document(user.uid)
                        .update("userActivities.$objectId.logged", true)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Logged successfully.", Toast.LENGTH_SHORT).show()
                            if (usedTextHint && usedImageHint) {
                                addPoints("txtAndImgHints")
                            } else if (usedTextHint) {
                                addPoints("txtHint")
                            } else if (usedImageHint) {
                                addPoints("imgHint")
                            } else {
                                addPoints("noHints")
                            }
                            incrementLogNumber(mapObject, screen)
                            onLogged
                        }
                        .addOnFailureListener { e ->
                            Log.e(screen, "Error marking as logged", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(screen, "Error fetching user data", e)
            }
    }
}

fun incrementLogNumber(mapObject: MapObject, screen: String) {
    val firestore = Firebase.firestore
    val objectId = mapObject.name.replace(" ", "_")

    objectId.let {
        firestore.collection("objects").document(objectId).get()
            .addOnSuccessListener { document ->
                val numLogs = (document.getLong("number_logs") ?: 1) + 1

                firestore.collection("objects").document(objectId)
                    .update("number_logs", numLogs)
                    .addOnSuccessListener {
                        Log.d(screen, "Number of logs incremented successfully.")
                    }
                    .addOnFailureListener { e ->
                        Log.e(screen, "Error updating number of logs", e)
                    }
            }
            .addOnFailureListener {e ->
                Log.e(screen, "Error fetching user data", e)
            }
    }

}
//postoji problem sa addPoints funkcijom - nije lepo implementirana ✅
//ne menja se ikonica markera kada je logged ✅
//obratiti paznju kada se loguje onda se dodaju poeni, ako se posle logovanja kliknu hintovi ne oduzimaju se poeni ili sta god, vec sve ostaje isto jer je vec logovao ✅
//kada se klikne prvo hint pa onda log onda se ne upise lepo u firestore, nece da se upise logged, ali hintovi hoce. Mada ako je prvo logged pa hintUsed onda hoce ✅
//notifikacije
//ne povecava se broj logovanja nakon uspesnog logovanja ✅
//proveriti servis

fun markTextHintUsed(context: Context, mapObject: MapObject, screen: String) {
    val firestore = Firebase.firestore
    val currentUser = Firebase.auth.currentUser

    currentUser?.let { user ->
        val objectId = mapObject.name.replace(" ", "_")
        firestore.collection("users").document(user.uid)
            .update("userActivities.$objectId.usedTextHint", true)
            .addOnSuccessListener {
                Log.d(screen, "Text Hint marked as used.")
            }
            .addOnFailureListener { e ->
                Log.e(screen, "Error marking text hint as used", e)
            }
    }
}

fun markImageHintUsed(context: Context, mapObject: MapObject, screen: String) {
    val firestore = Firebase.firestore
    val currentUser = Firebase.auth.currentUser

    currentUser?.let { user ->
        val objectId = mapObject.name.replace(" ", "_")
        firestore.collection("users").document(user.uid)
            .update("userActivities.$objectId.usedImageHint", true)
            .addOnSuccessListener {
                Log.d(screen, "Image Hint marked as used.")
            }
            .addOnFailureListener { e ->
                Log.e(screen, "Error marking image hint as used", e)
            }
    }
}

@Composable
fun DatePicker(label: String, widthNumber: Float, selectedDate: Long?, onDateChange: (Long?) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateChange(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Button(
        onClick = { datePickerDialog.show() },
        modifier = Modifier
            .fillMaxWidth(widthNumber)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
        shape = RoundedCornerShape(25.dp)
        ) {
        Text(text = if (selectedDate != null) dateFormat.format(Date(selectedDate)) else label, color = Color.White)
    }
}

private fun fetchMapObjects(firestore: FirebaseFirestore, onObjectsFetched: (List<MapObject>) -> Unit) {
    Log.d("MapScreen", "Fetching map objects from Firestore")
    firestore.collection("objects")
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

                Log.d("MapsScreen", "Document data: ${document.data}")

                if (name != null && description != null && hint != null && imageUrl != null &&
                    owner != null && latitude != null && longitude != null) {
                    MapObject(name, description, difficulty, terrain, hint, imageUrl, owner, numLogs, latitude, longitude, timestamp)
                } else {
                    Log.d("MapsScreen", "Invalid object data: $document")
                    null
                }
            }
            Log.d("MapsScreen", "Objects fetched: $objects")
            onObjectsFetched(objects)
        }
        .addOnFailureListener { exception ->
            Log.e("MapsScreen", "Error fetching objects", exception)
        }
}

private fun resizeBitmap(context: Context, drawableRes: Int, width: Int, height: Int): Bitmap {
    val bitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
    return Bitmap.createScaledBitmap(bitmap, width, height, false)
}

fun addPoints(action: String) {
    val user = Firebase.auth.currentUser
    val userId = user?.uid ?: return
    val firestore = Firebase.firestore

    firestore.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            val currentPoints = document.getLong("points") ?: 0
            val newPoints = when (action) {
                "noHints" -> currentPoints + 20
                "txtHint" -> currentPoints + 15
                "imgHint" -> currentPoints + 10
                "txtAndImgHints" -> currentPoints + 5
                else -> currentPoints
            }
            firestore.collection("users").document(userId).update("points", newPoints)
                .addOnSuccessListener {
                    Log.d("MapsScreen", "Points updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("MapsScreen", "Error updating points", e)
                }
        }
        .addOnFailureListener { e ->
            Log.e("MapsScreen", "Error getting user points", e)
        }
}
