package com.project.wellnesswise.components.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.project.wellnesswise.R
import com.project.wellnesswise.data.Gender
import com.project.wellnesswise.data.Habit
import com.project.wellnesswise.data.MedicalHistoryQuestion
import com.project.wellnesswise.data.RegistrationViewModel
import com.project.wellnesswise.data.UIEvent
import com.project.wellnesswise.ui.theme.componentShapes
import kotlinx.coroutines.launch

@Composable
fun NormalTextComponent(value: String) {
    Text(
        text = value,
        modifier = Modifier.fillMaxWidth(),
        style =
            TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
            ),
        color = colorResource(id = R.color.black),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun HeadingTextComponent(value: String) {
    Text(
        text = value,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
        // Added min height
        style =
            TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
            ),
        color = colorResource(id = R.color.black),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun MyTextField(
    labelValue: String,
    initialValue: String,
    onTextSelected: (String) -> Unit,
    isError: Boolean = false,
) {
    val textValue = remember { mutableStateOf(initialValue) }

    OutlinedTextField(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(componentShapes.small),
        label = { Text(text = labelValue) },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.gray_100),
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
                cursorColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
            ),
        keyboardActions = KeyboardActions.Default,
        value = textValue.value,
        onValueChange = {
            textValue.value = it
            onTextSelected(it)
        },
        isError = isError,
    )
}

@Composable
fun MyNumberField(
    labelValue: String,
    initialValue: String,
    keyboardType: KeyboardType = KeyboardType.Number,
    onTextSelected: (Int?) -> Unit,
    isError: Boolean = false,
) {
    val textValue = remember { mutableStateOf(initialValue) }

    OutlinedTextField(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(componentShapes.small),
        label = { Text(text = labelValue) },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.gray_100),
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
                cursorColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
            ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        keyboardActions = KeyboardActions.Default,
        value = textValue.value,
        onValueChange = {
            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                textValue.value = it
                onTextSelected(it.toIntOrNull())
            }
        },
        isError = isError,
    )
}

@Composable
fun MyPasswordField(
    labelValue: String,
    initialValue: String,
    onTextSelected: (String) -> Unit,
    isError: Boolean = false,
) {
    val password = remember { mutableStateOf(initialValue) }
    val passwordVisible = remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(componentShapes.small),
        label = { Text(text = labelValue) },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.gray_100),
                focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
                cursorColor = if (isError) MaterialTheme.colorScheme.error else colorResource(id = R.color.primary),
            ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        value = password.value,
        onValueChange = {
            password.value = it
            onTextSelected(it)
        },
        trailingIcon = {
            val iconImage =
                if (passwordVisible.value) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }
            val description =
                if (passwordVisible.value) {
                    stringResource(id = R.string.HidePassword)
                } else {
                    stringResource(id = R.string.ShowPassword)
                }
            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                Icon(imageVector = iconImage, contentDescription = description)
            }
        },
        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        isError = isError,
    )
}

@Composable
fun ClickableTextComponent(
    value: String,
    onTextSelected: (String) -> Unit = {},
) {
    val initialText = "By continuing you agree to our "
    val privacyPolicyText = "Privacy Policy"
    val andText = " and "
    val termsAndConditions = "Terms of Service"
    val annotatedString =
        buildAnnotatedString {
            append(initialText)
            withStyle(style = SpanStyle(color = colorResource(id = R.color.primary))) {
                pushStringAnnotation(tag = privacyPolicyText, annotation = privacyPolicyText)
                append(privacyPolicyText)
            }
            append(andText)
            withStyle(style = SpanStyle(color = colorResource(id = R.color.primary))) {
                pushStringAnnotation(tag = termsAndConditions, annotation = termsAndConditions)
                append(termsAndConditions)
            }
        }

    ClickableText(text = annotatedString, onClick = { offset ->
        annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.also { span ->
            Log.d("ClickableTextComponent", "Clicked on: ${span.item}")
            if ((span.item == termsAndConditions) || (span.item == privacyPolicyText)) {
                onTextSelected(span.item)
            }
        }
    })
}

