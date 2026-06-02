package com.example.teamnest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 40.dp,
    fontSize: TextUnit = 16.sp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = (name.take(1)).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        )
    }
}

@Composable
fun UrgentTaskCard(t: Task, onDone: (() -> Unit)? = null, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = t.title, fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 16.sp)
                Text(text = t.groupName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                    Spacer(Modifier.width(4.dp))
                    Text(text = "Due: ${t.deadline}", color = Color.Red, fontSize = 12.sp)
                }
            }
            if (onDone != null) {
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TaskCard(t: Task, isCompleted: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = t.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isCompleted) Color.Gray else Color.Black
            )
            Text(text = t.groupName, fontSize = 14.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = "Due: ${t.deadline}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun GroupCard(
    g: Group, 
    currentUserId: String, 
    onViewMembers: () -> Unit, 
    onDeleteGroup: (() -> Unit)? = null, // Callback for deletion
    onClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val isLeader = g.leaderId == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = g.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black)
                    Text(text = g.description, fontSize = 14.sp, color = Color.Gray, maxLines = 1)
                    
                    // Restrict Join Code visibility to Leaders only
                    if (isLeader && g.joinCode.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                clipboardManager.setText(AnnotatedString(g.joinCode))
                                Toast.makeText(context, "Join code copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFF9800))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "JOIN CODE: ${g.joinCode}", 
                                    fontSize = 13.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = Color(0xFFFF9800),
                                    letterSpacing = 1.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFF9800).copy(alpha = 0.7f))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(text = "${g.memberEmails.size}/15 Members", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF9800))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isLeader) "Leader" else "Member",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    // Delete Button - Only for Leaders
                    if (isLeader && onDeleteGroup != null) {
                        IconButton(
                            onClick = onDeleteGroup,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Group", tint = Color.Red.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).graphicsLayer(rotationZ = 180f).padding(top = 4.dp),
                        tint = Color.LightGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onViewMembers,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9800))
            ) {
                Text("VIEW TEAM MEMBERS", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun DeleteGroupDialog(groupName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("DELETE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        title = { Text("DELETE TEAM?", fontWeight = FontWeight.ExtraBold, color = Color.Red) },
        text = {
            Text("Are you sure you want to delete \"$groupName\"? This action cannot be undone and all team data will be lost.")
        }
    )
}

@Composable
fun MembersDialog(
    members: List<MemberDetail>,
    isCurrentUserLeader: Boolean, // True only for the original creator
    currentUserId: String,
    onRemoveMember: (MemberDetail) -> Unit,
    onPromoteToLeader: (MemberDetail) -> Unit,
    onInviteMember: (() -> Unit)? = null, // Callback for inviting member
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDismiss() }) { Text("CLOSE", fontWeight = FontWeight.Black) }
        },
        shape = RoundedCornerShape(24.dp),
        title = { Text("TEAM DIRECTORY", fontWeight = FontWeight.ExtraBold) },
        text = {
            if (members.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF9800))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(members) { member ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(name = member.name, size = 48.dp, fontSize = 20.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = member.name.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    Text(text = member.email, fontSize = 13.sp, color = Color.Gray)
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                        Icon(
                                            imageVector = if(member.role.contains("Leader")) Icons.Default.Security else Icons.Default.Group,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if(member.role.contains("Leader")) Color(0xFFFF9800) else Color.Gray
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(text = member.role, fontSize = 12.sp, color = if(member.role.contains("Leader")) Color(0xFFFF9800) else Color.Gray, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                // Original Leader (Owner) can perform actions on others
                                if (isCurrentUserLeader && member.id != currentUserId) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(Icons.Default.Person, contentDescription = "Member Actions", tint = Color.Gray)
                                        }
                                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                            if (member.role == "Member") {
                                                DropdownMenuItem(
                                                    text = { Text("Promote to Leader", fontWeight = FontWeight.Bold) },
                                                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF9800)) },
                                                    onClick = { 
                                                        onPromoteToLeader(member)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { Text("Remove from Team", color = Color.Red, fontWeight = FontWeight.Bold) },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                                onClick = {
                                                    onRemoveMember(member)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                    
                    if (isCurrentUserLeader && members.size < 15 && onInviteMember != null) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onInviteMember,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("INVITE MEMBER", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var n by remember { mutableStateOf("") }
    var d by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (n.isNotBlank()) onConfirm(n, d) }) {
                Text(text = "CREATE", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "NEW TEAM", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = n,
                    onValueChange = { n = it },
                    label = { Text("TEAM NAME") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it },
                    label = { Text("DESCRIPTION") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "CANCEL", color = Color.Gray)
            }
        }
    )
}

@Composable
fun JoinGroupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var c by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(c) }) {
                Text(text = "JOIN TEAM", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "JOIN BY CODE", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                Text(text = "Enter the 6-digit code shared by your team leader.", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = c,
                    onValueChange = { c = it },
                    label = { Text("INVITATION CODE") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "CANCEL", color = Color.Gray)
            }
        }
    )
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var t by remember { mutableStateOf("") }
    var d by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }
    var dl by remember { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (t.isNotBlank() && e.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(e.trim()).matches()) {
                    onConfirm(t, d, e, dl)
                } else if (t.isBlank()) {
                    Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please enter a valid assignee email", Toast.LENGTH_SHORT).show()
                }
            }) { Text(text = "CREATE", fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = Color.Gray) }
        },
        title = { Text(text = "ASSIGN NEW TASK", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("TASK TITLE") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it },
                    label = { Text("DESCRIPTION") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = e,
                    onValueChange = { e = it },
                    label = { Text("ASSIGNEE EMAIL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dl,
                    onValueChange = { dl = it },
                    label = { Text("DEADLINE (YYYY-MM-DD)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun EditTaskDialog(task: Task, onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var t by remember { mutableStateOf(task.title) }
    var d by remember { mutableStateOf(task.description) }
    var e by remember { mutableStateOf(task.assigneeEmail) }
    var dl by remember { mutableStateOf(task.deadline) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (t.isNotBlank() && e.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(e.trim()).matches()) {
                    onConfirm(t, d, e, dl)
                } else if (t.isBlank()) {
                    Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please enter a valid assignee email", Toast.LENGTH_SHORT).show()
                }
            }) { Text(text = "UPDATE", fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = Color.Gray) }
        },
        title = { Text(text = "EDIT TASK", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("TASK TITLE") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it },
                    label = { Text("DESCRIPTION") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = e,
                    onValueChange = { e = it },
                    label = { Text("ASSIGNEE EMAIL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dl,
                    onValueChange = { dl = it },
                    label = { Text("DEADLINE (YYYY-MM-DD)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun InviteMemberDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (email.isNotBlank()) {
                    onConfirm(email)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(email.trim()))
                        putExtra(Intent.EXTRA_SUBJECT, "Invitation to join TeamNest Group")
                        putExtra(Intent.EXTRA_TEXT, "Hello!\n\nYou have been invited to join a group in the TeamNest app. Please log in to the app to accept the invitation.")
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Send Invitation Email"))
                    } catch (ex: Exception) {
                        Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                    }
                }
            }) { Text("SEND INVITE & EMAIL", fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = Color.Gray) }
        },
        title = { Text("INVITE BY EMAIL", fontWeight = FontWeight.ExtraBold) },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("MEMBER EMAIL") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
