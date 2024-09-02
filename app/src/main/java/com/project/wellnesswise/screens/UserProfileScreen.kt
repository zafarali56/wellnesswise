import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.data.Habit
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter
import com.project.wellnesswise.screens.EditHabitsScreen
import com.project.wellnesswise.screens.EditMedicalHistoryScreen
import com.project.wellnesswise.screens.EditProfileScreen

@Composable
fun UserProfileScreen(authViewModel: AuthViewModel) {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentView by remember { mutableStateOf("main") }

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
            "medical", "habits", "edit" -> currentView = "main"
            "editMedical", "editHabits" -> currentView = "edit"
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
                            user = user,
                            userData = userData,
                            isLoading = isLoading,
                            onMedicalHistoryClick = { currentView = "medical" },
                            onHabitsClick = { currentView = "habits" },
                            onEditClick = { currentView = "edit" }
                        )

                        "medical" -> MedicalHistoryView(
                            userData = userData,
                            onBack = navigateBack
                        )

                        "habits" -> HabitsView(
                            userData = userData,
                            onBack = navigateBack
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
                            onEditMedicalHistory = { currentView = "editMedical" },
                            onEditHabits = { currentView = "editHabits" },
                            colorScheme = colorScheme
                        )

                        "editMedical" -> EditMedicalHistoryScreen(
                            medicalHistory = userData?.get("medicalHistory") as? Map<String, String>
                                ?: emptyMap(),
                            onSave = { updatedMedicalHistory ->
                                user?.let { currentUser ->
                                    firestore.collection("users").document(currentUser.uid)
                                        .update("medicalHistory", updatedMedicalHistory)
                                        .addOnSuccessListener {
                                            userData = userData?.toMutableMap()?.apply {
                                                put("medicalHistory", updatedMedicalHistory)
                                            }
                                            currentView = "edit"
                                        }
                                }
                            },
                            onBack = navigateBack,
                            colorScheme = colorScheme
                        )

                        "editHabits" -> EditHabitsScreen(
                            habits = (userData?.get("habits") as? List<String>)?.map {
                                Habit.valueOf(
                                    it
                                )
                            } ?: emptyList(),
                            onSave = { updatedHabits ->
                                user?.let { currentUser ->
                                    val habitStrings = updatedHabits.map { it.name }
                                    firestore.collection("users").document(currentUser.uid)
                                        .update("habits", habitStrings)
                                        .addOnSuccessListener {
                                            userData = userData?.toMutableMap()?.apply {
                                                put("habits", habitStrings)
                                            }
                                            currentView = "edit"
                                        }
                                }
                            },
                            onBack = navigateBack, colorScheme = colorScheme
                        )
                    }
                }
            }

            if (currentView == "main") {
                NavigationDrawer(
                    content = content,
                    onProfileClick = { WellnessWiseAppRouter.navigateTo(Screen.UserProfileScreen) },
                    onHomeClick = { WellnessWiseAppRouter.navigateTo(Screen.HomeScreen) },
                    onLogoutClick = { authViewModel.logOut() }
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