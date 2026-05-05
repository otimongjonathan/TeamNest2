package com.example.teamnest

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeOnboarding(onGetStarted: () -> Unit) {
    val pages = listOf(
        OnboardingPage("COLLABORATE", "Centralize your communication, file sharing, and task management in one workspace.", Icons.Default.Groups),
        OnboardingPage("STAY ORGANIZED", "Assign tasks, set deadlines, and receive smart reminders to keep your projects on track.", Icons.AutoMirrored.Filled.Assignment),
        OnboardingPage("SHARE IDEAS", "Brainstorm with your team, upload assets, and build amazing products together.", Icons.Default.Lightbulb)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState, 
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) { page ->
                OnboardingContent(pages[page], pagerState.currentPageOffsetFraction)
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(pages.size) { i ->
                        val width by animateDpAsState(
                            targetValue = if (pagerState.currentPage == i) 24.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == i) primaryColor else primaryColor.copy(alpha = 0.2f))
                        )
                    }
                }
                
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
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "GET STARTED" else "CONTINUE", 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingContent(p: OnboardingPage, offset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
            .graphicsLayer {
                alpha = 1f - kotlin.math.abs(offset)
                translationX = offset * size.width * 0.5f
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(240.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ) {
            Icon(
                imageVector = p.icon, 
                contentDescription = null, 
                modifier = Modifier.padding(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = p.title, 
            fontSize = 32.sp, 
            fontWeight = FontWeight.Black, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = p.description, 
            fontSize = 17.sp, 
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), 
            textAlign = TextAlign.Center, 
            lineHeight = 26.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel(), onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = primaryColor.copy(alpha = 0.1f)
            ) {}
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "TeamNest Logo",
                modifier = Modifier.size(160.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "TEAMNEST", 
            fontSize = 36.sp, 
            fontWeight = FontWeight.Black, 
            color = primaryColor,
            letterSpacing = 4.sp
        )
        Text(
            text = "Collaborate Effortlessly", 
            fontSize = 14.sp, 
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("EMAIL ADDRESS") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = primaryColor) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("PASSWORD") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isLoading) {
            CircularProgressIndicator(color = primaryColor)
        } else {
            Button(
                onClick = {
                    authViewModel.login(email, password) { success, error ->
                        if (success) onLoginSuccess()
                        else Toast.makeText(context, error ?: "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }, 
                Modifier
                    .fillMaxWidth()
                    .height(60.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) { 
                Text("SIGN IN", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) 
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onRegisterClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New to TeamNest? ", color = Color.Gray)
                Text("CREATE ACCOUNT", color = primaryColor, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun RegisterScreen(authViewModel: AuthViewModel = viewModel(), onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CREATE ACCOUNT", 
            fontSize = 32.sp, 
            fontWeight = FontWeight.Black, 
            color = primaryColor,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Join thousands of teams today", 
            fontSize = 14.sp, 
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("FULL NAME") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("EMAIL ADDRESS") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = primaryColor) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("PASSWORD") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("CONFIRM PASSWORD") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = primaryColor) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isLoading) {
            CircularProgressIndicator(color = primaryColor)
        } else {
            Button(
                onClick = {
                    authViewModel.register(username, email, password, confirmPassword) { success, error ->
                        if (success) onRegisterSuccess()
                        else Toast.makeText(context, error ?: "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                }, 
                Modifier
                    .fillMaxWidth()
                    .height(60.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) { 
                Text("GET STARTED", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) 
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onBackToLogin) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = Color.Gray)
                Text("SIGN IN", color = primaryColor, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
