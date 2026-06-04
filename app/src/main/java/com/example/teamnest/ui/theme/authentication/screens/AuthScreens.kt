package com.example.teamnest.ui.theme.authentication.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamnest.ui.theme.data.OnboardingPage
import com.example.teamnest.R
import com.example.teamnest.ui.theme.authentication.viewModel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeOnboarding(onGetStarted: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            "COLLABORATE",
            stringResource(R.string.collaborate), Icons.Default.Groups
        ),
        OnboardingPage(
            "STAY ORGANIZED",
            stringResource(R.string.stay_organized), Icons.AutoMirrored.Filled.Assignment
        ),
        OnboardingPage(
            "SHARE IDEAS",
            stringResource(R.string.share_ideas), Icons.Default.Lightbulb
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            OnboardingContent(pages[page])
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    Box(modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == i) primaryColor else Color.LightGray))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onGetStarted()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(28.dp)
            ) { Text(text = if (pagerState.currentPage == pages.size - 1) "GET STARTED" else "NEXT", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun OnboardingContent(p: OnboardingPage) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = p.icon, contentDescription = null, modifier = Modifier.size(160.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(48.dp))
        Text(text = p.title, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = p.description, fontSize = 18.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 24.sp)
    }
}

@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel(), onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(Modifier
        .fillMaxSize()
        .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "TeamNest Logo",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape) // Made the logo circular
                .background(Color.Black) // Background to make the logo pop if needed
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "TEAMNEST", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(16.dp) // Added rounded corners
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp) // Added rounded corners
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isLoading) {
            CircularProgressIndicator(color = primaryColor)
        } else {
            Button(onClick = {
                authViewModel.login(email, password) { success, error ->
                    if (success) onLoginSuccess()
                    else Toast.makeText(context, error ?: "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }, Modifier
                .fillMaxWidth()
                .height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) { Text("LOGIN") }
        }
        TextButton(onClick = onRegisterClick) { Text(stringResource(R.string.create_account), color = primaryColor) }
    }
}

@Composable
fun RegisterScreen(authViewModel: AuthViewModel = viewModel(), onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.create_account),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("USERNAME") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp) // Added rounded corners
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(16.dp) // Added rounded corners
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp) // Added rounded corners
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("CONFIRM PASSWORD") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp) // Added rounded corners
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isLoading) CircularProgressIndicator(color = primaryColor)
        else Button(
            onClick = {
                authViewModel.register(
                    username,
                    email,
                    password,
                    confirmPassword
                ) { success, error ->
                    if (success) onRegisterSuccess()
                    else Toast.makeText(context, error ?: "Registration Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }, Modifier
                .fillMaxWidth()
                .height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) { Text("REGISTER") }
        TextButton(onBackToLogin) { Text("BACK TO LOGIN", color = primaryColor) }
    }
}
