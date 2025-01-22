package com.project.wellnesswise.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.components.ui.HealthAssessmentMode
import com.project.wellnesswise.components.ui.LoadingAnimation
import com.project.wellnesswise.components.ui.MainProfileView
import com.project.wellnesswise.components.ui.MyPasswordField
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.viewModels.AuthViewModel
import com.project.wellnesswise.viewModels.RegistrationViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(authViewModel: AuthViewModel, registrationViewModel: RegistrationViewModel) {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentView by remember { mutableStateOf("main") }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showDeleteError by remember { mutableStateOf<String?>(null) }
    var deletePassword by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = LocalContext.current

    // Use dynamic color scheme
    val colorScheme = when {
        useDarkIcons -> dynamicLightColorScheme(context)
        else -> dynamicDarkColorScheme(context)
    }

    LaunchedEffect(colorScheme) {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = useDarkIcons
        )
    }
    LaunchedEffect(user) {
        user?.let { currentUser ->
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    userData = document.data
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    val navigateBack: () -> Unit = {
        when (currentView) {
            "main" -> WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
            "edit" -> currentView = "main"
            "healthAssessment" -> currentView = "edit"
            else -> currentView = "main"
        }
    }
    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            val content: @Composable () -> Unit = {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentView) {
                        "main" -> MainProfileView(
                            userData = userData,
                            isLoading = isLoading,
                            onEditClick = { currentView = "edit" },
                            onDeleteAccountClick = { showDeleteConfirmation = true }
                        )
                        "edit" -> EditProfileScreen(
                            userData = userData,
                            onSave = { updatedData ->
                                user?.let { currentUser ->
                                    firestore.collection("users").document(currentUser.uid)
                                        .update(updatedData)
                                        .addOnSuccessListener {
                                            userData = userData?.toMutableMap()
                                                ?.apply { putAll(updatedData) }
                                            currentView = "main"
                                        }
                                }
                            },
                            onBack = navigateBack,
                            onEditHealthAssessment = {
                                currentView = "healthAssessment"
                                registrationViewModel.loadExistingHealthAssessmentData(userData)
                                registrationViewModel.currentMode.value = HealthAssessmentMode.EDIT
                            },
                            colorScheme = colorScheme
                        )
                        "healthAssessment" -> HealthAssessmentScreen(
                            registrationViewModel = registrationViewModel,
                            mode = HealthAssessmentMode.EDIT,
                            onSave = {
                                val updatedHealthData = registrationViewModel.getHealthAssessmentData()
                                user?.let { currentUser ->
                                    firestore.collection("users").document(currentUser.uid)
                                        .update(updatedHealthData)
                                        .addOnSuccessListener {
                                            userData = userData?.toMutableMap()
                                                ?.apply { putAll(updatedHealthData) }
                                            currentView = "edit"
                                        }
                                }
                            },
                            onBack = {
                                registrationViewModel.setMode(HealthAssessmentMode.SIGNUP)
                                currentView = "edit"
                            }
                        )
                    }
                }
            }


            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isDeleting) {
                            showDeleteConfirmation = false
                            deletePassword = ""
                        }
                    },
                    title = { Text("Delete Account") },
                    text = {
                        Column {
                            Text("Are you sure you want to delete your account? This action cannot be undone. Please enter your password to confirm.")
                            Spacer(modifier = Modifier.height(16.dp))
                            MyPasswordField(
                                labelValue = "Password",
                                initialValue = deletePassword,
                                onTextSelected = { deletePassword = it },
                                isError = false
                            )
                            if (isDeleting) {
                                Spacer(modifier = Modifier.height(16.dp))
                                LoadingAnimation()
                                Text("Deleting account and all associated viewModels... Please wait.",
                                    modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (!isDeleting) {
                                    isDeleting = true
                                    coroutineScope.launch {
                                        val result = authViewModel.deleteAccount(deletePassword)
                                        isDeleting = false
                                        if (result.isSuccess) {
                                            showDeleteConfirmation = false
                                            WellnessWiseAppRouter.navigateTo(Screen.LoginScreen)
                                        } else {
                                            showDeleteError = result.exceptionOrNull()?.message ?: "An error occurred"
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = !isDeleting
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                if (!isDeleting) {
                                    showDeleteConfirmation = false
                                    deletePassword = ""
                                }
                            },
                            enabled = !isDeleting
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }


            if (currentView == "main") {
                NavigationDrawer(
                    content = content,
                    onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) },
                    onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) },
                    onLogoutClick = { authViewModel.logOut() }, userData = userData,
                    currentScreen = Screen.UserProfileScreen,
                    onHealthDataClick = {WellnessWiseAppRouter.navigateTo((Screen.HealthDataViewEditScreen))},
                    onAssessmentClick = {WellnessWiseAppRouter.navigateTo(Screen.HealthAssessmentEditViewScreen)}
                )
            } else {
                content()
            }

            SystemBackButtonHandler {
                navigateBack()
            }
        }
    }
}