package com.example.miniproject

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val header: String = "",
    val body: String = "",
    val authorEmail: String = "",
    val upvoteCount: Int = 0,
    val upvotedBy: List<String> = emptyList(),
    val timestamp: Timestamp? = null
)