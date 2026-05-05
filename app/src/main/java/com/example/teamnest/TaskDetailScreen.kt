package com.example.teamnest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TaskDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    var task = mutableStateOf<Task?>(null)
    var group = mutableStateOf<Group?>(null)
    var isLoading = mutableStateOf(true)

    fun fetchTaskDetails(taskId: String) {
        db.collection("tasks").document(taskId).get().addOnSuccessListener { taskDoc ->
            val taskObj = taskDoc.toObject(Task::class.java)
            task.value = taskObj
            if (taskObj != null) {
                db.collection("groups").document(taskObj.groupId).get().addOnSuccessListener { groupDoc ->
                    group.value = groupDoc.toObject(Group::class.java)
                    isLoading.value = false
                }
            } else {
                isLoading.value = false
            }
        }.addOnFailureListener {
            isLoading.value = false
        }
    }

    fun updateTaskCompletion(isCompleted: Boolean) {
        task.value?.id?.let { id ->
            db.collection("tasks").document(id).update("isCompleted", isCompleted)
                .addOnSuccessListener {
                    task.value = task.value?.copy(isCompleted = isCompleted)
                }
        }
    }
    
    fun isAdmin(): Boolean {
        val currentUserEmail = auth.currentUser?.email?.lowercase() ?: ""
        val currentUserUid = auth.currentUser?.uid ?: ""
        val g = group.value ?: return false
        return g.leaderId == currentUserUid || g.coLeaderEmails.any { it.equals(currentUserEmail, ignoreCase = true) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: TaskDetailViewModel = viewModel()
) {
    LaunchedEffect(taskId) {
        viewModel.fetchTaskDetails(taskId)
    }

    val task = viewModel.task.value
    val isLoading = viewModel.isLoading.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TASK DETAILS", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (task == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Task not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(text = "TITLE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                                Text(text = task.title, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        Text(text = "DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                        Text(
                            text = if (task.description.isBlank()) "No description provided." else task.description,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "ASSIGNEE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                                Text(text = task.assigneeEmail, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "DEADLINE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                                Text(text = task.deadline, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (task.isCompleted) Color.Gray else Color.Red)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "STATUS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                            Text(
                                text = if (task.isCompleted) "COMPLETED" else "IN PROGRESS",
                                fontWeight = FontWeight.Black,
                                color = if (task.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (viewModel.isAdmin()) {
                            Switch(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.updateTaskCompletion(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32))
                            )
                        }
                    }
                }
                
                if (!viewModel.isAdmin()) {
                    Text(
                        text = "Only group admins can mark tasks as complete.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 12.dp, start = 8.dp)
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                Button(
                    onClick = { navController.navigate("groupDetail/${task.groupId}") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("GO TO TEAM WORKSPACE", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
