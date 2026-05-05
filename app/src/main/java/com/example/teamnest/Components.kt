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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import com.example.teamnest.ui.theme.LocalIsDarkTheme
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
    val isDark = LocalIsDarkTheme.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF321414) else Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) Color(0xFF5A1E1E) else Color(0xFFFFCDD2)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isDark) Color(0xFF5A1E1E) else Color(0xFFFFCDD2),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PriorityHigh, 
                    contentDescription = null, 
                    tint = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = t.title, 
                    fontWeight = FontWeight.Black, 
                    color = if (isDark) Color(0xFFFF8A80) else Color(0xFFB71C1C), 
                    fontSize = 16.sp
                )
                Text(
                    text = t.groupName.uppercase(), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        Icons.Default.Timer, 
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp), 
                        tint = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Due: ${t.deadline}", 
                        color = if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (t.timestamp > 0) {
                    Text(
                        text = "Assigned ${formatTimestamp(t.timestamp)}",
                        fontSize = 10.sp,
                        color = (if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F)).copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (onDone != null) {
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "DONE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun TaskCard(t: Task, isCompleted: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isCompleted) Color.Gray.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = t.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (t.timestamp > 0) {
                        Text(
                            text = formatTimestamp(t.timestamp),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                Text(
                    text = t.groupName, 
                    fontSize = 13.sp, 
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(text = t.deadline, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    g: Group, 
    currentUserId: String, 
    onViewMembers: () -> Unit, 
    onDeleteGroup: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val isLeader = g.leaderId == currentUserId
    val isDark = LocalIsDarkTheme.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = g.name.uppercase(), 
                            fontWeight = FontWeight.Black, 
                            fontSize = 18.sp, 
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (g.timestamp > 0) {
                            Text(
                                text = formatTimestamp(g.timestamp),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = g.description, 
                        fontSize = 14.sp, 
                        color = Color.Gray, 
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    if (isLeader && g.joinCode.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable {
                                clipboardManager.setText(AnnotatedString(g.joinCode))
                                Toast.makeText(context, "Join code copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.VpnKey, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(14.dp), 
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = g.joinCode, 
                                    fontSize = 14.sp, 
                                    fontWeight = FontWeight.Black, 
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.width(12.dp))
                                Icon(
                                    Icons.Default.ContentCopy, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(14.dp), 
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text(text = "${g.memberEmails.size} Members", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLeader) Icons.Default.Security else Icons.Default.Person, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(14.dp), 
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isLeader) "LEADER" else "MEMBER",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
                
                if (isLeader && onDeleteGroup != null) {
                    IconButton(
                        onClick = onDeleteGroup,
                        modifier = Modifier.size(32.dp).background(Color.Red.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Group", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onViewMembers,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("TEAM DIRECTORY", fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("DELETE TEAM", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        title = { Text("DELETE TEAM?", fontWeight = FontWeight.Black, color = Color(0xFFD32F2F), letterSpacing = 1.sp) },
        text = {
            Text("Are you sure you want to delete \"$groupName\"? All shared files, tasks, and ideas will be permanently removed.")
        }
    )
}

@Composable
fun MembersDialog(
    members: List<MemberDetail>,
    isCurrentUserLeader: Boolean,
    currentUserId: String,
    isGroupFull: Boolean,
    onRemoveMember: (MemberDetail) -> Unit,
    onPromoteToLeader: (MemberDetail) -> Unit,
    onInviteMember: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDismiss() }) { Text("CLOSE", fontWeight = FontWeight.Black) }
        },
        shape = RoundedCornerShape(32.dp),
        title = { 
            Column {
                Text("TEAM DIRECTORY", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
                Text("Manage your teammates", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Normal)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (members.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(members) { member ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(name = member.name, size = 44.dp, fontSize = 18.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.name.uppercase(), 
                                            fontWeight = FontWeight.Black, 
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = member.email, 
                                            fontSize = 12.sp, 
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Surface(
                                            color = if(member.role.contains("Leader")) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = member.role.uppercase(), 
                                                fontSize = 9.sp, 
                                                color = if(member.role.contains("Leader")) MaterialTheme.colorScheme.primary else Color.Gray, 
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                    
                                    if (isCurrentUserLeader && member.id != currentUserId) {
                                        var expanded by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(onClick = { expanded = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = Color.Gray)
                                            }
                                            DropdownMenu(
                                                expanded = expanded, 
                                                onDismissRequest = { expanded = false },
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                if (member.role == "Member") {
                                                    DropdownMenuItem(
                                                        text = { Text("Promote to Leader", fontWeight = FontWeight.Bold) },
                                                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                        onClick = { 
                                                            onPromoteToLeader(member)
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                                DropdownMenuItem(
                                                    text = { Text("Remove from Team", color = Color.Red, fontWeight = FontWeight.Bold) },
                                                    leadingIcon = { Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color.Red) },
                                                    onClick = {
                                                        onRemoveMember(member)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (isCurrentUserLeader && !isGroupFull && onInviteMember != null) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = onInviteMember,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("INVITE NEW MEMBER", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
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
            Button(
                onClick = { if (n.isNotBlank()) onConfirm(n, d) },
                shape = RoundedCornerShape(16.dp),
                enabled = n.isNotBlank()
            ) {
                Text(text = "CREATE TEAM", fontWeight = FontWeight.Black)
            }
        },
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = "NEW TEAM", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = n,
                    onValueChange = { n = it },
                    label = { Text("TEAM NAME") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it },
                    label = { Text("DESCRIPTION") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold)
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
            Button(
                onClick = { if (c.length == 6) onConfirm(c) },
                shape = RoundedCornerShape(16.dp),
                enabled = c.length == 6
            ) {
                Text(text = "JOIN TEAM", fontWeight = FontWeight.Black)
            }
        },
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = "JOIN BY CODE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column {
                Text(
                    text = "Enter the 6-digit invitation code to join your workspace.", 
                    fontSize = 14.sp, 
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = c,
                    onValueChange = { if (it.length <= 6) c = it.uppercase() },
                    label = { Text("INVITATION CODE") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 18.sp
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold)
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
            Button(
                onClick = {
                    if (t.isNotBlank() && e.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(e.trim()).matches()) {
                        onConfirm(t, d, e, dl)
                    } else if (t.isBlank()) {
                        Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter a valid assignee email", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(16.dp)
            ) { Text(text = "ASSIGN TASK", fontWeight = FontWeight.Black) }
        },
        shape = RoundedCornerShape(28.dp),
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold) }
        },
        title = { Text(text = "NEW ASSIGNMENT", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("TASK TITLE") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = e,
                    onValueChange = { e = it },
                    label = { Text("ASSIGNEE EMAIL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dl,
                    onValueChange = { dl = it },
                    label = { Text("DEADLINE (YYYY-MM-DD)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it },
                    label = { Text("DESCRIPTION (OPTIONAL)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
            Button(
                onClick = {
                    if (t.isNotBlank() && e.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(e.trim()).matches()) {
                        onConfirm(t, d, e, dl)
                    } else if (t.isBlank()) {
                        Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter a valid assignee email", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(16.dp)
            ) { Text(text = "UPDATE TASK", fontWeight = FontWeight.Black) }
        },
        shape = RoundedCornerShape(28.dp),
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold) }
        },
        title = { Text(text = "EDIT TASK", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = t,
                    onValueChange = { t = it },
                    label = { Text("TASK TITLE") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = e,
                    onValueChange = { e = it },
                    label = { Text("ASSIGNEE EMAIL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dl,
                    onValueChange = { dl = it },
                    label = { Text("DEADLINE") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = d,
                    onValueChange = { d = it },
                    label = { Text("DESCRIPTION") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onConfirm(email)
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.trim()))
                            putExtra(Intent.EXTRA_SUBJECT, "Join my Team on TeamNest")
                            putExtra(Intent.EXTRA_TEXT, "Hey!\n\nI've invited you to collaborate on a project using TeamNest. Download the app and join our workspace to get started.\n\nCheers!")
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Send Invitation Email"))
                        } catch (ex: Exception) {
                            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                enabled = email.isNotBlank()
            ) { 
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("SEND INVITE", fontWeight = FontWeight.Black) 
            }
        },
        shape = RoundedCornerShape(28.dp),
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold) }
        },
        title = { Text("INVITE TEAMMATE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column {
                Text(
                    text = "We'll send an invitation to this email address.", 
                    fontSize = 14.sp, 
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("EMAIL ADDRESS") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    )
}
