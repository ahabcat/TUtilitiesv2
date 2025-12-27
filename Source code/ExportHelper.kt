package com.example.miniproject

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast

fun downloadPostAsText(
    context: Context,
    post: Post
) {
    val fileName = "post_${post.id}.txt"

    val content = buildString {
        appendLine("Title:")
        appendLine(post.header)
        appendLine()
        appendLine("Body:")
        appendLine(post.body)
        appendLine()
        appendLine("Upvotes: ${post.upvoteCount}")
        appendLine()
        appendLine("Upvoted by:")
        post.upvotedBy.forEach { appendLine(it) }
    }

    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri = resolver.insert(
        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
        contentValues
    )

    uri?.let {
        resolver.openOutputStream(it)?.use { output ->
            output.write(content.toByteArray())
        }

        Toast.makeText(context, "File downloaded to Downloads", Toast.LENGTH_LONG).show()
    }
}



/*
fun exportPostToFile(context: Context, post: Post) {
    val content = buildString {
        appendLine("Header:")
        appendLine(post.header)
        appendLine()
        appendLine("Body:")
        appendLine(post.body)
        appendLine()
        appendLine("Author:")
        appendLine(post.authorEmail)
        appendLine()
        appendLine("Upvotes: ${post.upvoteCount}")
        appendLine("Upvoted By:")
        post.upvotedBy.forEach {
            appendLine(it)
        }
    }

    val fileName = "post_${post.id}.txt"

    val file = File(context.getExternalFilesDir(null), fileName)
    file.writeText(content)

    Toast.makeText(
        context,
        "Saved to ${file.absolutePath}",
        Toast.LENGTH_LONG
    ).show()
}

 */
