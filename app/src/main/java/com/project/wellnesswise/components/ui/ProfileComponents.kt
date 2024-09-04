import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.Font
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
            Column (modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally){
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
                            color = colorScheme.primary
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
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),

            colors = CardDefaults.cardColors(
                containerColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer


        ) ){

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)

            ) {


                item {
                    Spacer(Modifier.height(5.dp))
                    Text("Email: ${user?.email ?: "N/A"}", fontSize = 18.sp, )
                    Spacer(modifier = Modifier.height(5.dp))
                }

                if (isLoading) {
                    item { LoadingAnimation() }
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
            horizontalArrangement = Arrangement.spacedBy(9.dp)

        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onHabitsClick,
                colors = ButtonDefaults.buttonColors( colorScheme.primary),
            ) {
                Text("Habits",  fontWeight = FontWeight.SemiBold,)
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onMedicalHistoryClick,
                colors = ButtonDefaults.buttonColors( colorScheme.primary),
            ) {
                Text("Medical History",  fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(16.dp))
       ButtonComponent(value = "Edit Profile", onButtonClicked = onEditClick, isEnabled = true)

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryView(userData: Map<String, Any>?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Medical history", color = colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface
                )
            )
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = colorScheme.background
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
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
                                    fontSize = 20.sp,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Ans: $answer",
                                    fontSize = 20.sp,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsView(userData: Map<String, Any>?, onBack: () -> Unit) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Habits", color = colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = colorScheme.onSurface,
                        navigationIconContentColor = colorScheme.onSurface
                    )
                )
            },
            containerColor = colorScheme.background
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = colorScheme.background
            ) {
Column (modifier = Modifier.padding(horizontal = 26.dp)) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer
        )
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(22.dp)
        ) {
            val habits = userData?.get("habits") as? List<String>
            habits?.let {
                items(it) { habit ->
                    Text(
                        text = formatHabitName(habit),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(5.dp),
                    )
                }
            }
        }
    }
}
            }
        }
    }

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


