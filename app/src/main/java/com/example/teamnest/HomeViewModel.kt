package com.example.teamnest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val tasks = mutableStateListOf<Task>()
    val groups = mutableStateListOf<Group>()
    val invitations = mutableStateListOf<Invitation>()

    private var tasksListener: ListenerRegistration? = null
    private var groupsListener: ListenerRegistration? = null
    private var invitationsListener: ListenerRegistration? = null

    fun startListening(context: Context) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return

        tasksListener?.remove()
        tasksListener = db.collection("tasks")
            .whereEqualTo("assigneeEmail", userEmail)
            .whereEqualTo("isCompleted", false)
            .addSnapshotListener { snapshot, _ ->
                tasks.clear()
                snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) }?.let { tasks.addAll(it) }
            }

        groupsListener?.remove()
        groupsListener = db.collection("groups")
            .whereArrayContains("memberEmails", userEmail)
            .limit(3)
            .addSnapshotListener { snapshot, _ ->
                groups.clear()
                snapshot?.documents?.mapNotNull { it.toObject(Group::class.java) }?.let { groups.addAll(it) }
            }

        invitationsListener?.remove()
        invitationsListener = db.collection("invitations")
            .whereEqualTo("inviteeEmail", userEmail)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val invite = change.document.toObject(Invitation::class.java)
                        showInviteNotification(context, invite)
                    }
                }

                invitations.clear()
                snapshot?.documents?.mapNotNull { it.toObject(Invitation::class.java) }?.let { invitations.addAll(it) }
            }
    }

    private fun showInviteNotification(context: Context, invite: Invitation) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "invites_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Group Invitations", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("New Group Invitation!")
            .setContentText("You've been invited to join '${invite.groupName}' by ${invite.inviterEmail}")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(invite.id.hashCode(), notification)
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
        tasksListener?.remove()
        groupsListener?.remove()
        invitationsListener?.remove()
    }
}
