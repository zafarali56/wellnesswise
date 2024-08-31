import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                UserImage()
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = user?.displayName ?: "User Name",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )

            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {


                item {
                    Spacer(Modifier.height(5.dp))
                    Text("Email: ${user?.email ?: "N/A"}", fontFamily = FontFamily.Serif)
                    Spacer(modifier = Modifier.height(5.dp))
                }

                if (isLoading) {
                    item { Text("Loading user data...") }
                } else {
                    userData?.let { data ->
                        item {
                            Text("Full Name: ${data["fullName"] as? String ?: "N/A"}", fontFamily = FontFamily.Serif)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item {
                            Text("Age: ${(data["age"] as? Number)?.toString() ?: "N/A"}", fontFamily = FontFamily.Serif)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item {
                            Text("Gender: ${data["gender"] as? String ?: "N/A"}", fontFamily = FontFamily.Serif)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item {
                            Text("Height: ${(data["height"] as? Number)?.toString() ?: "N/A"}", fontFamily = FontFamily.Serif)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onHabitsClick,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary))
            ) {
                Text("View Habits")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onMedicalHistoryClick,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary))
            ) {
                Text("View Medical History")
            }
        }
    }
}

@Composable
fun MedicalHistoryView(userData: Map<String, Any>?, onBack: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Medical History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                val medicalHistory = userData?.get("medicalHistory") as? Map<String, String>
                medicalHistory?.forEach { (question, answer) ->
                    item {
                        Text(
                            text = question,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Ans: $answer",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HabitsView(userData: Map<String, Any>?, onBack: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Habits",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                val habits = userData?.get("habits") as? List<String>
                habits?.let {
                    items(it) { habit ->
                        Text(
                            text = formatHabitName(habit),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}