import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.LoadingAnimation
import com.project.wellnesswise.components.ui.UserImg

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
        ) {
            item {
                ProfileHeader(userData, isLoading)
            }

            if (isLoading) {
                item { LoadingAnimation() }
            } else {
                userData?.let { data ->
                    val groupedData = groupProfileData(data)
                    groupedData.forEach { (groupTitle, items) ->
                        item {
                            ProfileSection(groupTitle, items)
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ButtonComponent(value = "Edit Profile", onButtonClicked = onEditClick, isEnabled = true)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDeleteAccountClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Account")
                    }
                }
        }
    }
}

@Composable
fun ProfileHeader(userData: Map<String, Any>?, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserImg()
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            LoadingAnimation()
        } else {
            userData?.let { data ->
                Text(
                    text = data["fullName"] as? String ?: "N/A",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileSection(title: String, items: List<Pair<String, Any?>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            items.forEach { (label, value) ->
                ProfileItem(label, value)
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: Any?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = getIconForLabel(label)
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value?.toString() ?: "N/A",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

fun getIconForLabel(label: String): ImageVector {
    return when (label.toLowerCase()) {
        "age" -> Icons.Default.Cake
        "gender" -> Icons.Default.Person
        "height", "weight" -> Icons.Default.FitnessCenter
        "blood pressure", "heart rate", "blood sugar", "cholesterol" -> Icons.Default.Attribution
        "physical activity" -> Icons.Default.DirectionsRun
        "sleep hours" -> Icons.Default.Bedtime
        "diet quality" -> Icons.Default.Restaurant
        "alcohol consumption" -> Icons.Default.LocalBar
        "smoking" -> Icons.Default.SmokingRooms
        "stress level" -> Icons.Default.Psychology
        "air quality index"-> Icons.Default.Air
        "exposure to pollutants" -> Icons.Default.WbSunny
        else -> Icons.Default.Info
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}