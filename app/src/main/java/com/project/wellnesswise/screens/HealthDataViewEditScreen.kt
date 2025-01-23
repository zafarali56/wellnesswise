package com.project.wellnesswise.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
    authViewModel: AuthViewModel // Pass AuthViewModel for logout functionality
) {
    // State to hold health data
    var healthData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Editable fields
    var waistCircumference by remember { mutableDoubleStateOf(0.0) }
    var triglycerides by remember { mutableDoubleStateOf(0.0) }
    var cholesterol by remember { mutableDoubleStateOf(0.0) }
    var bloodSugar by remember { androidx.compose.runtime.mutableDoubleStateOf(0.0) }
    var systolic by remember { mutableStateOf(0) }
    var diastolic by remember { mutableStateOf(0) }
    var heartRate by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        fetch_HealthDataFromFirebase { data ->
            healthData = data
            // Debug log to verify fetched data
            println("Fetched health data: $data")

            // Initialize editable fields with current data
            waistCircumference = (data?.get("waistCircumference") as? Double) ?: 0.0
            triglycerides = (data?.get("triglycerides") as? Double) ?: 0.0
            cholesterol = (data?.get("cholesterol") as? Double) ?: 0.0
            bloodSugar = (data?.get("bloodSugar") as? Double) ?: 0.0
            heartRate = (data?.get("heartRate") as? Number)?.toInt() ?: 0
            val bloodPressure = (data?.get("bloodPressure") as? String)?.split("/")
            systolic = bloodPressure?.getOrNull(0)?.toIntOrNull() ?: 0
            diastolic = bloodPressure?.getOrNull(1)?.toIntOrNull() ?: 0

            // Debug log to verify initialized fields
            println("Initialized heartRate: $heartRate")
            isLoading = false
        }
    }    // Fetch user data for the NavigationDrawer
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    LaunchedEffect(Unit) {
        fetch_UserData { data ->
            userData = data
        }
    }

    // Determine if the data source is Manual or Google Fit
    val dataSourcePreference = healthData?.get("dataSourcePreference") as? String ?: "MANUAL"

    WellnessWiseTheme {
        NavigationDrawer(
            content = {
                Scaffold(
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
                                    HealthDataSection(
                                        groupTitle,
                                        items,
                                        waistCircumference,
                                        triglycerides,
                                        cholesterol,
                                        bloodSugar,
                                        systolic,
                                        diastolic,
                                        heartRate,
                                        dataSourcePreference,
                                        onWaistCircumferenceChange = { waistCircumference = it },
                                        onTriglyceridesChange = { triglycerides = it },
                                        onCholesterolChange = { cholesterol = it },
                                        onBloodSugarChange = { bloodSugar = it },
                                        onSystolicChange = { systolic = it },
                                        onDiastolicChange = { diastolic = it },
                                        onHeartRateChange = { heartRate = it },
                                        onSave = { field, value ->
                                            updateHealthDataInFirebase(field, value)
                                        }
                                    )
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
            onAssessmentClick = { WellnessWiseAppRouter.navigateTo(Screen.HealthAssessmentEditViewScreen) },
            userData = userData, // Pass user data to NavigationDrawer
            currentScreen = Screen.HealthDataScreen // Set current screen
        )
    }
}

// Function to fetch health data from Firebase
private fun fetch_HealthDataFromFirebase(onDataFetched: (Map<String, Any>?) -> Unit) {
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

// Function to update health data in Firebase
private fun updateHealthDataInFirebase(field: String, value: Any) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) return

    val db = FirebaseFirestore.getInstance()
    val updates = mapOf(field to value)

    db.collection("users").document(userId)
        .update(updates)
        .addOnSuccessListener {
            // Show toast on success
            // You can use a toast utility function here
        }
        .addOnFailureListener {
            // Show toast on failure
        }
}

