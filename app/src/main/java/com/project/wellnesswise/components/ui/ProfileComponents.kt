import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import com.project.wellnesswise.components.ui.ButtonComponent
import com.project.wellnesswise.components.ui.LoadingAnimation
import com.project.wellnesswise.components.ui.UserImg
import com.project.wellnesswise.components.ui.formatHabitName

@Composable
fun MainProfileView(
    user: FirebaseUser?,
    userData: Map<String, Any>?,
    isLoading: Boolean,
    onMedicalHistoryClick: () -> Unit,
    onHabitsClick: () -> Unit,
    onEditClick: () -> Unit
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
                UserImg()
                Spacer(modifier = Modifier.height(10.dp))
                if (isLoading) {
                    LoadingAnimation()
                } else {
                    userData?.let { data ->
                        Text(
                            data["fullName"] as? String ?: "N/A",
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                        )
                    }


                }
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
                    .padding(20.dp)

            ) {


                item {
                    Spacer(Modifier.height(5.dp))
                    Text("Email: ${user?.email ?: "N/A"}", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                }

                if (isLoading) {
                    item { Text("Loading user data...") }
                } else {
                    userData?.let { data ->

                        item {
                            Text("Age: ${(data["age"] as? Number)?.toString() ?: "N/A"}", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item {
                            Text("Gender: ${data["gender"] as? String ?: "N/A"}", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item {
                            Text("Height: ${(data["height"] as? Number)?.toString() ?: "N/A"}" , fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item {
                            Text ( "Weight: ${(data["weight"] as? Number)?.toString() ?: "N/A"}",  fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item {
                            Text("Blood Pressure: ${data["bloodPressure"] as? String ?: "N/A"}", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item { Text("Heart Rate: ${(data["heartRate"]  )?: "N/A"}",fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item { Text("Blood Sugar: ${(data["bloodSugar"] )?: "N/A"}",fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                        item { Text("Cholesterol: ${(data["cholesterol"] ) ?: "N/A"}",fontSize = 18.sp)
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
                colors = ButtonDefaults.buttonColors( MaterialTheme.colorScheme.primary),
            ) {
                Text("View Habits", fontWeight = FontWeight.Bold)
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onMedicalHistoryClick,
                colors = ButtonDefaults.buttonColors( MaterialTheme.colorScheme.primary),
            ) {
                Text("View Medical History", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
       ButtonComponent(value = "Edit Profile", onButtonClicked = onEditClick, isEnabled = true)

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

                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ans: $answer",
                            fontWeight = FontWeight.Bold,

                            fontSize = 20.sp
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
                            fontSize = 20.sp,
                            modifier = Modifier.padding( 10.dp),
                            fontWeight = FontWeight.SemiBold

                        )
                    }
                }
            }
        }
    }
}



fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


