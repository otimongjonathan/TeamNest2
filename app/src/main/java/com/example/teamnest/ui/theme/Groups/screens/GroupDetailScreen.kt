package com.example.teamnest.ui.theme.Groups.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamnest.ui.theme.data.Group
import com.example.teamnest.ui.theme.Groups.Viewmodel.GroupDetailViewModel
import com.example.teamnest.ui.theme.authentication.viewModel.AuthViewModel
import com.example.teamnest.ui.theme.components.AddTaskDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavController,
    detailViewModel: GroupDetailViewModel = viewModel()
) {
    val group by detailViewModel.group
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(groupId) {
        detailViewModel.listenToGroup(groupId)
    }

    group?.let { g ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = g.name.uppercase(), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { p ->
            Column(modifier = Modifier.padding(p)) {
                TabRow(selectedTabIndex = selectedTab, contentColor = MaterialTheme.colorScheme.primary) {
                    Tab(
                        selected = selectedTab == 0, 
                        onClick = { selectedTab = 0 }, 
                        text = { Text("TASKS", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1, 
                        onClick = { selectedTab = 1 }, 
                        text = { Text("FILES", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2, 
                        onClick = { selectedTab = 2 }, 
                        text = { Text("IDEAS", fontWeight = FontWeight.Bold) }
                    )
                }
                Crossfade(targetState = selectedTab, label = "detailTabTransition") { tab ->
                    when (tab) {
                        0 -> GroupTasksTab(g, detailViewModel)
                        1 -> GroupFilesTab(g, detailViewModel)
                        2 -> GroupIdeasTab(g, detailViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupTasksTab(
    group: Group,
    detailViewModel: GroupDetailViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val tasks = detailViewModel.tasks
    val currentUserEmail = authViewModel.currentUser.value?.email?.lowercase() ?: ""
    val isLeader = group.leaderId == authViewModel.currentUser.value?.uid
    var showAddTask by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLeader) {
            // Join Code Card for Leaders in Detail Screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "GROUP JOIN CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                    Text(text = group.joinCode, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF9800), letterSpacing = 2.sp)
                    Text(text = "TeamNest teammates can join using this code", fontSize = 10.sp, textAlign = TextAlign.Center, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showAddTask = true }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("NEW TASK", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn {
            items(tasks) { task ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { detailViewModel.updateTaskCompletion(task.id, it) },
                        enabled = (task.assigneeEmail == currentUserEmail || isLeader)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = task.title, fontWeight = FontWeight.Bold)
                        Text(text = "Assignee: ${task.assigneeEmail}", fontSize = 12.sp)
                    }
                    if (isLeader) {
                        IconButton(onClick = { detailViewModel.deleteTask(task.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
    if (showAddTask) {
        AddTaskDialog(onDismiss = { showAddTask = false }, onConfirm = { t, d, e, dl ->
            detailViewModel.createTask(context, group, t, d, e, dl) {
                showAddTask = false
            }
        })
    }
}

@Composable
fun GroupFilesTab(group: Group, detailViewModel: GroupDetailViewModel) {
    val files = detailViewModel.files
    val context = LocalContext.current
    val isUploading by detailViewModel.isUploading

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { detailViewModel.uploadFile(group.id, it, context) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { launcher.launch("*/*") }, 
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("UPLOAD FILE (PDF, PPT, IMG)", fontWeight = FontWeight.Bold)
        }
        if (isUploading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(files) { file ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.url))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                file.type.contains("pdf") -> Icons.Default.PictureAsPdf
                                file.type.contains("image") -> Icons.Default.Image
                                else -> Icons.AutoMirrored.Filled.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(file.name, fontWeight = FontWeight.Bold)
                            Text("From: ${file.sender}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupIdeasTab(
    group: Group,
    detailViewModel: GroupDetailViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val ideas = detailViewModel.ideas
    var ideaText by remember { mutableStateOf("") }
    val currentUserId = authViewModel.currentUser.value?.uid ?: ""
    val userProfile by authViewModel.userProfile
    val context = LocalContext.current
    val isPostingIdea by detailViewModel.isPostingIdea

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = ideaText,
                onValueChange = { ideaText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Share an idea...") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Button(
                onClick = {
                    if (ideaText.isNotBlank()) {
                        detailViewModel.postIdea(
                            context = context,
                            groupId = group.id,
                            content = ideaText,
                            authorName = userProfile?.name ?: "Unknown User",
                            onSuccess = { ideaText = "" }
                        )
                    }
                },
                enabled = !isPostingIdea,
                modifier = Modifier.padding(start = 8.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isPostingIdea) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("POST", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(ideas) { i ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = i.author.uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "shared an idea", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(text = i.content, modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { detailViewModel.toggleLike(i) }) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = null,
                                    tint = if (i.likes.contains(currentUserId)) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                            Text(text = "${i.likes.size}")
                        }
                    }
                }
            }
        }
    }
}
