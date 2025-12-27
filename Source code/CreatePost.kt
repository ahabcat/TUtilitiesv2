package com.example.miniproject

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePost(navController: NavController) {

    var header by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Post") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // -------- HEADER --------
            OutlinedTextField(
                value = header,
                onValueChange = { header = it },
                label = { Text("Post Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // -------- BODY --------
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Post Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                minLines = 6,
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(10.dp))

            // -------- POST BUTTON --------
            Button(
                onClick = {
                    coroutineScope.launch {
                        isPosting = true

                        val success = PostRepository.createPost(
                            header = header.trim(),
                            body = body.trim()
                        )

                        isPosting = false

                        if (success) {
                            Toast.makeText(
                                context,
                                "Post published successfully!",
                                Toast.LENGTH_LONG
                            ).show()

                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to publish post",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = header.isNotBlank()
                        && body.isNotBlank()
                        && !isPosting
            ) {
                Text(if (isPosting) "Posting..." else "Post")
            }
        }
    }
}
