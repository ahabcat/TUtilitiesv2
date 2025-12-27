package com.example.miniproject

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

object PostRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val postsRef = firestore.collection("posts")

    /**
     * Create a new post
     */
    suspend fun createPost(
        header: String,
        body: String
    ): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return false

        val postData = hashMapOf(
            "header" to header,
            "body" to body,
            "authorEmail" to user.email,
            "upvoteCount" to 0,
            "upvotedBy" to emptyList<String>(),
            "timestamp" to FieldValue.serverTimestamp()
        )

        postsRef.add(postData).await()
        return true
    }

    /**
     * Listen to posts in real-time
     */
    fun listenToPosts(
        onUpdate: (List<Post>) -> Unit
    ): ListenerRegistration {
        return postsRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                val posts = snapshot.documents.mapNotNull { doc ->
                    try {
                        Post(
                            id = doc.id,
                            header = doc.getString("header") ?: "",
                            body = doc.getString("body") ?: "",
                            authorEmail = doc.getString("authorEmail") ?: "",
                            upvoteCount = doc.getLong("upvoteCount")?.toInt() ?: 0,
                            upvotedBy = doc.get("upvotedBy") as? List<String> ?: emptyList(),
                            timestamp = doc.getTimestamp("timestamp")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                onUpdate(posts)
            }
    }

    /**
     * Upvote a post (only once per user)
     * Uses Firestore transaction (SAFE)
     */
    suspend fun upvotePost(postId: String): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return false
        val userEmail = user.email ?: return false

        val postRef = postsRef.document(postId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)

                if (!snapshot.exists()) return@runTransaction false

                val upvotedBy =
                    snapshot.get("upvotedBy") as? List<String> ?: emptyList()

                if (upvotedBy.contains(userEmail)) {
                    return@runTransaction false
                }

                transaction.update(
                    postRef,
                    mapOf(
                        "upvotedBy" to upvotedBy + userEmail,
                        "upvoteCount" to FieldValue.increment(1)
                    )
                )
                true
            }.await()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetch a single post (used for exporting)
     */
    suspend fun getPostById(postId: String): Post? {
        val doc = postsRef.document(postId).get().await()
        if (!doc.exists()) return null

        return Post(
            id = doc.id,
            header = doc.getString("header") ?: "",
            body = doc.getString("body") ?: "",
            authorEmail = doc.getString("authorEmail") ?: "",
            upvoteCount = doc.getLong("upvoteCount")?.toInt() ?: 0,
            upvotedBy = doc.get("upvotedBy") as? List<String> ?: emptyList(),
            timestamp = doc.getTimestamp("timestamp")
        )
    }
}