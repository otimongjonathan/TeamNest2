package com.example.teamnest

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val tasks = mutableStateListOf<Task>()
    val groups = mutableStateListOf<Group>()
    val invitations = mutableStateListOf<Invitation>()

    private var tasksListener: ListenerRegistration? = null
    private var groupsListener: ListenerRegistration? = null
    private var invitationsListener: ListenerRegistration? = null
    private var currentListeningEmail: String? = null

    fun startListening(context: Context) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return

        // Reset if user changed
        if (currentListeningEmail != userEmail) {
            stopListening()
            currentListeningEmail = userEmail
        }

        if (tasksListener == null) {
            tasksListener = db.collection("tasks")
                .whereEqualTo("assigneeEmail", userEmail)
                .whereEqualTo("isCompleted", false)
                .addSnapshotListener { snapshot, _ ->
                    tasks.clear()
                    val sortedTasks = snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) }
                        ?.sortedByDescending { it.timestamp } ?: emptyList()
                    tasks.addAll(sortedTasks)
                }
        }

        if (groupsListener == null) {
            groupsListener = db.collection("groups")
                .whereArrayContains("memberEmails", userEmail)
                .addSnapshotListener { snapshot, _ ->
                    groups.clear()
                    val sortedGroups = snapshot?.documents?.mapNotNull { it.toObject(Group::class.java) }
                        ?.sortedByDescending { it.timestamp } ?: emptyList()
                    groups.addAll(sortedGroups.take(3))
                }
        }

        if (invitationsListener == null) {
            invitationsListener = db.collection("invitations")
                .whereEqualTo("inviteeEmail", userEmail)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    invitations.clear()
                    snapshot?.documents?.mapNotNull { it.toObject(Invitation::class.java) }?.let { invitations.addAll(it) }
                }
        }
    }

    private fun stopListening() {
        tasksListener?.remove()
        groupsListener?.remove()
        invitationsListener?.remove()
        tasksListener = null
        groupsListener = null
        invitationsListener = null
    }

    fun acceptInvitation(invite: Invitation, onResult: (Boolean, String?) -> Unit) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return
        db.runTransaction { transaction ->
            val groupRef = db.collection("groups").document(invite.groupId)
            val inviteRef = db.collection("invitations").document(invite.id)
            val groupDoc = transaction.get(groupRef)

            if (groupDoc.exists()) {
                val currentMembers = groupDoc.get("memberEmails") as? List<*> ?: emptyList<String>()
                if (!currentMembers.contains(userEmail)) {
                    transaction.update(groupRef, "memberEmails", currentMembers + userEmail)
                }
                transaction.update(inviteRef, "status", "ACCEPTED")
            } else {
                throw Exception("Group no longer exists")
            }
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            Log.e("HomeViewModel", "Transaction failed", e)
            onResult(false, e.message)
        }
    }

    fun declineInvitation(inviteId: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("invitations").document(inviteId).update("status", "DECLINED")
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun markTaskComplete(taskId: String) {
        db.collection("tasks").document(taskId).update("isCompleted", true)
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
