package com.example.miniproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Text

@Composable
fun LoginScreen(navController: NavController,
                onLoginClick: () -> Unit,
                onLoginSuccess: () -> Unit
)
{

    val isLoggedIn = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener {
            isLoggedIn.value = it.currentUser != null
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(isLoggedIn.value) {
        if (isLoggedIn.value) {
            onLoginSuccess()
        }
    }
    /*
        if (isLoggedIn) {
            onLoginSuccess()
        }

    */
    /*
        var email by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var loginErrorMessage by remember { mutableStateOf("") }

        val auth = remember { Firebase.auth }
        val allowedDomain = "tezu.ac.in"

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

     */

    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()          //without this, box layout will cover only as much space as its children acquire
            .padding(24.dp),
        contentAlignment = Alignment.Center   //works as intended only when box uses the whole screen (fillMaxSize())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Image(

                painter = painterResource(id = R.drawable.tu_logo),
                contentDescription = "University Logo"
            )

            Text("Welcome", style = MaterialTheme.typography.titleLarge)

            Button(
                onClick = { onLoginClick()
                    //navController.navigate("home")    //for testing without login, use this and comment previous line
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in with Google")
            }

            /*
                        // Enrollment Text Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                                email = it
                                                isError = !emailRegex.matches(it) && it.isNotEmpty()
                                            },
                            label = { Text("Official Email ID") },
                            placeholder = { Text("@tezu.ac.in") },
                            trailingIcon = {
                                            if(email.isNotEmpty()) {
                                            IconButton(onClick = { email = ""}) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                                    }
                                } },
                            singleLine = true,
                            isError = isError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

            */

            /*
                        if (isError) {          //may make the app laggy as the spacing increases/decreases
                            Text(
                                text = "Invalid email format",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }

             */

            /*
                        // Password Text Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation =
                                if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            trailingIcon = {
                                val visibilityIcon = if(passwordVisible) Icons.Filled.Visibility
                                                     else Icons.Filled.VisibilityOff

                                IconButton(onClick = {passwordVisible = !passwordVisible}) {
                                    Icon(imageVector = visibilityIcon, contentDescription = "show/hide password")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

            */

            /*
                        if (loginErrorMessage.isNotEmpty()) {
                            Text(loginErrorMessage, color = Color.Red, fontSize = 14.sp)
                        }

                        // Login Button
                        Button(
                            onClick = {

                                loginErrorMessage = ""

                                if (!emailRegex.matches(email)) {
                                    loginErrorMessage = "Invalid email format"
                                    email = ""
                                    return@Button
                                }

                                if (!email.endsWith("@$allowedDomain")) {
                                    loginErrorMessage = "Only official university email IDs are allowed"
                                    return@Button
                                }

                                if (password.length < 6) {
                                    loginErrorMessage = "Password must be at least 6 characters"
                                    return@Button
                                }
                                navController.navigate("home")

            */

            /*
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        loginErrorMessage = "Login successful!"
                                        navController.navigate("home")
                                    }
                                    .addOnFailureListener { e ->
                                        loginErrorMessage = e.message ?: "Login failed"
                                        email = ""
                                        password = ""
                                    }

                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Login")
                        }

             */

        }
    }
}
