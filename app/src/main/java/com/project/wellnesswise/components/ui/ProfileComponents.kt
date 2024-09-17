import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun MainProfileView(
    user: FirebaseUser?,
    userData: Map<String, Any>?,
    isLoading: Boolean,
    onEditClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
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
                            color = colorScheme.onSurface,
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

       ButtonComponent(value = "Edit Profile", onButtonClicked = onEditClick, isEnabled = true)


        Spacer(Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDeleteAccountClick,
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
        ) {
            Text("Delete Account")
        }
    }
}



fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


