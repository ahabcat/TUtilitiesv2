package com.example.miniproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.miniproject.ui.theme.MiniProjectTheme
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    companion object {
        private const val REQ_ONE_TAP = 1001
    }

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val auth = FirebaseAuth.getInstance()
    // val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

    // Handle One-Tap result
    private val oneTapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential =
                        oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken

                    if (idToken != null) {
                        val firebaseCredential =
                            GoogleAuthProvider.getCredential(idToken, null)

                        auth.signInWithCredential(firebaseCredential)
                            .addOnSuccessListener {
                                Log.d("AUTH", "LOGIN SUCCESS")
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                            }
                            .addOnFailureListener {
                                Log.e("AUTH", "Firebase Auth Error", it)
                                Toast.makeText(this, "Auth Failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: ApiException) {
                    Log.e("AUTH", "One Tap API Error: ${e.statusCode} - ${e.message}")
                } catch (e: Exception) {
                    Log.e("AUTH", "One Tap General Error", e)
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initOneTap()   // Initialize One Tap before UI
        enableEdgeToEdge()
        setContent()
        {
            MiniProjectTheme()
            {
                AppNavigation(
                    onGoogleSignIn = { launchOneTap() }     //named argument 'onGoogleSignIn' of AppNavigation()
                )
            }
        }
    }

    private fun initOneTap() {
        oneTapClient = Identity.getSignInClient(this)

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id)) // VERY IMPORTANT
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    fun launchOneTap() {

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    // Extract Google credential from One Tap flow
                    val intentSender = result.pendingIntent.intentSender

                    startIntentSenderForResult(
                        intentSender,
                        REQ_ONE_TAP,
                        null,
                        0,
                        0,
                        0,
                        null
                    )

                } catch (e: Exception) {
                    Log.e("AUTH", "IntentSender error", e)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "One Tap Failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {

            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val googleIdToken = credential.googleIdToken

                if (googleIdToken != null) {
                    val firebaseCredential =
                        GoogleAuthProvider.getCredential(googleIdToken, null)

                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener { authResult ->
                            val email = authResult.user?.email ?: ""

                            val allowedDomain = "@tezu.ac.in"

                            if (!email.endsWith(allowedDomain, ignoreCase = true)) {
                                auth.signOut()
                                oneTapClient.signOut()

                                Toast.makeText(
                                    this,
                                    "Only university emails allowed",
                                    Toast.LENGTH_LONG
                                ).show()

                                return@addOnSuccessListener
                            }

                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show()
                        }
                }

            } catch (e: Exception) {
                Log.e("AUTH", "One Tap Result Error", e)
            }
        }
    }

}
