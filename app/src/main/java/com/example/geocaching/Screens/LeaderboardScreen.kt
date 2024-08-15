package com.example.geocaching.Screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class User(
    val username: String,
    val points: Long,
    val photoUrl: String
)

@Composable
fun LeaderboardScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    //ovo se poziva uvek kada se staruje leaderboard screen
    LaunchedEffect(Unit) {
        fetchLeaderboard { fetchedUsers ->
            users = fetchedUsers
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Leaderboard", fontSize = 48.sp,
                    fontWeight = FontWeight.Bold ,
                    color = Color(80, 141, 78)
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (users.size >= 3) {
                    // Display the top 3 users in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        users.take(3).forEachIndexed { index, user ->
                            TopThreeUserCard(user = user, rank = index + 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn {
                    itemsIndexed(users.drop(3)) { index, user ->
                        RegularUserCard(index = index + 4, user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun TopThreeUserCard(user: User, rank: Int) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberImagePainter(data = user.photoUrl),
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(90.dp)
                .border(
                    BorderStroke(
                        4.dp,
                        color = when (rank) {
                            1 -> Color(214, 175, 54)
                            2 -> Color(167, 167, 173)
                            3 -> Color(167, 112, 68)
                            else -> Color.Black
                        },
                    ), CircleShape
                )
        )
        Text(
            text = when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> ""
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${user.username}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${user.points} points",
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RegularUserCard(user: User, index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        shape = RoundedCornerShape(8.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(144, 238, 144)
//        )
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                painter = rememberImagePainter(data = user.photoUrl),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(50.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = "${user.username}",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            Column(
                modifier = Modifier.padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${user.points}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "points",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

private fun fetchLeaderboard(onUsersFetched: (List<User>) -> Unit) {
    val firestore = Firebase.firestore

    firestore.collection("users")
        .limit(21)
        .orderBy("points", Query.Direction.DESCENDING) //sortiranje korisnika po broju poene, opadajuce
        .get()
        .addOnSuccessListener { documents ->
            val users = documents.mapNotNull { document ->
                val username = document.getString("username")
                val points = document.getLong("points")
                val photoUrl = document.getString("photoUrl")

                Log.d("LeaderboardScreen", "Fetched user: $username with $points points and photoUrl: $photoUrl")

                if (username != null && points != null && photoUrl != null) {
                    User(username, points, photoUrl)
                } else {
                    Log.d("LeaderboardScreen", "Invalid user data: $document")
                    null
                }
            }
            //ovime vracamo listu korisnika koju smo sad napravili
            onUsersFetched(users)
        }
        .addOnFailureListener { e ->
            Log.e("LeaderboardScreen", "Error with fetching users", e)
        }
}