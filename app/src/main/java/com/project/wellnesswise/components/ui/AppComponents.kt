package com.project.wellnesswise.components.ui

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
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
import kotlinx.coroutines.launch
import java.util.Locale

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
        color = colorScheme.onSurface,
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
        color = colorScheme.onSurface,
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
                .fillMaxWidth(),
        shape = shapes.small, // This line adds rounded corners
        label = { Text(text = labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            focusedLabelColor = colorScheme.primary,
            cursorColor = colorScheme.primary,
        ),
        keyboardActions = KeyboardActions.Default,
        value = textValue.value,
        onValueChange = {
            textValue.value = it
            onTextSelected(it)
        },
        isError = isError,
        singleLine = true,
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
                .fillMaxWidth(),

        shape = shapes.small,
        label = { Text(text = labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            focusedLabelColor = colorScheme.primary,
            cursorColor = colorScheme.primary,
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
        singleLine = true,
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
                .fillMaxWidth(),
        shape = shapes.small,
        label = { Text(text = labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            focusedLabelColor = colorScheme.primary,
            cursorColor = colorScheme.primary,
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
        singleLine = true,
    )
}

@Composable
fun ClickableTextComponent(
    onTextSelected: (String) -> Unit = {},
) {
    val initialText = "By continuing you agree to our "
    val privacyPolicyText = "Privacy Policy"
    val andText = " and "
    val termsAndConditions = "Terms of Service"
    val annotatedString =
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = colorScheme.primary)) {
                pushStringAnnotation(tag = termsAndConditions, annotation = termsAndConditions)
                append(initialText)
            }

            withStyle(style = SpanStyle(color = colorScheme.primary)) {
                pushStringAnnotation(tag = privacyPolicyText, annotation = privacyPolicyText)
                append(privacyPolicyText)
            }
            withStyle(style = SpanStyle(color = colorScheme.primary)) {
                pushStringAnnotation(tag = termsAndConditions, annotation = termsAndConditions)
                append(andText)
            }
            withStyle(style = SpanStyle( colorScheme.primary)) {
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
            colors = CheckboxDefaults.colors(
                checkedColor = colorScheme.primary,
                uncheckedColor = colorScheme.onSurfaceVariant,
                checkmarkColor = colorScheme.onPrimary
            ),
        )
        ClickableTextComponent(onTextSelected)
    }
}

@Composable
fun ButtonComponent(
    value: String,
    onButtonClicked: () -> Unit = {},
    isEnabled: Boolean = true,
) {
    val backgroundColor = if (isEnabled) {
        colorScheme.primary
    } else {
        colorScheme.surfaceVariant
    }

    val contentColor = if (isEnabled) {
        colorScheme.onPrimary
    } else {
        colorScheme.onSurfaceVariant
    }

    Button(
        onClick = { onButtonClicked.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        enabled = isEnabled,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(48.dp)
                .background(
                    color = backgroundColor,
                    shape = shapes.medium
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
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
        Text(
            text = value,
            fontSize = 18.sp,
            color = colorScheme.primary
        )
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
    val initialText = if (tryingToLogin) "Already have an account? " else "Don't have an account? "
    val loginText = if (tryingToLogin) "Login" else "Register"

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = colorScheme.onBackground)) {
            append(initialText)
        }
        withStyle(style = SpanStyle(color = colorScheme.primary)) {
            pushStringAnnotation(tag = loginText, annotation = loginText)
            append(loginText)
        }
    }

    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center
        ),
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.also { span ->
                Log.d("ClickableTextComponent", "Clicked on: ${span.item}")
                if (span.item == loginText) {
                    onTextSelected(span.item)
                }
            }
        }
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
        color = colorScheme.onSurface.copy(alpha = 0.6f),
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
            colors = RadioButtonDefaults.colors(
                selectedColor = colorScheme.primary,
                unselectedColor = colorScheme.onSurfaceVariant,

                ),
        )
        Text(text = "Male", style = MaterialTheme.typography.bodyMedium)

        RadioButton(
            selected = selectedGender == Gender.FEMALE,
            onClick = {
                selectedGender = Gender.FEMALE
                onGenderSelected(selectedGender)
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = colorScheme.primary,
                unselectedColor = colorScheme.onSurfaceVariant,

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
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorScheme.primary,
                        uncheckedColor = colorScheme.onSurfaceVariant,
                        checkmarkColor = colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =
                    habit.name
                        .replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    }
}


fun formatHabitName(name: String): String {
    return name.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
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
                    color = colorScheme.primary,
                    shape = RoundedCornerShape(11.dp),
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
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f)) // This will push the icon to the end
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colorScheme.onPrimary,
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
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorScheme.primary,
                                unselectedColor = colorScheme.onSurfaceVariant,

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
        title = { Text("Wellness Wise") },
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
    userData: Map<String, Any>?

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
                    UserImg()
                    // User name
                    Text(
                        text = userData?.get("fullName") as? String ?: "User Name",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    // User email
                    Text(
                        text = user?.email ?: "user@example.com",
                        fontSize = 16.sp,
                        color = colorScheme.onSurface,
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

    colorScheme.primary // Get the primary color from the theme

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        shape = RoundedCornerShape(26.dp),
        color = if (isPressed)
            colorScheme.primaryContainer
        else
            colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.12f)),
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
                tint = colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface,
            )
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
            shape = shapes.small,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                focusedLabelColor = colorScheme.primary,
                cursorColor = colorScheme.primary,
            ),
            isError = isError,
            enabled = enabled,
            readOnly = !enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        if (isError && enabled) {
            Text(
                text = errorMessage,
                color = colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
        if (!enabled) {
            Text(
                text = "This field is synced with Google Fit",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

@Composable
fun UserImg(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.AccountCircle,
        contentDescription = "User Profile",
        modifier = modifier.size(100.dp),
        tint = colorScheme.primary
    )
}


@Composable
fun LoadingAnimation() {
    val dots = 3
    val delayUnit = 300

    @Composable
    fun Dot(
        scale: Float
    ) = Spacer(
        Modifier
            .size(24.dp)
            .scale(scale)
            .background(
                color = colorScheme.primary,
                shape = CircleShape
            )
            .fillMaxWidth()
    )

    val infiniteTransition = rememberInfiniteTransition(label = "")

    @Composable
    fun animateScaleWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = delayUnit * 4
                0f at delay using LinearEasing
                1f at delay + delayUnit using LinearEasing
                0f at delay + delayUnit * 2
            }
        ),
        label = ""
    )

    val scales = (0 until dots).map { animateScaleWithDelay(it * delayUnit) }

    Row(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        scales.forEach { scale ->
            Dot(scale.value)
            Spacer(Modifier.width(12.dp))
        }
    }
}



@Composable
fun CustomBloodPressureInput(
    systolic: String,
    diastolic: String,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        BPTextField(
            value = systolic,
            onValueChange = onSystolicChange,
            label = "Systolic",
            modifier = Modifier.weight(1f)
        )
        BPTextField(
            value = diastolic,
            onValueChange = onDiastolicChange,
            label = "Diastolic",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BPTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var isError by remember { mutableStateOf(false) }

    Column(modifier = modifier) {

        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                    onValueChange(it)
                    isError = it.isNotEmpty() && it.toIntOrNull() !in 40..250
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline,
                focusedLabelColor = colorScheme.primary,
                cursorColor = colorScheme.primary,
                errorBorderColor = colorScheme.error
            ),
            shape = RoundedCornerShape(10.dp)
        )
        if (isError) {
            Text(
                text = "Invalid input",
                color = colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}