@Composable
fun CheckBoxComponent(
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTextSelected: (String) -> Unit = {},
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                CheckboxDefaults.colors(
                    checkedColor = colorResource(id = R.color.primary), // Set the tick color here
                    uncheckedColor = Color.Gray, // Optional: Set the unchecked color
                    checkmarkColor = Color.White, // Set the checkmark color here
                ),
        )
        ClickableTextComponent(value = value, onTextSelected)
    }
}

@Composable
fun ButtonComponent(
    value: String,
    onButtonClicked: () -> Unit = {},
    isEnabled: Boolean = false,
) {
    Button(
        onClick = { onButtonClicked.invoke() },
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        enabled = isEnabled,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(48.dp)
                    .background(
                        color = colorResource(id = R.color.primary),
                        shape = RoundedCornerShape(50.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DividerTextComponent(value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            color = colorResource(id = R.color.gray_100),
            thickness = 1.dp,
        )
        Text(modifier = Modifier.padding(8.dp), text = value, fontSize = 18.sp, color = colorResource(id = R.color.primary))
        HorizontalDivider(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            color = colorResource(id = R.color.gray_100),
            thickness = 1.dp,
        )
    }
}

@Composable
fun ClickableLoginTextComponent(
    tryingToLogin: Boolean = true,
    onTextSelected: (String) -> Unit = {},
) {
    val initialText = if (tryingToLogin) "Already have an account? " else " Don't have an account? "

    val loginText = if (tryingToLogin) "Login" else "Register"

    val annotatedString =
        buildAnnotatedString {
            append(initialText)
            withStyle(style = SpanStyle(color = colorResource(id = R.color.primary))) {
                pushStringAnnotation(tag = initialText, annotation = initialText)
            }

            withStyle(style = SpanStyle(color = colorResource(id = R.color.primary))) {
                pushStringAnnotation(tag = loginText, annotation = loginText)
                append(loginText)
            }
        }

    ClickableText(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
        style =
            TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
            ),
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.also { span ->
                Log.d("ClickableTextComponent", "Clicked on: ${span.item}")
                if (span.item == loginText) {
                    onTextSelected(span.item)
                }
            }
        },
    )
}

@Composable
fun UnderLinedTextComponent(value: String) {
    Text(
        text = value,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
        style =
            TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
            ),
        color = colorResource(id = R.color.gray_100),
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline,
    )
}

@Composable
fun GenderSelection(
    initialGender: Gender,
    onGenderSelected: (Gender) -> Unit,
) {
    var selectedGender by remember { mutableStateOf(initialGender) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(id = R.string.Gender), style = MaterialTheme.typography.bodyLarge)

        RadioButton(
            selected = selectedGender == Gender.MALE,
            onClick = {
                selectedGender = Gender.MALE
                onGenderSelected(selectedGender)
            },
            colors =
                RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.primary),
                    unselectedColor = Color.Gray,
                ),
        )
        Text(text = "Male", style = MaterialTheme.typography.bodyMedium)

        RadioButton(
            selected = selectedGender == Gender.FEMALE,
            onClick = {
                selectedGender = Gender.FEMALE
                onGenderSelected(selectedGender)
            },
            colors =
                RadioButtonDefaults.colors(
                    selectedColor = colorResource(id = R.color.primary),
                    unselectedColor = Color.Gray,
                ),
        )
        Text(text = "Female", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun HabitSelection(
    registrationViewModel: RegistrationViewModel,
    onHabitsSelected: (List<Habit>) -> Unit,
) {
    val habits = Habit.entries
    var selectedHabits by remember { mutableStateOf(registrationViewModel.registrationUIState.value.habits) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        habits.forEach { habit ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = selectedHabits.contains(habit),
                    onCheckedChange = { isChecked ->
                        selectedHabits =
                            if (isChecked) {
                                selectedHabits + habit
                            } else {
                                selectedHabits - habit
                            }
                        onHabitsSelected(selectedHabits)
                        registrationViewModel.onEvent(UIEvent.HabitsChanged(selectedHabits))
                    },
                    colors =
                        CheckboxDefaults.colors(
                            checkedColor = colorResource(id = R.color.primary),
                            uncheckedColor = Color.Gray,
                        ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =
                        habit.name
                            .replace("_", " ")
                            .lowercase()
                            .capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    }
}

@Composable
fun HabbitAndMedHistoryButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.primary),
                    shape = RoundedCornerShape(15.dp),
                ).clickable { onClick() }
                .padding(17.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f)) // This will push the icon to the end
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

