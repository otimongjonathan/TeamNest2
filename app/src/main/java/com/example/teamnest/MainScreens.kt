package com.example.teamnest

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun MainScreenWithBottomNav(
    navController: NavController,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("HOME", "TASKS", "REMINDERS", "GROUPS", "PROFILE")
    val userProfile by authViewModel.userProfile
    val userName = userProfile?.name ?: "User"
    val userEmail = userProfile?.email ?: ""
    val isDarkTheme by themeViewModel.isDarkTheme
    
    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(80.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { 
                                Text(
                                    text = label, 
                                    fontWeight = if (selectedTab == index) FontWeight.ExtraBold else FontWeight.Medium, 
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.Home
                                        1 -> Icons.AutoMirrored.Filled.Assignment
                                        2 -> Icons.Default.Notifications
                                        3 -> Icons.Default.Groups
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray.copy(alpha = 0.7f),
                                unselectedTextColor = Color.Gray.copy(alpha = 0.7f),
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
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
                label = "mainTabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeScreen(
                        navController = navController,
                        userName = userName,
                        onLogout = onLogout,
                        onSeeAllGroups = { selectedTab = 3 }
                    )
                    1 -> TasksScreen(navController, themeViewModel)
                    2 -> RemindersScreen(navController)
                    3 -> GroupsScreen(navController)
                    4 -> ProfileScreen(userName, userEmail, onLogout, themeViewModel, authViewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    name: String, 
    email: String, 
    onLogout: () -> Unit,
    themeViewModel: ThemeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "MY PROFILE", 
            fontSize = 24.sp, 
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp), 
            thickness = 1.dp, 
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Box(contentAlignment = Alignment.BottomEnd) {
            UserAvatar(name = name, size = 120.dp, fontSize = 48.sp)
            Surface(
                modifier = Modifier.size(36.dp).clickable { showEditDialog = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Icon(
                    Icons.Default.Edit, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = name.uppercase(), fontSize = 26.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Text(text = email.lowercase(), fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(40.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("PREFERENCES", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Theme Toggle Button
                Surface(
                    onClick = { themeViewModel.toggleTheme() },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDarkTheme) Color(0xFF2F3133) else Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = if (isDarkTheme) "Light Mode" else "Dark Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { themeViewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("ACCOUNT", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(Modifier.width(12.dp))
                    Text("LOG OUT", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFD32F2F))
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = name,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                authViewModel.updateProfileName(newName) { success, error ->
                    if (success) {
                        showEditDialog = false
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error ?: "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("EDIT PROFILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("NAME") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TasksScreen(navController: NavController, themeViewModel: ThemeViewModel = viewModel()) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
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
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("MY TASKS", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("MANAGE", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
            label = "tasksTabTransition"
        ) { tab ->
            if (tab == 0) MyTasksContent(navController) else ManageTasksTab(navController = navController, themeViewModel = themeViewModel)
        }
    }
}

@Composable
fun MyTasksContent(navController: NavController, tasksViewModel: TasksViewModel = viewModel()) {
    val tasks = tasksViewModel.myTasks
    LaunchedEffect(Unit) {
        tasksViewModel.startListeningMyTasks()
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item { Spacer(Modifier.height(20.dp)); Text("PENDING", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Gray, letterSpacing = 1.sp); Spacer(Modifier.height(16.dp)) }
        items(tasks.filter { !it.isCompleted }) { t ->
            TaskCard(t) { navController.navigate("taskDetail/${t.id}") }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (tasks.any { it.isCompleted }) {
            item { 
                Spacer(modifier = Modifier.height(24.dp))
                Text("COMPLETED", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp)) 
            }
            items(tasks.filter { it.isCompleted }) { t ->
                TaskCard(t, true) { navController.navigate("taskDetail/${t.id}") }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ManageTasksTab(
    navController: NavController,
    tasksViewModel: TasksViewModel = viewModel(), 
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val context = LocalContext.current
    val leadGroups = tasksViewModel.leadGroups
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showCreateTask by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    val currentUserEmail = authViewModel.currentUser.value?.email ?: ""
    val currentUserId = authViewModel.currentUser.value?.uid ?: ""
    val isDarkTheme by themeViewModel.isDarkTheme

    val tasks = tasksViewModel.selectedGroupTasks

    LaunchedEffect(Unit) {
        tasksViewModel.startListeningLeadGroups()
    }

    LaunchedEffect(leadGroups.size) {
        if (selectedGroup == null && leadGroups.isNotEmpty()) {
            selectedGroup = leadGroups.first()
        }
    }

    LaunchedEffect(selectedGroup?.id) {
        selectedGroup?.let { tasksViewModel.listenToGroupTasks(it.id) }
    }

    if (leadGroups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "YOU DON'T LEAD ANY GROUPS.", color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true }, 
                        modifier = Modifier.fillMaxWidth().height(56.dp), 
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Text(text = selectedGroup?.name?.uppercase() ?: "SELECT GROUP", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                        leadGroups.forEach { group ->
                            DropdownMenuItem(text = { Text(text = group.name.uppercase(), fontWeight = FontWeight.Bold) }, onClick = { selectedGroup = group; expanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            selectedGroup?.let { group ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "TEAM JOIN CODE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(text = group.joinCode, fontSize = 40.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = 6.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(text = "Share this code with your team members", fontSize = 12.sp, textAlign = TextAlign.Center, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showCreateTask = true }, 
                            modifier = Modifier.weight(1f).height(50.dp), 
                            shape = RoundedCornerShape(16.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { 
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("TASK", fontWeight = FontWeight.Black) 
                        }
                        Button(
                            onClick = { showInviteDialog = true }, 
                            modifier = Modifier.weight(1f).height(50.dp), 
                            shape = RoundedCornerShape(16.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) { 
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("INVITE", fontWeight = FontWeight.Black) 
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "TEAM MEMBERS (${group.memberEmails.size}/15)", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(group.memberEmails) { email ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = email, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            if (email != currentUserEmail && group.leaderId == currentUserId) {
                                IconButton(onClick = { tasksViewModel.removeMember(group.id, email) }, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "TASKS", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (tasks.isEmpty()) {
                    item { Text("No tasks assigned yet.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(vertical = 8.dp)) }
                } else {
                    items(tasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { navController.navigate("taskDetail/${task.id}") }, 
                            shape = RoundedCornerShape(16.dp), 
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = task.isCompleted, 
                                    onCheckedChange = { tasksViewModel.updateTaskCompletion(task.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                    Text(text = task.title, fontWeight = FontWeight.Bold, color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface)
                                    Text(text = task.assigneeEmail, fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "DUE: ${task.deadline}", fontSize = 11.sp, color = if (!task.isCompleted) Color(0xFFD32F2F) else Color.Gray, fontWeight = FontWeight.Black)
                                }
                                Row {
                                    IconButton(onClick = { taskToEdit = task }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { tasksViewModel.deleteTask(task.id) }) {
                                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateTask) {
        AddTaskDialog(onDismiss = { showCreateTask = false }, onConfirm = { t, d, e, dl ->
            tasksViewModel.createTask(context, selectedGroup!!, t, d, e, dl) {
                showCreateTask = false
            }
        })
    }

    if (showInviteDialog) {
        InviteMemberDialog(onDismiss = { showInviteDialog = false }, onConfirm = { email ->
            tasksViewModel.sendInvite(context, selectedGroup!!, currentUserEmail, email)
            showInviteDialog = false
        })
    }

    taskToEdit?.let { task ->
        EditTaskDialog(task = task, onDismiss = { taskToEdit = null }, onConfirm = { t, d, e, dl ->
            tasksViewModel.updateTask(task.id, t, d, e, dl, selectedGroup!!) {
                taskToEdit = null
            }
        })
    }
}

@Composable
fun RemindersScreen(
    navController: NavController,
    remindersViewModel: RemindersViewModel = viewModel()
) {
    val tasks = remindersViewModel.reminderTasks

    LaunchedEffect(Unit) {
        remindersViewModel.startListening()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "REMINDERS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 1.sp)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text(text = "NO URGENT REMINDERS", color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("taskDetail/${task.id}") },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(Color.Red.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = task.title, fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = task.groupName.uppercase(), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                    Spacer(Modifier.width(4.dp))
                                    Text(text = "DUE: ${task.deadline}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupsScreen(
    navController: NavController, 
    groupsViewModel: GroupsViewModel = viewModel(), 
    authViewModel: AuthViewModel = viewModel(),
    tasksViewModel: TasksViewModel = viewModel()
) {
    val groups = groupsViewModel.groups
    var showJoin by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var showMembers by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<Group?>(null) }
    var selectedGroupForMembers by remember { mutableStateOf<Group?>(null) }
    
    val context = LocalContext.current
    val currentUserId = authViewModel.currentUser.value?.uid ?: ""
    val currentUserEmail = authViewModel.currentUser.value?.email ?: ""

    LaunchedEffect(Unit) {
        groupsViewModel.startListening()
    }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "GROUPS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 1.sp)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { showCreate = true }, 
                modifier = Modifier.weight(1f).height(50.dp), 
                shape = RoundedCornerShape(16.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { 
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("CREATE", fontWeight = FontWeight.Black) 
            }
            Button(
                onClick = { showJoin = true }, 
                modifier = Modifier.weight(1f).height(50.dp), 
                shape = RoundedCornerShape(16.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) { 
                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("JOIN", fontWeight = FontWeight.Black) 
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        if (groups.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "You haven't joined any groups.", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                items(groups) { g ->
                    GroupCard(
                        g = g,
                        currentUserId = currentUserId,
                        onViewMembers = {
                            groupsViewModel.fetchGroupMembers(g.memberEmails, g.leaderId, g.coLeaderEmails)
                            selectedGroupForMembers = g
                            showMembers = true
                        },
                        onDeleteGroup = {
                            groupToDelete = g
                            showDeleteDialog = true
                        },
                        onClick = { navController.navigate("groupDetail/${g.id}") }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showCreate) {
        CreateGroupDialog(onDismiss = { showCreate = false }, onConfirm = { n, d ->
            groupsViewModel.createGroup(n, d)
            showCreate = false
            Toast.makeText(context, "Team Created!", Toast.LENGTH_SHORT).show()
        })
    }

    if (showJoin) JoinGroupDialog(onDismiss = { showJoin = false }, onConfirm = { code -> 
        groupsViewModel.joinGroupByCode(code) { success, msg ->
            if (success) {
                showJoin = false
            }
            Toast.makeText(context, msg ?: (if (success) "Joined!" else "Error"), Toast.LENGTH_SHORT).show()
        }
    })
    
    if (showMembers && selectedGroupForMembers != null) {
        MembersDialog(
            members = groupsViewModel.groupMembers,
            isCurrentUserLeader = selectedGroupForMembers!!.leaderId == currentUserId,
            currentUserId = currentUserId,
            isGroupFull = selectedGroupForMembers!!.memberEmails.size >= 15,
            onRemoveMember = { member ->
                groupsViewModel.removeMember(selectedGroupForMembers!!.id, member.email) { success, msg ->
                    if (success) {
                        val updatedEmails = selectedGroupForMembers!!.memberEmails.filter { it != member.email }
                        groupsViewModel.fetchGroupMembers(updatedEmails, selectedGroupForMembers!!.leaderId, selectedGroupForMembers!!.coLeaderEmails)
                    } else {
                        Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onPromoteToLeader = { member ->
                groupsViewModel.promoteToLeader(selectedGroupForMembers!!.id, member.email) { success, msg ->
                    if (success) {
                        val updatedCoLeaders = (selectedGroupForMembers!!.coLeaderEmails + member.email.lowercase()).distinct()
                        groupsViewModel.fetchGroupMembers(selectedGroupForMembers!!.memberEmails, selectedGroupForMembers!!.leaderId, updatedCoLeaders)
                    } else {
                        Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onInviteMember = {
                showInviteDialog = true
            },
            onDismiss = { showMembers = false }
        )
    }

    if (showInviteDialog && selectedGroupForMembers != null) {
        InviteMemberDialog(
            onDismiss = { showInviteDialog = false },
            onConfirm = { email ->
                tasksViewModel.sendInvite(context, selectedGroupForMembers!!, currentUserEmail, email)
                showInviteDialog = false
            }
        )
    }

    if (showDeleteDialog && groupToDelete != null) {
        DeleteGroupDialog(
            groupName = groupToDelete!!.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                groupsViewModel.deleteGroup(groupToDelete!!.id) { success, msg ->
                    showDeleteDialog = false
                    if (!success) {
                        Toast.makeText(context, msg ?: "Error deleting group", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
