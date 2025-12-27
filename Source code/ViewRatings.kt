package com.example.miniproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import kotlinx.coroutines.tasks.await

data class Review(
    val rating: Int = 0,
    val reviewText: String = "",
    val instructorName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewRatings(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var selectedSchool by remember { mutableStateOf("") }
    var schoolExpanded by remember { mutableStateOf(false) }

    var selectedDept by remember { mutableStateOf("") }
    var deptExpanded by remember { mutableStateOf(false) }

    var selectedCourse by remember { mutableStateOf("") }
    var courseExpanded by remember { mutableStateOf(false) }

    var courseList by remember { mutableStateOf<List<String>>(emptyList()) }
    var avgRating by remember { mutableStateOf<Double?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    var hasViewed by remember { mutableStateOf(false) }

    // ---- SAME DATA AS ReviewCourse.kt ----
    val schools = listOf(
        "School of Engineering",
        "School of Humanities & Social Sciences",
        "School of Management Sciences",
        "School of Multidisciplinary Studies",
        "School of Sciences"
    )

    val departmentsBySchool = mapOf(
        "School of Engineering" to listOf("App. Sci.", "CE", "CSE", "Design", "EE", "ECE", "Energy", "FET", "ME", "VSSD"),
        "School of Humanities & Social Sciences" to listOf(
            "Assamese", "Cultural Studies", "English", "FL", "LLT", "MCJ", "Sociology",
            "Hindi", "SW", "Education", "Law", "CWS"
        ),
        "School of Management Sciences" to listOf("Bus. Admin.", "Commerce", "CDM"),
        "School of Multidisciplinary Studies" to listOf("CMR"),
        "School of Sciences" to listOf("Chem. Sci.", "EVS", "Math. Sci.", "MBBT", "Physics")
    )

    val departmentList = departmentsBySchool[selectedSchool] ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Course Ratings") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (!hasViewed) {
                // ---------------- SCHOOL DROPDOWN ----------------
                Text("School of Study", fontWeight = FontWeight.Bold)

                Box {
                    OutlinedTextField(
                        value = selectedSchool,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { schoolExpanded = true },
                        placeholder = { Text("Select School") }
                    )

                    DropdownMenu(
                        expanded = schoolExpanded,
                        onDismissRequest = { schoolExpanded = false }
                    ) {
                        schools.forEach { school ->
                            DropdownMenuItem(
                                text = { Text(school) },
                                onClick = {
                                    selectedSchool = school
                                    selectedDept = ""
                                    selectedCourse = ""
                                    avgRating = null
                                    reviews = emptyList()
                                    courseList = emptyList()
                                    schoolExpanded = false
                                }
                            )
                        }
                    }
                }

                // ---------------- DEPARTMENT DROPDOWN ----------------
                if (selectedSchool.isNotEmpty()) {

                    Text("Department", fontWeight = FontWeight.Bold)

                    Box {
                        OutlinedTextField(
                            value = selectedDept,
                            onValueChange = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { deptExpanded = true },
                            placeholder = { Text("Select Department") }
                        )

                        DropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false }
                        ) {
                            departmentList.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept) },
                                    onClick = {
                                        selectedDept = dept
                                        selectedCourse = ""
                                        avgRating = null
                                        reviews = emptyList()
                                        deptExpanded = false

                                        coroutineScope.launch {
                                            val snapshot = db.collection("reviews")
                                                .whereEqualTo("school", selectedSchool)
                                                .whereEqualTo("department", dept)
                                                .get()
                                                .await()

                                            courseList = snapshot.documents
                                                .mapNotNull { it.getString("courseName") }
                                                .distinct()
                                                .sorted()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // ---------------- COURSE DROPDOWN ----------------
                if (selectedDept.isNotEmpty()) {

                    Text("Course", fontWeight = FontWeight.Bold)

                    Box {
                        OutlinedTextField(
                            value = selectedCourse,
                            onValueChange = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { courseExpanded = true },
                            placeholder = { Text("Select Course") }
                        )

                        DropdownMenu(
                            expanded = courseExpanded,
                            onDismissRequest = { courseExpanded = false }
                        ) {
                            courseList.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course) },
                                    onClick = {
                                        selectedCourse = course
                                        courseExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ---------------- VIEW BUTTON ----------------
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // existing Firestore logic
                            val docId = "${selectedSchool}_${selectedDept}_${selectedCourse}"
                                .replace(" ", "_")

                            val ratingDoc = db.collection("course_ratings")
                                .document(docId)
                                .get()
                                .await()

                            avgRating = ratingDoc.getDouble("avgRating")

                            val reviewSnap = db.collection("reviews")
                                .whereEqualTo("school", selectedSchool)
                                .whereEqualTo("department", selectedDept)
                                .whereEqualTo("courseName", selectedCourse)
                                .get()
                                .await()

                            reviews = reviewSnap.documents.map {
                                Review(
                                    rating = it.getLong("rating")?.toInt() ?: 0,
                                    reviewText = it.getString("reviewText") ?: "",
                                    instructorName = it.getString("instructorName") ?: ""
                                )
                            }

                            hasViewed = true   // to show only the reviews after pressing "View"
                        }
                    },
                    enabled = selectedSchool.isNotEmpty()
                            && selectedDept.isNotEmpty()
                            && selectedCourse.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View")
                }
            }

            if (hasViewed) {
                // ---------------- AVERAGE RATING ----------------
                avgRating?.let {
                    Text(
                        "Average Rating: ${"%.2f".format(it)} / 5",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------- REVIEWS ----------------
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reviews) { review ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Instructor: ${review.instructorName}",
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Rating: ${review.rating} / 5")
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(review.reviewText)
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        hasViewed = false
                        avgRating = null
                        reviews = emptyList()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Another Course")
                }
            }

        }

    }
}