@Composable
fun MedicalHistorySection(
    registrationViewModel: RegistrationViewModel,
    questions: List<MedicalHistoryQuestion>,
) {
    val medicalHistory =
        registrationViewModel.registrationUIState.value.medicalHistory
            .toMutableMap()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        questions.forEach { question ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
            ) {
                Text(text = question.question, style = MaterialTheme.typography.bodyMedium)
                question.suggestedAnswers.forEach { answer ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = medicalHistory[question.question] == answer,
                            onClick = {
                                medicalHistory[question.question] = answer
                                registrationViewModel.onEvent(UIEvent.MedicalHistoryChanged(question.question, answer))
                            },
                            colors =
                                RadioButtonDefaults.colors(
                                    selectedColor = colorResource(id = R.color.primary),
                                    unselectedColor = Color.Gray,
                                ),
                        )
                        Text(text = answer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(onNavigationIconClick: () -> Unit) {
    TopAppBar(
        title = { Text("WellnessWise") },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
    )
}

@Composable
fun NavigationDrawer(
    content: @Composable () -> Unit,
    onProfileClick: () -> Unit,
    onHomeClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Giant logo icon
                    UserImage()
                    // User name
                    Text(
                        text = user?.displayName ?: "User Name",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    // User email
                    Text(
                        text = user?.email ?: "user@example.com",
                        fontSize = 16.sp,
                        color = Color.Gray,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation items
                    listOf(
                        Triple(stringResource(id = R.string.Home), Icons.Default.Home, onHomeClick),
                        Triple(stringResource(id = R.string.Profile), Icons.Default.Person, onProfileClick),
                        Triple(
                            stringResource(id = R.string.Logout),
                            Icons.AutoMirrored.Filled.ExitToApp,
                            onLogoutClick,
                        ),
                    ).forEach { (text, icon, onClick) ->
                        NavigationItem(
                            text = text,
                            icon = icon,
                            onClick = {
                                onClick()
                                scope.launch { drawerState.close() }
                            },
                        )
                    }
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    AppToolbar(onNavigationIconClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    })
                },
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    content()
                }
            }
        },
    )
}

@Composable
fun NavigationItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val primaryColor = MaterialTheme.colorScheme.primary // Get the primary color from the theme

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        shape = RoundedCornerShape(26.dp),
        color = if (isPressed) primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorResource(id = R.color.primary),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isLargeCard: Boolean = true,
) {
    Card(
        modifier = modifier.height(if (isLargeCard) 120.dp else 100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(if (isLargeCard) 40.dp else 20.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style =
                        if (isLargeCard) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleSmall
                        },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style =
                            if (isLargeCard) {
                                MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp)
                            } else {
                                MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
                            },
                        fontWeight = FontWeight.Bold,
                        color = color,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style =
                            if (isLargeCard) {
                                MaterialTheme.typography.bodyMedium
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                        color = color.copy(alpha = 0.7f),
                        modifier = Modifier.alignByBaseline(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorMessage: String,
    enabled: Boolean,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            colors =
                TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.primary),
                    focusedLabelColor = colorResource(id = R.color.primary),
                    cursorColor = colorResource(id = R.color.primary),
                    disabledBorderColor = colorResource(id = R.color.gray_300),
                    disabledLabelColor = colorResource(id = R.color.gray_500),
                    disabledTextColor = colorResource(id = R.color.gray_700),
                ),
            isError = isError,
            enabled = enabled,
            readOnly = !enabled,
        )
        if (isError && enabled) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
        if (!enabled) {
            Text(
                text = "This field is synced with Google Fit",
                color = colorResource(id = R.color.gray_500),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

@Composable
fun UserImage() {
    Image(
        painter = painterResource(id = R.drawable.iconfordrawer),
        contentDescription = null,
        modifier =
            Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
    )
}
