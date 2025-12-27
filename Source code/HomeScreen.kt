package com.example.miniproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {   //navController parameter needed even if in this page there is atleast 1 redirection to
                                                //another page
    //val user = FirebaseAuth.getInstance().currentUser
    Box(
        modifier = Modifier
            .fillMaxSize()          //without this, box layout will cover only as much space as its children acquire
            .padding(24.dp),
        contentAlignment = Alignment.Center   //works as intended only when box uses the whole screen (fillMaxSize())
    ) {
        Column(     //column starts from top left corner so it does not appear horizontally cetered
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            //modifier = Modifier.fillMaxSize()    //this makes the column's horizontalAlignment work when box's alignment is not centered
        ) {
            Button(
                onClick = {
                    navController.navigate("createpost")       //calls CreatePost() under "createposts" in AppNavigation.kt
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create a post")
            }

            Button(
                onClick = {
                    navController.navigate("viewposts")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View posts")
            }

            Button(
                onClick = {
                    navController.navigate("sharelocation")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share location of a bus")
            }

            Button(
                onClick = {
                    navController.navigate("trackbus")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Track current bus")
            }

            Button(
                onClick = {
                    navController.navigate("reviewcourse")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Review a course")
            }

            Button(
                onClick = {
                    navController.navigate("viewratings")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View ratings of courses")
            }

            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
            }) {
                Text("Log Out")
            }
        }
    }
}