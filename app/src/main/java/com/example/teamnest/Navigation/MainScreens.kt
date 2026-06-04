package com.example.teamnest.Navigation

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamnest.ui.theme.data.Group
import com.example.teamnest.ui.theme.reminders.viewmodel.RemindersViewModel
import com.example.teamnest.ui.theme.data.Task
import com.example.teamnest.ui.theme.Groups.Viewmodel.GroupsViewModel
import com.example.teamnest.ui.theme.authentication.viewModel.AuthViewModel
import com.example.teamnest.ui.theme.components.AddTaskDialog
import com.example.teamnest.ui.theme.components.CreateGroupDialog
import com.example.teamnest.ui.theme.components.DeleteGroupDialog
import com.example.teamnest.ui.theme.components.EditTaskDialog
import com.example.teamnest.ui.theme.components.GroupCard
import com.example.teamnest.ui.theme.components.InviteMemberDialog
import com.example.teamnest.ui.theme.components.JoinGroupDialog
import com.example.teamnest.ui.theme.components.MembersDialog
import com.example.teamnest.ui.theme.components.TaskCard
import com.example.teamnest.ui.theme.components.UserAvatar
import com.example.teamnest.ui.theme.home.screens.HomeScreen
import com.example.teamnest.ui.theme.tasks.viewmodel.TasksViewModel

@Composable
fun MainScreenWithBottomNav(
    navController: NavController,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("HOME", "TASKS", "REMINDERS", "GROUPS", "PROFILE")
    val userProfile by authViewModel.userProfile
    val userName = userProfile?.name ?: "User"
    val userEmail = userProfile?.email ?: ""
    
    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.3f)
                )
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    tabs.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { 
                                Text(
                                    text = label, 
                                    fontWeight = FontWeight.Black, 
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
                                selectedIconColor = Color(0xFFFF9800),
                                selectedTextColor = Color(0xFFFF9800),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFFFF3E0)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    navController = navController,
                    userName = userName,
                    onLogout = onLogout,
                    onSeeAllGroups = { selectedTab = 3 }
                )
                1 -> TasksScreen(navController)
                2 -> RemindersScreen(navController)
                3 -> GroupsScreen(navController)
                4 -> ProfileScreen(userName, userEmail, onLogout)
            }
        }
    }
}

@Composable
fun ProfileScreen(name: String, email: String, onLogout: () -> Unit) {
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
            textAlign = TextAlign.Start
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
        
        Spacer(modifier = Modifier.height(40.dp))
        UserAvatar(name = name, size = 120.dp, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = name.uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
        Text(text = email.lowercase(), fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("ACCOUNT SETTINGS", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("LOGOUT", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TasksScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFFFF9800),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFFFF9800)
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("MY TASKS", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Black)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("MANAGE", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Black)
            }
        }
        Crossfade(targetState = selectedTab, label = "tasksTabTransition") { tab ->
            if (tab == 0) MyTasksContent(navController) else ManageTasksTab()
        }
    }
}