// Function to fetch user data for the NavigationDrawer
private fun fetch_UserData(onDataFetched: (Map<String, Any>?) -> Unit) {
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
fun EditableHealthDataItem(
    label: String,
    value: Any, // Expects a Double
    onValueChange: (Double) -> Unit, // Accepts a Double
    onSave: () -> Unit
) {
    var textValue by remember { mutableStateOf(value.toString()) }

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
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    textValue = newValue
                    val doubleValue = newValue.toDoubleOrNull() ?: 0.0
                    onValueChange(doubleValue)
                },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
            Button(onClick = onSave) {
                Text("Save")
            }
        }
    }
}
@Composable
fun HealthDataSection(
    title: String,
    items: List<Pair<String, Any?>>,
    waistCircumference: Double,
    triglycerides: Double,
    cholesterol: Double,
    bloodSugar: Double,
    systolic: Int,
    diastolic: Int,
    heartRate: Int,
    dataSourcePreference: String,
    onWaistCircumferenceChange: (Double) -> Unit,
    onTriglyceridesChange: (Double) -> Unit,
    onCholesterolChange: (Double) -> Unit,
    onBloodSugarChange: (Double) -> Unit,
    onSystolicChange: (Int) -> Unit,
    onDiastolicChange: (Int) -> Unit,
    onHeartRateChange: (Int) -> Unit,
    onSave: (String, Any) -> Unit
) {
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
                    when (label) {
                        "Waist Circumference" -> EditableHealthDataItem(
                            label = label,
                            value = waistCircumference,
                            onValueChange = { onWaistCircumferenceChange(it as Double) },
                            onSave = { onSave("waistCircumference", waistCircumference) }
                        )
                        "Triglycerides" -> EditableHealthDataItem(
                            label = label,
                            value = triglycerides,
                            onValueChange = { onTriglyceridesChange(it as Double) },
                            onSave = { onSave("triglycerides", triglycerides) }
                        )
                        "Cholesterol" -> EditableHealthDataItem(
                            label = label,
                            value = cholesterol,
                            onValueChange = { onCholesterolChange(it as Double) },
                            onSave = { onSave("cholesterol", cholesterol) }
                        )
                        "Blood Sugar" -> if (dataSourcePreference == "MANUAL") {
                            EditableHealthDataItem(
                                label = label,
                                value = bloodSugar,
                                onValueChange = { onBloodSugarChange(it as Double) },
                                onSave = { onSave("bloodSugar", bloodSugar) }
                            )
                        } else {
                            HealthDataItem(label, value)
                        }
                        "Blood Pressure" -> if (dataSourcePreference == "MANUAL") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Systolic",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                OutlinedTextField(
                                    value = systolic.toString(),
                                    onValueChange = { onSystolicChange(it.toIntOrNull() ?: 0) },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                                Text(
                                    text = "Diastolic",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                OutlinedTextField(
                                    value = diastolic.toString(),
                                    onValueChange = { onDiastolicChange(it.toIntOrNull() ?: 0) },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                                Button(onClick = { onSave("bloodPressure", "$systolic/$diastolic") }) {
                                    Text("Save")
                                }
                            }
                        } else {
                            HealthDataItem(label, value)
                        }
                        "Heart Rate" -> if (dataSourcePreference == "MANUAL") {
                            EditableHealthDataItem(
                                label = label,
                                value = heartRate,
                                onValueChange = { onHeartRateChange(it.toInt()) },
                                onSave = { onSave("heartRate", heartRate) }
                            )
                        } else {
                            HealthDataItem(label, value)
                        }
                        else -> HealthDataItem(label, value)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthDataItem(
    label: String,
    value: Any?
) {
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

// Function to group health data based on data source preference
private fun groupHealthData(data: Map<String, Any>): List<Pair<String, List<Pair<String, Any?>>>> {
    return listOf(
        "Health Metrics" to listOf(
            "Cholesterol" to data["cholesterol"],
            "Heart Rate" to data["heartRate"],
            "Blood Pressure" to data["bloodPressure"],
            "Waist Circumference" to data["waistCircumference"],
            "Triglycerides" to data["triglycerides"],
            "Blood Sugar" to data["bloodSugar"]
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