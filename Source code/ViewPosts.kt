package com.example.miniproject

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPosts() {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    //  Firestore realtime listener
    DisposableEffect(Unit) {
        val listener = PostRepository.listenToPosts { updatedPosts ->
            posts = updatedPosts
        }

        onDispose {
            listener.remove()
        }
    }

    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No posts available")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                //.systemBarsPadding()          //either use this or use both the paddings below
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(posts) { post ->
                PostCard(
                    post = post,
                    onUpvote = {
                        coroutineScope.launch {
                            val success =
                                PostRepository.upvotePost(post.id)

                            if (!success) {
                                Toast
                                    .makeText(
                                        context,
                                        "You have already upvoted",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                    },
                    onDownload = {
                        coroutineScope.launch {
                            val fullPost =
                                PostRepository.getPostById(post.id)

                            fullPost?.let {
                                downloadPostAsText(context, post)
                            }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun PostCard(
    post: Post,
    onUpvote: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = post.header,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Author: ${post.authorEmail}",
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = "Upvotes: ${post.upvoteCount}",
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(onClick = onUpvote) {
                    Text("Upvote")
                }

                OutlinedButton(onClick = onDownload) {
                    Text("Download")
                }
            }
        }
    }
}

