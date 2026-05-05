package com.example.teamnest

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TasksViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val myTasks = mutableStateListOf<Task>()
    val leadGroups = mutableStateListOf<Group>()
    val selectedGroupTasks = mutableStateListOf<Task>()

    private var myTasksListener: ListenerRegistration? = null
    private var leadGroupsListener: ListenerRegistration? = null
    private var groupTasksListener: ListenerRegistration? = null

    fun startListeningMyTasks() {
        val email = auth.currentUser?.email?.lowercase() ?: return
        myTasksListener?.remove()
        myTasksListener = db.collection("tasks")
            .whereEqualTo("assigneeEmail", email)
            .addSnapshotListener { snapshot, _ ->
                myTasks.clear()
                val sortedTasks = snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) }
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                myTasks.addAll(sortedTasks)
            }
    }

    fun startListeningLeadGroups() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email?.lowercase() ?: return
        leadGroupsListener?.remove()
        
        // Show groups where user is either the Owner (leaderId) or a Co-Leader
        leadGroupsListener = db.collection("groups")
            .where(
                com.google.firebase.firestore.Filter.or(
                    com.google.firebase.firestore.Filter.equalTo("leaderId", uid),
                    com.google.firebase.firestore.Filter.arrayContains("coLeaderEmails", email)
                )
            )
            .addSnapshotListener { snapshot, _ ->
                leadGroups.clear()
                val sortedGroups = snapshot?.documents?.mapNotNull { it.toObject(Group::class.java) }
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                leadGroups.addAll(sortedGroups)
            }
    }

    fun listenToGroupTasks(groupId: String) {
        groupTasksListener?.remove()
        groupTasksListener = db.collection("tasks")
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, _ ->
                selectedGroupTasks.clear()
                val sortedTasks = snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) }
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                selectedGroupTasks.addAll(sortedTasks)
            }
    }

    fun createTask(context: Context, group: Group, title: String, description: String, email: String, deadline: String, onComplete: () -> Unit) {
        val assigneeEmail = email.trim().lowercase()
        if (group.memberEmails.any { it.equals(assigneeEmail, ignoreCase = true) }) {
            val ref = db.collection("tasks").document()
            val newTask = Task(ref.id, group.id, group.name, title, description, deadline, assigneeEmail, false, System.currentTimeMillis())
            ref.set(newTask).addOnSuccessListener {
                sendTaskEmail(context, email, title, group.name, deadline)
                Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
                onComplete()
            }.addOnFailureListener {
                Toast.makeText(context, "Error creating task: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Error: Assignee '$assigneeEmail' is not a member of this group.", Toast.LENGTH_LONG).show()
        }
    }

    fun updateTask(taskId: String, newTitle: String, newDescription: String, newAssignee: String, newDeadline: String, group: Group, onComplete: () -> Unit) {
        val assigneeEmail = newAssignee.trim().lowercase()
        if (group.memberEmails.any { it.equals(assigneeEmail, ignoreCase = true) }) {
            val updates = mapOf(
                "title" to newTitle,
                "description" to newDescription,
                "assigneeEmail" to assigneeEmail,
                "deadline" to newDeadline
            )
            db.collection("tasks").document(taskId).update(updates).addOnSuccessListener {
                onComplete()
            }
        }
    }

    fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        db.collection("tasks").document(taskId).update("isCompleted", isCompleted)
    }

    fun deleteTask(taskId: String) {
        db.collection("tasks").document(taskId).delete()
    }

    fun removeMember(groupId: String, memberEmail: String) {
        db.collection("groups").document(groupId).get().addOnSuccessListener { snapshot ->
            val group = snapshot.toObject(Group::class.java)
            if (group != null) {
                val updatedMembers = group.memberEmails.filter { it.lowercase() != memberEmail.lowercase() }
                val updatedCoLeaders = group.coLeaderEmails.filter { it.lowercase() != memberEmail.lowercase() }
                db.collection("groups").document(groupId).update(
                    "memberEmails", updatedMembers,
                    "coLeaderEmails", updatedCoLeaders
                )
            }
        }
    }

    fun sendInvite(context: Context, group: Group, inviterEmail: String, inviteeEmail: String) {
        val email = inviteeEmail.trim().lowercase()
        
        if (group.memberEmails.contains(email)) {
            Toast.makeText(context, "User is already a member", Toast.LENGTH_SHORT).show()
            return
        }

        if (group.memberEmails.size >= 15) {
            Toast.makeText(context, "Group is full (max 15 members)", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("invitations")
            .whereEqualTo("groupId", group.id)
            .whereEqualTo("inviteeEmail", email)
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    Toast.makeText(context, "Invitation already pending for this user", Toast.LENGTH_SHORT).show()
                } else {
                    val ref = db.collection("invitations").document()
                    val invitation = Invitation(
                        id = ref.id,
                        groupId = group.id,
                        groupName = group.name,
                        inviterEmail = inviterEmail,
                        inviteeEmail = email,
                        status = "PENDING"
                    )
                    ref.set(invitation).addOnSuccessListener {
                        Toast.makeText(context, "Invitation sent!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        myTasksListener?.remove()
        leadGroupsListener?.remove()
        groupTasksListener?.remove()
    }
}
