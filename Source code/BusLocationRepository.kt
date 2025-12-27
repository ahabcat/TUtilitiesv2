package com.example.miniproject

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

object BusLocationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val busRef = db.collection("bus_locations")

    suspend fun updateBusLocation(
        busId: String,
        latitude: Double,
        longitude: Double
    ) {
        val data = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        busRef.document(busId).set(data)
    }
}