@Composable
fun MyTasksContent(navController: NavController, tasksViewModel: TasksViewModel = viewModel()) {
    val tasks = tasksViewModel.myTasks
    LaunchedEffect(Unit) {
        tasksViewModel.startListeningMyTasks()
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("PENDING", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Gray); Spacer(Modifier.height(12.dp)) }
        items(tasks.filter { !it.isCompleted }) { t ->
            TaskCard(t) { navController.navigate("groupDetail/${t.groupId}") }
            Spacer(modifier = Modifier.height(8.dp))
        }
        item { Spacer(modifier = Modifier.height(24.dp)); Text("COMPLETED", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Gray); Spacer(Modifier.height(12.dp)) }
        items(tasks.filter { it.isCompleted }) { t ->
            TaskCard(t, true) { navController.navigate("groupDetail/${t.groupId}") }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ManageTasksTab(tasksViewModel: TasksViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    val leadGroups = tasksViewModel.leadGroups
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var showCreateTask by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    val currentUserEmail = authViewModel.currentUser.value?.email ?: ""
    val currentUserId = authViewModel.currentUser.value?.uid ?: ""

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
            Text(text = "YOU DON'T LEAD ANY GROUPS.", color = Color.Gray, fontWeight = FontWeight.Black)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(text = selectedGroup?.name ?: "SELECT GROUP", fontWeight = FontWeight.Black)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth()) {
                        leadGroups.forEach { group ->
                            DropdownMenuItem(text = { Text(text = group.name, fontWeight = FontWeight.Bold) }, onClick = { selectedGroup = group; expanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            selectedGroup?.let { group ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "GROUP JOIN CODE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                            Text(text = group.joinCode, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF9800), letterSpacing = 4.sp)
                            Text(text = "Share this code with teammates to let them join!", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showCreateTask = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) { Text("CREATE TASK", fontWeight = FontWeight.Black) }
                        Button(onClick = { showInviteDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)) { Text("INVITE MEMBER", fontWeight = FontWeight.Black) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "MEMBERS (${group.memberEmails.size}/15)", fontWeight = FontWeight.Black)
                }

                items(group.memberEmails) { email ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = email, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                        // Only Owner (leaderId) can remove members from this list
                        if (email != currentUserEmail && group.leaderId == currentUserId) {
                            IconButton(onClick = { tasksViewModel.removeMember(group.id, email) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Text(text = "TASKS", fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 8.dp))
                }

                if (tasks.isEmpty()) {
                    item { Text("No tasks yet.", color = Color.Gray, fontStyle = FontStyle.Italic) }
                } else {
                    items(tasks) { task ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = task.isCompleted, onCheckedChange = { tasksViewModel.updateTaskCompletion(task.id, it) })
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = task.title, fontWeight = FontWeight.Bold, color = if (task.isCompleted) Color.Gray else Color.Black)
                                    Text(text = "Assignee: ${task.assigneeEmail}", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "Due: ${task.deadline}", fontSize = 11.sp, color = if (!task.isCompleted) Color.Red else Color.Gray, fontWeight = FontWeight.Black)
                                }
                                Row {
                                    IconButton(onClick = { taskToEdit = task }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Task", tint = Color.Gray)
                                    }
                                    IconButton(onClick = { tasksViewModel.deleteTask(task.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Gray)
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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        remindersViewModel.startListening(context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "REMINDERS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF9800))
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
        
        if (tasks.isEmpty()) {
            Text(text = "NO URGENT REMINDERS.", color = Color.Gray, fontWeight = FontWeight.Bold)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = task.title, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(text = task.groupName, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(text = "DUE: ${task.deadline}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupsScreen(navController: NavController, groupsViewModel: GroupsViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()) {
    val groups = groupsViewModel.groups
    var showJoin by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var showMembers by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<Group?>(null) }
    var selectedGroupForMembers by remember { mutableStateOf<Group?>(null) }
    
    val context = LocalContext.current
    val currentUserId = authViewModel.currentUser.value?.uid ?: ""

    LaunchedEffect(Unit) {
        groupsViewModel.startListening()
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "GROUPS", fontSize = 24.sp, fontWeight = FontWeight.Black)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showCreate = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) { Text("CREATE TEAM", fontWeight = FontWeight.Black) }
            Button(onClick = { showJoin = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)) { Text("JOIN BY CODE", fontWeight = FontWeight.Black) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(groups) { g ->
                GroupCard(
                    g = g,
                    currentUserId = currentUserId,
                    onViewMembers = {
                        groupsViewModel.fetchGroupMembers(
                            g.memberEmails,
                            g.leaderId,
                            g.coLeaderEmails
                        )
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
            Toast.makeText(
                context,
                msg ?: (if (success) "Joined!" else "Error"),
                Toast.LENGTH_SHORT
            ).show()
        }
    })
    
    if (showMembers && selectedGroupForMembers != null) {
        MembersDialog(
            members = groupsViewModel.groupMembers,
            isCurrentUserLeader = selectedGroupForMembers!!.leaderId == currentUserId,
            currentUserId = currentUserId,
            onRemoveMember = { member ->
                groupsViewModel.removeMember(
                    selectedGroupForMembers!!.id,
                    member.email
                ) { success, msg ->
                    if (success) {
                        val updatedEmails =
                            selectedGroupForMembers!!.memberEmails.filter { it != member.email }
                        groupsViewModel.fetchGroupMembers(
                            updatedEmails,
                            selectedGroupForMembers!!.leaderId,
                            selectedGroupForMembers!!.coLeaderEmails
                        )
                    } else {
                        Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onPromoteToLeader = { member ->
                groupsViewModel.promoteToLeader(
                    selectedGroupForMembers!!.id,
                    member.email
                ) { success, msg ->
                    if (success) {
                        val updatedCoLeaders =
                            (selectedGroupForMembers!!.coLeaderEmails + member.email.lowercase()).distinct()
                        groupsViewModel.fetchGroupMembers(
                            selectedGroupForMembers!!.memberEmails,
                            selectedGroupForMembers!!.leaderId,
                            updatedCoLeaders
                        )
                    } else {
                        Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showMembers = false }
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
                        Toast.makeText(context, msg ?: "Error deleting group", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        )
    }
}
