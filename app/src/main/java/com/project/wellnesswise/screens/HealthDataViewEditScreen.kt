package com.project.wellnesswise.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.ui.theme.WellnessWiseTheme
import com.project.wellnesswise.viewModels.AuthViewModel

@Composable
fun HealthDataViewEditScreen(
    authViewModel: AuthViewModel, // Pass AuthViewModel for logout functionality
) {
    // State to hold health data
    var healthData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch data from Firebase when the screen is launched
    LaunchedEffect(Unit) {
        fetchHealthDataFromFirebase { data ->
            healthData = data
            isLoading = false
        }
    }

    // Fetch user data for the NavigationDrawer
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    LaunchedEffect(Unit) {
        fetchUserData { data ->
            userData = data
        }
    }

    WellnessWiseTheme {
        NavigationDrawer(
            content = {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {},
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit health data",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 5.dp)
                    ) {
                        if (isLoading) {
                            item { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                        } else {
                            healthData?.let { data ->
                                val groupedData = groupHealthData(data)
                                items(groupedData) { (groupTitle, items) ->
                                    HealthDataSection(groupTitle, items)
                                }
                            }
                        }
                    }
                }
            },
            onLogoutClick = { authViewModel.logOut() }, // Handle logout
            onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) }, // Navigate to profile
            onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) }, // Navigate to home
            onHealthDataClick = { WellnessWiseAppRouter.navigateTo(Screen.HealthDataViewEditScreen) }, // Navigate to health data
            onAssessmentClick = {WellnessWiseAppRouter.navigateTo(Screen.HealthAssessmentEditViewScreen)},
            userData = userData, // Pass user data to NavigationDrawer
            currentScreen = Screen.HealthDataScreen // Set current screen
        )
    }
}

// Function to fetch health data from Firebase
private fun fetchHealthDataFromFirebase(onDataFetched: (Map<String, Any>?) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        onDataFetched(null)
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data
                onDataFetched(data)
            } else {
                onDataFetched(null)
            }
        }
        .addOnFailureListener {
            onDataFetched(null)
        }
}

// Function to fetch user data for the NavigationDrawer
private fun fetchUserData(onDataFetched: (Map<String, Any>?) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        onDataFetched(null)
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data
                onDataFetched(data)
            } else {
                onDataFetched(null)
            }
        }
        .addOnFailureListener {
            onDataFetched(null)
        }
}

@Composable
fun HealthDataSection(title: String, items: List<Pair<String, Any?>>) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (expanded) {
                items.forEach { (label, value) ->
                    HealthDataItem(label, value)
                }
            }
        }
    }
}

@Composable
fun HealthDataItem(label: String, value: Any?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = value?.toString() ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

fun groupHealthData(data: Map<String, Any>): List<Pair<String, List<Pair<String, Any?>>>> {
    return listOf(
        "Health Metrics" to listOf(
            "Cholesterol" to data["cholesterol"],
            "Heart Rate" to data["heartRate"],
            "Blood Pressure" to data["bloodPressure"],
            "Waist Circumference" to data["waistCircumference"],
            "Triglycerides" to data["triglycerides"]
        ),
        "Data Sources" to listOf(
            "Data Source Preference" to data["dataSourcePreference"],
            "Blood Pressure Source" to data["bloodPressureSource"],
            "Blood Sugar Source" to data["bloodSugarSource"],
            "Heart Rate Source" to data["heartRateSource"],
            "Last Updated" to (data["lastUpdated"] as? com.google.firebase.Timestamp)?.toDate(),
            "Last Updated Source" to data["lastUpdatedSource"]
        )
    )
}

