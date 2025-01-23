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
import com.project.wellnesswise.components.ui.HealthAssessmentMode
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.ui.theme.WellnessWiseTheme
import com.project.wellnesswise.viewModels.AuthViewModel
import com.project.wellnesswise.viewModels.RegistrationViewModel

@Composable
fun HealthAssessmentViewEditScreen(
    authViewModel: AuthViewModel, // Pass AuthViewModel for logout functionality
    registrationViewModel: RegistrationViewModel // Pass RegistrationViewModel for health assessment data
) {
    // State to hold health assessment data
    var healthAssessmentData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch data from Firebase when the screen is launched
    LaunchedEffect(Unit) {
        fetchHealthAssessmentDataFromFirebase { data ->
            healthAssessmentData = data
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

    // Determine the current view (main or edit)
    var currentView by remember { mutableStateOf("main") }

    WellnessWiseTheme {
        NavigationDrawer(
            content = {
                Scaffold(
                    floatingActionButton = {
                        if (currentView == "main") {
                            FloatingActionButton(
                                onClick = {
                                    // Navigate to the edit screen
                                    currentView = "edit"
                                    registrationViewModel.loadExistingHealthAssessmentData(healthAssessmentData)
                                    registrationViewModel.currentMode.value = HealthAssessmentMode.EDIT
                                },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit health assessment data",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { paddingValues ->
                    when (currentView) {
                        "main" -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(horizontal = 5.dp)
                            ) {
                                if (isLoading) {
                                    item { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                                } else {
                                    healthAssessmentData?.let { data ->
                                        val groupedData = groupHealthAssessmentData(data)
                                        items(groupedData) { (groupTitle, items) ->
                                            HealthDataSections(groupTitle, items)
                                        }
                                    }
                                }
                            }
                        }
                        "edit" -> {
                            HealthAssessmentScreen(
                                registrationViewModel = registrationViewModel,
                                mode = HealthAssessmentMode.EDIT,
                                onSave = {
                                    val updatedHealthData = registrationViewModel.getHealthAssessmentData()
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        val db = FirebaseFirestore.getInstance()
                                        db.collection("users").document(userId)
                                            .update(updatedHealthData)
                                            .addOnSuccessListener {
                                                healthAssessmentData = healthAssessmentData?.toMutableMap()
                                                    ?.apply { putAll(updatedHealthData) }
                                                currentView = "main"
                                            }
                                    }
                                },
                                onBack = {
                                    registrationViewModel.setMode(HealthAssessmentMode.SIGNUP)
                                    currentView = "main"
                                }
                            )
                        }
                    }
                }
            },
            onLogoutClick = { authViewModel.logOut() }, // Handle logout
            onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) }, // Navigate to profile
            onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) }, // Navigate to home
            onHealthDataClick = { WellnessWiseAppRouter.navigateTo(Screen.HealthDataViewEditScreen) }, // Navigate to health data
            onAssessmentClick = { WellnessWiseAppRouter.navigateTo(Screen.HealthAssessmentEditViewScreen) }, // Navigate to health assessment screen
            userData = userData, // Pass user data to NavigationDrawer
            currentScreen = Screen.HealthAssessmentEditViewScreen // Set current screen
        )
    }
    SystemBackButtonHandler {
        WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
    }
}

// Function to fetch health assessment data from Firebase
private fun fetchHealthAssessmentDataFromFirebase(onDataFetched: (Map<String, Any>?) -> Unit) {
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

// Function to group health assessment data
private fun groupHealthAssessmentData(data: Map<String, Any>): List<Pair<String, List<Pair<String, Any?>>>> {
    return listOf(
        "Lifestyle" to listOf(
            "Physical Activity" to data["physicalActivity"],
            "Sleep Hours" to data["sleepHours"],
            "Diet Quality" to data["dietQuality"],
            "Alcohol Consumption" to data["alcoholConsumption"],
            "Smoking" to data["smoking"],
            "Stress Level" to data["stressLevel"]
        ),
        "Medical History" to listOf(
            "Chronic Conditions" to data["chronicConditions"],
            "Previous Surgeries" to data["previousSurgeries"],
            "Family Cancer History" to data["familyCancer"],
            "Family Diabetes History" to data["familyDiabetes"],
            "Family Heart Disease History" to data["familyHeart"]
        ),
        "Environmental Factors" to listOf(
            "Air Quality Index" to data["airQualityIndex"],
            "Exposure To Pollutants" to data["exposureToPollutants"],
            "Access to Healthcare" to data["accessToHealthcare"]
        )
    )
}

@Composable
fun HealthDataSections(title: String, items: List<Pair<String, Any?>>) {
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
                    HealthDataItems(label, value)
                }
            }
        }
    }
}

@Composable
fun HealthDataItems(label: String, value: Any?) {
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