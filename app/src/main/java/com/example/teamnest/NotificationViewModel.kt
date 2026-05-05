package com.example.teamnest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class NotificationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var invitationsListener: ListenerRegistration? = null
    private var tasksListener: ListenerRegistration? = null
    private var groupsListener: ListenerRegistration? = null
    
    private var isInitialGroupsProcessed = false
    private var currentListeningEmail: String? = null
    
    // Store previous group states to detect role changes
    private val previousCoLeaderStatus = mutableMapOf<String, Boolean>()

    fun startGlobalListening(context: Context) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return
        val appContext = context.applicationContext
        val roomDb = AppDatabase.getDatabase(appContext).notifiedNotificationDao()

        if (currentListeningEmail == userEmail) return
        
        Log.d("Notifications", "Starting global listeners for $userEmail")
        stopAllListeners()
        currentListeningEmail = userEmail
        isInitialGroupsProcessed = false
        previousCoLeaderStatus.clear()

        // 1. Listen for Group Invitations
        // We remove the initial processing flag here and rely on Room DB to avoid duplicates.
        // This ensures that even "missed" invitations trigger a notification when the app opens.
        invitationsListener = db.collection("invitations")
            .whereEqualTo("inviteeEmail", userEmail)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Notifications", "Invitations listener error", e)
                    return@addSnapshotListener
                }
                snapshot?.let { snap ->
                    snap.documents.forEach { doc ->
                        val invite = doc.toObject(Invitation::class.java)
                        if (invite != null) {
                            val notificationId = "invite_${invite.id}"
                            checkAndNotify(
                                appContext, 
                                roomDb, 
                                notificationId, 
                                "New Team Invitation!", 
                                "You've been invited to join '${invite.groupName}' by ${invite.inviterEmail}", 
                                invite.id.hashCode()
                            )
                        }
                    }
                }
            }

        // 2. Listen for Urgent Task Reminders
        tasksListener = db.collection("tasks")
            .whereEqualTo("assigneeEmail", userEmail)
            .whereEqualTo("isCompleted", false)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val today = LocalDate.now()
                val tasks = snapshot?.toObjects(Task::class.java) ?: emptyList()
                
                tasks.forEach { task ->
                    try {
                        if (task.deadline.isBlank()) return@forEach
                        val deadline = LocalDate.parse(task.deadline)
                        val daysUntil = ChronoUnit.DAYS.between(today, deadline)
                        
                        // Notify if due within 3 days
                        if (daysUntil in 0..3) {
                            val notificationId = "task_urgent_${task.id}_${task.deadline}"
                            checkAndNotify(appContext, roomDb, notificationId, "Urgent Task: ${task.title}", "Due in $daysUntil days in '${task.groupName}'", task.id.hashCode())
                        } else if (daysUntil < 0) {
                            val notificationId = "task_overdue_${task.id}"
                            checkAndNotify(appContext, roomDb, notificationId, "Task Overdue!", "'${task.title}' is overdue since ${task.deadline}", task.id.hashCode())
                        }
                    } catch (ex: Exception) {
                        Log.e("Notifications", "Date error", ex)
                    }
                }
            }

        // 3. Listen for Leadership/Role Changes
        groupsListener = db.collection("groups")
            .whereArrayContains("memberEmails", userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                snapshot?.let { snap ->
                    snap.documentChanges.forEach { change ->
                        val group = change.document.toObject(Group::class.java)
                        val isCoLeader = group.coLeaderEmails.any { it.equals(userEmail, ignoreCase = true) }
                        val isOwner = group.leaderId == auth.currentUser?.uid

                        when (change.type) {
                            DocumentChange.Type.MODIFIED -> {
                                if (isInitialGroupsProcessed) {
                                    val wasCoLeader = previousCoLeaderStatus[group.id] ?: false
                                    
                                    if (!wasCoLeader && isCoLeader && !isOwner) {
                                        val nid = "role_leader_${group.id}"
                                        checkAndNotify(appContext, roomDb, nid, "Promotion!", "You are now a Leader in '${group.name}'", group.id.hashCode() + 10)
                                    } else if (wasCoLeader && !isCoLeader && !isOwner) {
                                        val nid = "role_member_${group.id}"
                                        checkAndNotify(appContext, roomDb, nid, "Role Updated", "You are no longer a Leader in '${group.name}'", group.id.hashCode() + 11)
                                    }
                                }
                            }
                            else -> {}
                        }
                        previousCoLeaderStatus[group.id] = isCoLeader
                    }
                    isInitialGroupsProcessed = true
                }
            }
    }

    private fun checkAndNotify(context: Context, dao: NotifiedNotificationDao, idString: String, title: String, message: String, idInt: Int) {
        viewModelScope.launch {
            if (!dao.isNotified(idString)) {
                showSystemNotification(context, title, message, idInt)
                dao.insertNotified(NotifiedNotification(idString))
            }
        }
    }

    private fun showSystemNotification(context: Context, title: String, message: String, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "teamnest_smart_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "TeamNest Activity", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Urgent alerts, invitations, and role changes"
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher_foreground) // Use app icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(0xFFF7931E.toInt()) // Brand Orange
            .build()

        notificationManager.notify(id, notification)
    }

    fun stopAllListeners() {
        invitationsListener?.remove()
        tasksListener?.remove()
        groupsListener?.remove()
        invitationsListener = null
        tasksListener = null
        groupsListener = null
        currentListeningEmail = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAllListeners()
    }
}
