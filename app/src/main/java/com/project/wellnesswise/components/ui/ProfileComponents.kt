import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.LoadingAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainProfileView(
    user: FirebaseUser?,
    userData: Map<String, Any>?,
    isLoading: Boolean,
    onEditClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            item {
                ProfileHeader(userData, isLoading)
            }

            if (isLoading) {
                item { LoadingAnimation() }
            } else {
                userData?.let { data ->
                    val groupedData = groupProfileData(data)
                    items(groupedData) { (groupTitle, items) ->
                        ProfileSection(groupTitle, items)
                    }
                }
            }

            item {
                ActionsSection(onEditClick, onDeleteAccountClick)
            }
        }
    }

@Composable
fun ProfileHeader(userData: Map<String, Any>?, isLoading: Boolean) {
    val user = FirebaseAuth.getInstance().currentUser
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                LoadingAnimation()
            } else {
                userData?.let { data ->
                    Text(
                        text = data["fullName"] as? String ?: "N/A",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = user?.email ?: "user@example.com",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
@Composable
fun ProfileSection(title: String, items: List<Pair<String, Any?>>) {
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
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
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
                    ProfileItem(label, value)
                }
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: Any?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value?.toString() ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
@Composable
fun ActionsSection(onEditClick: () -> Unit, onDeleteAccountClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ButtonComponent(
            value = "Edit Profile",
            onButtonClicked = onEditClick,
            isEnabled = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onDeleteAccountClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Delete Account")
        }
    }
}

fun groupProfileData(data: Map<String, Any>): List<Pair<String, List<Pair<String, Any?>>>> {
    return listOf(
        "Personal Information" to listOf(
            "Age" to data["age"],
            "Gender" to data["gender"],
            "Height" to data["height"],
            "Weight" to data["weight"],
            "Waist Circumference" to data["waistCircumference"]
        ),
        "Health Metrics" to listOf(
            "Blood Pressure" to data["bloodPressure"],
            "Heart Rate" to data["heartRate"],
            "Blood Sugar" to data["bloodSugar"],
            "Cholesterol" to data["cholesterol"],
            "Triglycerides" to data["triglycerides"]
        ),
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

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}