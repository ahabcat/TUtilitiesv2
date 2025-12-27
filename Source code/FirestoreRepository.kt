package com.example.miniproject

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val reviewsRef = db.collection("reviews")
    private val courseRatingsRef = db.collection("course_ratings")


    //Checks if user already reviewed this course.

    suspend fun hasExistingReview(
    userId: String,
    school: String,
    dept: String,
    courseName: String
    ): Boolean {
    val query = reviewsRef
    .whereEqualTo("userId", userId)
    .whereEqualTo("school", school)
    .whereEqualTo("department", dept)
    .whereEqualTo("courseName", courseName)
    .get()
    .await()

    return !query.isEmpty
    }

    /**
     * Saves review + updates rating aggregates.
     */
    suspend fun saveReview(
        school: String,
        department: String,
        courseName: String,
        instructorName: String,
        rating: Int,
        reviewText: String
    ): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        //  ---- Prevent Duplicate Submissions ----
         if (hasExistingReview(userId, school, department, courseName)) {
            return false
         }

        // ---- Save actual review ----
        val reviewData = hashMapOf(
            "userId" to userId,
            "school" to school,
            "department" to department,
            "courseName" to courseName,
            "instructorName" to instructorName,
            "rating" to rating,
            "reviewText" to reviewText,
            "timestamp" to FieldValue.serverTimestamp()
        )

        reviewsRef.add(reviewData).await()

        // ---- Update aggregate rating ----
        val docId = "${school}_${department}_${courseName}".replace(" ", "_")
        val courseDoc = courseRatingsRef.document(docId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(courseDoc)

            val newCount: Int
            val newTotal: Int
            val newAvg: Double

            if (snapshot.exists()) {
                val oldCount = snapshot.getLong("ratingCount")?.toInt() ?: 0
                val oldTotal = snapshot.getLong("totalRating")?.toInt() ?: 0

                newCount = oldCount + 1
                newTotal = oldTotal + rating
            } else {
                newCount = 1
                newTotal = rating
            }

            newAvg = newTotal.toDouble() / newCount

            val updateData = mapOf(
                "ratingCount" to newCount,
                "totalRating" to newTotal,
                "avgRating" to newAvg
            )

            transaction.set(courseDoc, updateData)
        }.await()

        return true
    }
}