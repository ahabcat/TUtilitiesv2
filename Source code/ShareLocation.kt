package com.example.miniproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLocation(navController: NavController) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var isSharing by remember { mutableStateOf(false) }

    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                coroutineScope.launch {
                    BusLocationRepository.updateBusLocation(
                        busId = "bus_1",
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Share Bus Location") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                if (isSharing) "Location sharing is ACTIVE"
                else "Location sharing is OFF",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val hasPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission) {
                        Toast.makeText(context, "Location permission required", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (!isSharing) {
                        locationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                        isSharing = true
                    } else {
                        locationClient.removeLocationUpdates(locationCallback)
                        isSharing = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSharing) "Stop Sharing" else "Start Sharing")
            }
        }
    }
}
