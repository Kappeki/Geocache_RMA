package com.example.geocaching.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.geocaching.service.LocationService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.rememberCameraPositionState

enum class Size {
    Micro, Small, Medium, Large
}

data class MapObject(
    //mozda i navigate opcija
    val name: String,
    val description: String,
    val difficulty: Double,
    val terrain: Double,
    val size: Size,
    val hint: String, //obican text kao hint
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String, //image kao hint?
    val owner: String,
//    val numberReviews: Int, //vrv nece da ima ovo, ali mozda i hoce jer to dodje kao activity - founded/not founded pa poruka uz to
    val timestamp: Long // created when
) // promeniti mozda

//didnt found - ne dobijas poene ili eventualno veoma malo poena za pokusaj
//found - poeni se dobijaju na osnovu racunice koja ukljucuje difficulty, terrain i size
// Mozda i ako je koristio hint onda se umanje poeni

//obican marker za nesto sto nije ni found ni not found
//poseban marker za found i poseban za not found
//na pocetku moze da se odabere da li je found ili not found
//posle toga sta se odabere od ta dva vise ne moze da se odabere, vec moze samo ovo suportno

//postavis cache - dobijes poene (uvek isti broj poena)
//logujes cache - dobijas poene u zavisnosti od toga kakav si cache logovao i na koji nacin
//rangiranje na osnovu tih poena

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
    var showReviewDialog by remember { mutableStateOf(false) }
    var showReviewsDialog by remember { mutableStateOf(false) }
    var showAlreadyReviewedDialog by remember { mutableStateOf(false) }
    var showAlreadyVisitedDialog by remember { mutableStateOf(false) }
    var currentUsername by remember { mutableStateOf("") }
    val firestore = Firebase.firestore
    var mapObjects by remember { mutableStateOf<List<MapObject>>(emptyList()) }

    val currentUser = Firebase.auth.currentUser

    //pribavljamo current usera i smestamo u currentUsername, ovo LaunchedEffect se pokrece svaki put kad se current user
    //promeni
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
}
