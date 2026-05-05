package com.example.teamnest

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    navController: NavController,
    userName: String,
    onLogout: () -> Unit,
    onSeeAllGroups: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    groupsViewModel: GroupsViewModel = viewModel(),
    tasksViewModel: TasksViewModel = viewModel()
) {
    val context = LocalContext.current
    val tasks = homeViewModel.tasks
    val groups = homeViewModel.groups
    val invitations = homeViewModel.invitations
    val currentUserId = authViewModel.currentUser.value?.uid ?: ""
    val currentUserEmail = authViewModel.currentUser.value?.email ?: ""
    var showMembers by remember { mutableStateOf(false) }
    var selectedGroupForMembers by remember { mutableStateOf<Group?>(null) }
    var showInviteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeViewModel.startListening(context)
    }

    val urgent = tasks.filter { 
        try { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(it.deadline)) in 0..3 } 
        catch (e: Exception) { false } 
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Dashboard Widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.WavingHand, 
                                    contentDescription = null, 
                                    tint = Color.White.copy(alpha = 0.9f), 
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Welcome back,", 
                                    color = Color.White.copy(alpha = 0.8f), 
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userName.uppercase(), 
                                color = Color.White, 
                                fontSize = 28.sp, 
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${urgent.size} URGENT TASKS", 
                                    color = Color.White, 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) { 
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout, 
                                contentDescription = "Logout", 
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            ) 
                        }
                    }
                }
            }
        }

        // Pending Invitations Widget
        if (invitations.isNotEmpty()) {
            item {
                HomeWidget(
                    title = "PENDING INVITATIONS",
                    icon = Icons.Default.Mail,
                    iconColor = Color(0xFF2196F3)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        invitations.forEach { invite ->
                            InvitationCard(invite, homeViewModel)
                        }
                    }
                }
            }
        }

        // Urgent Tasks Widget
        item {
            HomeWidget(
                title = "UPCOMING DEADLINES",
                icon = Icons.Default.Timer,
                iconColor = Color(0xFFF44336)
            ) {
                if (urgent.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(text = "You're all caught up!", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        urgent.forEach { task ->
                            UrgentTaskCard(task, onDone = null) {
                                navController.navigate("taskDetail/${task.id}")
                            }
                        }
                    }
                }
            }
        }

        // Recent Groups Widget
        item {
            HomeWidget(
                title = "YOUR TEAMS",
                icon = Icons.Default.Groups,
                iconColor = Color(0xFFFF9800),
                headerAction = {
                    TextButton(onClick = onSeeAllGroups) { 
                        Text(
                            text = "VIEW ALL", 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        ) 
                    }
                }
            ) {
                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No teams joined yet.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        groups.take(3).forEach { g ->
                            GroupCard(
                                g = g,
                                currentUserId = currentUserId,
                                onViewMembers = {
                                    groupsViewModel.fetchGroupMembers(g.memberEmails, g.leaderId, g.coLeaderEmails)
                                    selectedGroupForMembers = g
                                    showMembers = true
                                },
                                onClick = { navController.navigate("groupDetail/${g.id}") }
                            )
                        }
                    }
                }
            }
        }
    }

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
}

@Composable
fun HomeWidget(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    headerAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                letterSpacing = 1.sp
            )
            headerAction?.invoke()
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
fun InvitationCard(invite: Invitation, homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Invitation to Join", 
                        fontSize = 12.sp, 
                        color = Color.Gray, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = invite.groupName, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 18.sp, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "from ${invite.inviterEmail}", 
                fontSize = 13.sp, 
                color = Color.Gray,
                modifier = Modifier.padding(start = 56.dp)
            )

            if (isProcessing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clip(CircleShape), 
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Row(
                    modifier = Modifier.padding(top = 20.dp), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isProcessing = true
                            homeViewModel.acceptInvitation(invite) { success, error ->
                                isProcessing = false
                                if (success) Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) { 
                        Text("ACCEPT", fontSize = 13.sp, fontWeight = FontWeight.Black) 
                    }

                    OutlinedButton(
                        onClick = {
                            isProcessing = true
                            homeViewModel.declineInvitation(invite.id) { _, _ ->
                                isProcessing = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        modifier = Modifier.weight(1f).height(44.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) { 
                        Text("DECLINE", fontSize = 13.sp, fontWeight = FontWeight.Black) 
                    }
                }
            }
        }
    }
}
