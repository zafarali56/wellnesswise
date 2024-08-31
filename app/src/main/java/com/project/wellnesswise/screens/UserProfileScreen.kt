import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.wellnesswise.components.ui.NavigationDrawer
import com.project.wellnesswise.data.AuthViewModel
import com.project.wellnesswise.navigations.Screen
import com.project.wellnesswise.navigations.SystemBackButtonHandler
import com.project.wellnesswise.navigations.WellnessWiseAppRouter

@Composable
fun UserProfileScreen(authViewModel: AuthViewModel) {
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentView by remember { mutableStateOf("main") }

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

    val content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentView) {
                "main" -> MainProfileView(
                    user = user,
                    userData = userData,
                    isLoading = isLoading,
                    onMedicalHistoryClick = { currentView = "medical" },
                    onHabitsClick = { currentView = "habits" }
                )
                "medical" -> MedicalHistoryView(
                    userData = userData,
                    onBack = { currentView = "main" }
                )
                "habits" -> HabitsView(
                    userData = userData,
                    onBack = { currentView = "main" }
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
        when (currentView) {
            "medical", "habits" -> currentView = "main"
            else -> WellnessWiseAppRouter.navigateTo(Screen.HomeScreen)
        }
    }
}