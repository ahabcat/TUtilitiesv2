package com.example.miniproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackBus(navController: NavController) {

    var busLat by remember { mutableDoubleStateOf(0.0) }
    var busLng by remember { mutableDoubleStateOf(0.0) }
    var hasLocation by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()

    val cameraPositionState = rememberCameraPositionState()

    val markerState = remember {
        MarkerState(position = LatLng(0.0, 0.0))
    }

    DisposableEffect(Unit) {
        val listener = firestore
            .collection("bus_locations")
            .document("bus_1")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val lat = snapshot.getDouble("latitude")
                    val lng = snapshot.getDouble("longitude")

                    if (lat != null && lng != null) {
                        busLat = lat
                        busLng = lng
                        hasLocation = true
                    }
                }
            }

        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Track Bus") }
            )
        }
    ) { padding ->

        if (!hasLocation) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Bus location not available yet")
            }
        } else {

            val busPosition = LatLng(busLat, busLng)

            LaunchedEffect(busPosition) {
                markerState.position = busPosition
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(busPosition, 15f)
            }

            GoogleMap(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = markerState,
                    title = "Bus Location"
                )
            }
        }
    }
}
