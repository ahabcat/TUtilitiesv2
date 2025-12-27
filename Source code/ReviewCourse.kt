package com.example.miniproject

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewCourse(navController: NavController)
{
    var selectedSchool by remember { mutableStateOf("") }
    var schoolExpanded by remember { mutableStateOf(false) }

    var selectedDept by remember { mutableStateOf("") }
    var deptExpanded by remember { mutableStateOf(false) }

    var courseExpanded by remember { mutableStateOf(false) }
    var courseList by remember { mutableStateOf<List<String>>(emptyList()) }

    var courseName by remember { mutableStateOf("") }
    var instructorName by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(-1) }
    var reviewText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    val schools = listOf(
        "School of Engineering",
        "School of Humanities & Social Sciences",
        "School of Management Sciences",
        "School of Multidisciplinary Studies",
        "School of Sciences"
    )

    val departmentsBySchool = mapOf(
        "School of Engineering" to listOf("App. Sci.", "CE", "CSE", "Design", "EE", "ECE", "Energy", "FET", "ME",  "VSSD"),
        "School of Humanities & Social Sciences" to listOf("Assamese", "Cultural Studies", "English", "FL", "LLT", "MCJ", "Sociology",
            "Hindi", "SW", "Education", "Law", "CWS"
        ),
        "School of Management Sciences" to listOf("Bus. Admin.", "Commerce", "CDM"),
        "School of Multidisciplinary Studies" to listOf("CMR"),
        "School of Sciences" to listOf("Chem. Sci.", "EVS", "Math. Sci.", "MBBT", "Physics")
    )

    val departmentList = departmentsBySchool[selectedSchool] ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Review a Course") }
            )
        },

        bottomBar = { HomeBottomBar(navController) }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                //.imePadding()        //if imePadding below does not fix then change the order of occurrence
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------------- SCHOOL DROPDOWN ----------------
            Text("School of Study", fontWeight = FontWeight.Bold)

            Box {
                OutlinedTextField(
                    value = selectedSchool,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { schoolExpanded = true },
                    enabled = false,
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
                                selectedDept = ""            // reset department
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deptExpanded = true },
                        enabled = false,
                        placeholder = { Text("Select Department") }
                    )

                    DropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false },
                    ) {
                        departmentList.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept) },
                                onClick = {
                                    selectedDept = dept
                                    courseName = ""
                                    courseList = emptyList()
                                    deptExpanded = false

                                    coroutineScope.launch {
                                        val snapshot = FirebaseFirestore.getInstance()
                                            .collection("courses")
                                            .whereEqualTo("school", selectedSchool)
                                            .whereEqualTo("department", dept)
                                            .get()
                                            .await()

                                        courseList = snapshot.documents
                                            .mapNotNull { it.getString("courseName") }
                                            .sorted()
                                    }
                                }

                            )
                        }
                    }
                }

                // ---------------- COURSE DROPDOWN ----------------
                if (selectedDept.isNotEmpty()) {

                    Text("Course", fontWeight = FontWeight.Bold)

                    Box {
                        OutlinedTextField(
                            value = courseName,
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
                                        courseName = course
                                        courseExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ---------------- INSTRUCTOR NAME ----------------
            OutlinedTextField(
                value = instructorName,
                onValueChange = { instructorName = it },
                label = { Text("Instructor Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------------- STAR RATING (1–10) ----------------
            Text("Rating (0–5)", fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp,alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..5) {           //modified here to start from 0 and above rating..=(-1)
                    Icon(
                        painter = painterResource(
                            id = if (i <= rating)
                                R.drawable.baseline_star_24
                            else
                                R.drawable.baseline_star_outline_24
                        ),
                        contentDescription = "Star $i",
                        tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { rating = i }
                    )
                }
            }

            // ---------------- WRITTEN REVIEW ----------------
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                label = { Text("Write Review") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ---------------- SUBMIT BUTTON ----------------
            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = FirestoreRepository.saveReview(
                            selectedSchool,
                            selectedDept,
                            courseName,
                            instructorName,
                            rating,
                            reviewText
                        )

                        if (!success) {
                            Toast.makeText(context, "You already reviewed this course!", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        Toast.makeText(context, "Review submitted!", Toast.LENGTH_LONG).show()

                        navController.navigate("home") {
                            popUpTo("reviewcourse") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSchool.isNotEmpty()
                        && selectedDept.isNotEmpty()
                        && courseName.isNotEmpty()
                        && instructorName.isNotEmpty()
                        && rating != -1
                        && reviewText.isNotEmpty()
            ) {
                Text("Submit Review")
            }

        }
    }
}
