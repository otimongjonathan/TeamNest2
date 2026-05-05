package com.example.teamnest

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamnest.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavController,
    detailViewModel: GroupDetailViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val group by detailViewModel.group
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }
    val currentUserUid = authViewModel.currentUser.value?.uid
    val context = LocalContext.current

    LaunchedEffect(groupId) {
        detailViewModel.listenToGroup(groupId)
    }

    group?.let { g ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(text = g.name.uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 1.sp)
                            Text(text = g.description, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        if (g.leaderId == currentUserUid) {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Group")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { p ->
            Column(modifier = Modifier.padding(p)) {
                TabRow(
                    selectedTabIndex = selectedTab, 
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    val tabs = listOf("TASKS", "FILES", "IDEAS")
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index, 
                            onClick = { selectedTab = index }, 
                            text = { Text(label, fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 13.sp) }
                        )
                    }
                }
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn(tween(400))).togetherWith(
                                slideOutHorizontally { -it / 3 } + fadeOut(tween(400))
                            )
                        } else {
                            (slideInHorizontally { -it } + fadeIn(tween(400))).togetherWith(
                                slideOutHorizontally { it / 3 } + fadeOut(tween(400))
                            )
                        }.using(SizeTransform(clip = false))
                    },
                    label = "detailTabTransition"
                ) { tab ->
                    when (tab) {
                        0 -> GroupTasksTab(g, detailViewModel, navController)
                        1 -> GroupFilesTab(g, detailViewModel)
                        2 -> GroupIdeasTab(g, detailViewModel)
                    }
                }
            }
        }

        if (showEditDialog) {
            EditGroupDialog(
                group = g,
                onDismiss = { showEditDialog = false },
                onConfirm = { name, desc ->
                    detailViewModel.updateGroupDetails(context, g.id, name, desc) {
                        showEditDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun EditGroupDialog(
    group: Group,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var description by remember { mutableStateOf(group.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("EDIT GROUP DETAILS", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun GroupTasksTab(
    group: Group,
    detailViewModel: GroupDetailViewModel,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val tasks = detailViewModel.tasks
    val currentUserEmail = authViewModel.currentUser.value?.email?.lowercase() ?: ""
    val currentUserUid = authViewModel.currentUser.value?.uid
    val isAdmin = group.leaderId == currentUserUid || group.coLeaderEmails.any { it.equals(currentUserEmail, ignoreCase = true) }
    
    var showAddTask by remember { mutableStateOf(false) }
    val isDark = LocalIsDarkTheme.current

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        if (isAdmin) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(text = "JOIN CODE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                        Text(text = group.joinCode, fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 4.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { showAddTask = true }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ASSIGN NEW TASK", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tasks assigned yet.", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tasks) { task ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("taskDetail/${task.id}") },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp), 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { detailViewModel.updateTaskCompletion(task.id, it) },
                                enabled = isAdmin,
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = task.title, 
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (task.timestamp > 0) {
                                        Text(
                                            text = formatTimestamp(task.timestamp),
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Text(
                                    text = if (task.assigneeEmail.equals(currentUserEmail, ignoreCase = true)) "Assigned to You" else "For: ${task.assigneeEmail}", 
                                    fontSize = 12.sp, 
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (isAdmin) {
                                IconButton(onClick = { detailViewModel.deleteTask(task.id) }) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Red.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
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

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Button(
            onClick = { launcher.launch("*/*") }, 
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("UPLOAD WORKSPACE FILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        
        if (isUploading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).clip(CircleShape), 
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (files.isEmpty() && !isUploading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No files shared yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(files) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.url))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        file.type.contains("pdf") -> Icons.Default.PictureAsPdf
                                        file.type.contains("image") -> Icons.Default.Image
                                        else -> Icons.AutoMirrored.Filled.InsertDriveFile
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Shared by ${file.sender}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
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

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = ideaText,
                onValueChange = { ideaText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("What's on your mind?") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
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
                enabled = !isPostingIdea && ideaText.isNotBlank(),
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (ideaText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                if (isPostingIdea) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post", tint = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("TEAM BRAINSTORMING", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Gray, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (ideas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ideas shared yet. Be the first!", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(ideas) { i ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(name = i.author, size = 32.dp, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = i.author.uppercase(), 
                                            fontWeight = FontWeight.Black, 
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (i.timestamp > 0) {
                                            Text(
                                                text = formatTimestamp(i.timestamp),
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Text(text = "Shared an idea", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = i.content, 
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                onClick = { detailViewModel.toggleLike(i) },
                                color = if (i.likes.contains(currentUserId)) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp),
                                border = if (i.likes.contains(currentUserId)) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (i.likes.contains(currentUserId)) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                        contentDescription = null,
                                        tint = if (i.likes.contains(currentUserId)) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${i.likes.size}", 
                                        color = if (i.likes.contains(currentUserId)) MaterialTheme.colorScheme.primary else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
