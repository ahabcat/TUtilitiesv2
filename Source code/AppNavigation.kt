package com.example.miniproject

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun AppNavigation(onGoogleSignIn: () -> Unit)
{
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(                      //just named arguments, can be sent without the names as well
                navController = navController,
                onLoginClick = onGoogleSignIn,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("createpost") {
            CreatePost(navController = navController)
        }

        composable("viewposts") {
            ViewPosts()
        }

        composable("sharelocation") {
            ShareLocation(navController = navController)
        }

        composable("trackbus") {
            TrackBus(navController = navController)
        }

        composable("reviewcourse") {
            ReviewCourse(navController = navController)
        }

        composable("viewratings") {
            ViewRatings(navController = navController)
        }

    }
}
