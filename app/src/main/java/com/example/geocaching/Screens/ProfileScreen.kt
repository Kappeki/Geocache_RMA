package com.example.geocaching.Screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.geocaching.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class FullUser(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val points: Long = 0,
    val photoUrl: String = "",
//    val password: String
)

@Composable
fun ProfileScreen(navController: NavController) {

    val auth = Firebase.auth
    val currentUser = Firebase.auth.currentUser
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val userId = currentUser?.uid
    var fullUser by remember { mutableStateOf(FullUser()) }

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
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = rememberImagePainter(data = fullUser.photoUrl),
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                BorderStroke(1.dp, Color.Black),
                                RoundedCornerShape(16.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "${fullUser.username}",
                        fontSize = 26.sp,
//                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Email:")
                    Text(text = "${fullUser.email}")
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "",
                        modifier = Modifier
                            .clickable {

                            }
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "First name:")
                    Text(text = "${fullUser.firstName}")
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "",
                        modifier = Modifier
                            .clickable {

                            }
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Last name:")
                    Text(text = "${fullUser.lastName}")
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "",
                        modifier = Modifier
                            .clickable {

                            }
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Phone number:")
                    Text(text = "${fullUser.phoneNumber}")
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "",
                        modifier = Modifier
                            .clickable {

                            }
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF00796B)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(text = "Change profile picture")
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Button(
                        onClick = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(Color(255, 49, 49)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(text = "LOG OUT")
                    }
                }
                // fali opcija za promenu username-a i sifre, kao i implementacija
                // edita svih podataka
                // takodje obratiti paznju na opciju kako se prenosi sifra
                // i vrlo moguce da ne bi trebalo da postoji opcija za promenu emaila
            }
        }
    }
}