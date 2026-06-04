package com.example.teamnest.ui.theme.home.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.teamnest.ui.theme.data.Group
import com.example.teamnest.ui.theme.Groups.Viewmodel.GroupsViewModel
import com.example.teamnest.ui.theme.home.screens.viewmodel.HomeViewModel
import com.example.teamnest.ui.theme.data.Invitation
import com.example.teamnest.ui.theme.authentication.viewModel.AuthViewModel
import com.example.teamnest.ui.theme.components.GroupCard
import com.example.teamnest.ui.theme.components.MembersDialog
import com.example.teamnest.ui.theme.components.UrgentTaskCard
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
    groupsViewModel: GroupsViewModel = viewModel()
) {
    val context = LocalContext.current
    val tasks = homeViewModel.tasks
    val groups = homeViewModel.groups
    val invitations = homeViewModel.invitations
    val currentUserId = authViewModel.currentUser.value?.uid ?: ""
    var showMembers by remember { mutableStateOf(false) }
    var selectedGroupForMembers by remember { mutableStateOf<Group?>(null) }

    LaunchedEffect(Unit) {
        homeViewModel.startListening(context)
    }

    val urgent = tasks.filter { 
        try { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(it.deadline)) in 0..3 } 
        catch (e: Exception) { false } 
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Dashboard Widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WavingHand, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Hello,", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                        }
                        Text(text = userName.uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "You have ${urgent.size} urgent tasks today", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) { 
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White) 
                    }
                }
            }
        }

        // Pending Invitations Widget
        if (invitations.isNotEmpty()) {
            item {
                HomeWidget(
                    title = "INVITATIONS",
                    icon = Icons.Default.Email,
                    iconColor = Color(0xFF2196F3)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                title = "URGENT DEADLINES",
                icon = Icons.Default.AssignmentLate,
                iconColor = Color.Red
            ) {
                if (urgent.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(text = "All caught up! No urgent tasks.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        urgent.forEach { task ->
                            // Removed onDone callback to restrict task completion to admins only
                            UrgentTaskCard(task, onDone = null) {
                                navController.navigate("groupDetail/${task.groupId}")
                            }
                        }
                    }
                }
            }
        }

        // Recent Groups Widget
        item {
            HomeWidget(
                title = "MY TEAMS",
                icon = Icons.Default.Groups,
                iconColor = Color(0xFFFF9800),
                headerAction = {
                    TextButton(onClick = onSeeAllGroups) { 
                        Text(
                            text = "SEE ALL", 
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ) 
                    }
                }
            ) {
                if (groups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Not in any groups yet.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        groups.forEach { g ->
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
}

@Composable
fun HomeWidget(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    headerAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )
                headerAction?.invoke()
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun InvitationCard(invite: Invitation, homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)), // Light blue background
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp).animateContentSize()) {
            Text(text = "Join ${invite.groupName}?", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
            Text(text = "Invited by: ${invite.inviterEmail}", fontSize = 13.sp, color = Color.Gray)

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), color = Color(0xFF1976D2))
            } else {
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            isProcessing = true
                            homeViewModel.acceptInvitation(invite) { success, error ->
                                isProcessing = false
                                if (success) Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text("ACCEPT", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                    OutlinedButton(
                        onClick = {
                            isProcessing = true
                            homeViewModel.declineInvitation(invite.id) { _, _ ->
                                isProcessing = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text("DECLINE", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
