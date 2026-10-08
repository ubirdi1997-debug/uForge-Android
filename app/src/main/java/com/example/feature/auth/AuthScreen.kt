package com.example.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.repository.AuthRepository
import com.example.ui.theme.Emerald400
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    isOpen: Boolean,
    authRepository: AuthRepository,
    onAuthSuccess: (isSignup: Boolean) -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val scope = rememberCoroutineScope()
    var isSignup by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Mara Dev") }
    var email by remember { mutableStateOf("mara@usafe.in") }
    var password by remember { mutableStateOf("demo-pass-123") }
    var isLoading by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Centered Forge Logo "F"
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Emerald400),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Slate950
                    )
                }

                Text(
                    text = "Forge Studio",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Slate100
                )

                Text(
                    text = "Sign in with your uSafe account",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Segmented control: Log in | Sign up
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (!isSignup) Slate950 else Slate800)
                            .border(if (!isSignup) 1.dp else 0.dp, if (!isSignup) Emerald400 else Slate800, RoundedCornerShape(9.dp))
                            .clickable { isSignup = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log in",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (!isSignup) Emerald400 else Slate400
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSignup) Slate950 else Slate800)
                            .border(if (isSignup) 1.dp else 0.dp, if (isSignup) Emerald400 else Slate800, RoundedCornerShape(9.dp))
                            .clickable { isSignup = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign up",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isSignup) Emerald400 else Slate400
                        )
                    }
                }

                // Name field (if signup)
                if (isSignup) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Name", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate950)
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = name,
                                onValueChange = { name = it },
                                textStyle = TextStyle(fontFamily = InterFontFamily, fontSize = 14.sp, color = Slate100),
                                cursorBrush = SolidColor(Emerald400),
                                modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                            )
                        }
                    }
                }

                // Email field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Email", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate950)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = email,
                            onValueChange = { email = it },
                            textStyle = TextStyle(fontFamily = InterFontFamily, fontSize = 14.sp, color = Slate100),
                            cursorBrush = SolidColor(Emerald400),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )
                    }
                }

                // Password field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Password", fontFamily = InterFontFamily, fontSize = 12.sp, color = Slate400, modifier = Modifier.padding(bottom = 4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate950)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = password,
                            onValueChange = { password = it },
                            textStyle = TextStyle(fontFamily = InterFontFamily, fontSize = 14.sp, color = Slate100),
                            cursorBrush = SolidColor(Emerald400),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Emerald400)
                        .clickable(enabled = !isLoading) {
                            isLoading = true
                            scope.launch {
                                val result = if (isSignup) {
                                    authRepository.signup(name, email, password)
                                } else {
                                    authRepository.login(email, password)
                                }
                                isLoading = false
                                if (result.isSuccess) {
                                    onToast("Signed in as ${result.user?.name}")
                                    onAuthSuccess(isSignup)
                                } else {
                                    onToast(result.errorMessage ?: "Authentication failed")
                                }
                            }
                        }
                        .testTag("auth_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) "Connecting to auth.usafe.in…" else if (isSignup) "Create account" else "Log in",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Slate950
                    )
                }

                Text(
                    text = "Uses auth.usafe.in for sign-in and api.usafe.in for your profile.",
                    fontFamily = InterFontFamily,
                    fontSize = 11.5.sp,
                    color = Slate400,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
