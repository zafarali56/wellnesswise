import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import com.project.wellnesswise.R
import com.project.wellnesswise.components.ui.UserImage
import com.project.wellnesswise.components.ui.formatHabitName

@Composable
fun MainProfileView(
    user: FirebaseUser?,
    userData: Map<String, Any>?,
    isLoading: Boolean,
    onMedicalHistoryClick: () -> Unit,
    onHabitsClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,


    ) {
        item {
            UserImage()
            Spacer(Modifier.height(10.dp))
            Text(
                text = user?.displayName ?: "User Name",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
            )
            Spacer(Modifier.height(10.dp))
        }

        item { UserDataBox(label = "Email", value = user?.email ?: "N/A") }

        if (isLoading) {
            item { Text("Loading user data...") }
        } else {
            userData?.let { data ->
                item { UserDataBox(label = "Full Name", value = data["fullName"] as? String ?: "N/A") }
                item { UserDataBox(label = "Age", value = (data["age"] as? Number)?.toString() ?: "N/A") }
                item { UserDataBox(label = "Gender", value = data["gender"] as? String ?: "N/A") }
                item { UserDataBox(label = "Height", value = (data["height"] as? Number)?.toString() ?: "N/A") }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Button(
                                onClick = onHabitsClick,

                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary))
                            ) {
                                Text("View Habits")
                            }
                            Button(

                                onClick = onMedicalHistoryClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary) )
                            ) {
                                Text("View Medical History")
                            }
                        }
                }
            }
        }
    }
}

@Composable
fun MedicalHistoryView(userData: Map<String, Any>?, onBack: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Medical History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 25.dp, vertical = 15.dp)
        ) {
            val medicalHistory = userData?.get("medicalHistory") as? Map<String, String>
            medicalHistory?.forEach { (question, answer) ->
                item {
                        Text(
                            text = "$question",
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ans: $answer",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f), fontFamily = FontFamily.Serif, fontSize = 20.sp
                        )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HabitsView(userData: Map<String, Any>?, onBack: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Habits",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val habits = userData?.get("habits") as? List<String>
            habits?.let {
                items(it) { habit ->
                    UserDataBox(label = "Habit", value = formatHabitName(habit))
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}
@Composable
fun UserDataBox(label: String, value: String) {
    Box(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .border(
                width = 1.9.dp,
                color = colorResource(id = R.color.gray_300),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .padding(vertical = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text(
                text = "$label:",
                fontWeight = FontWeight.W500,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = value,
                fontWeight = FontWeight.W400,
                fontSize = 20.sp
            )
        }
    }
}