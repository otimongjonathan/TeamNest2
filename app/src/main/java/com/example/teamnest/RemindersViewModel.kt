package com.example.teamnest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class RemindersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val reminderTasks = mutableStateListOf<Task>()
    private var tasksListener: ListenerRegistration? = null
    
    private val notifiedTaskIds = mutableSetOf<String>()

    fun startListening(context: Context) {
        val email = auth.currentUser?.email?.lowercase() ?: return
        val today = LocalDate.now()

        tasksListener?.remove()
        tasksListener = db.collection("tasks")
            .whereEqualTo("assigneeEmail", email)
            .whereEqualTo("isCompleted", false)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(Task::class.java) ?: emptyList()
                val urgentTasks = list.filter {
                    try {
                        val deadline = LocalDate.parse(it.deadline)
                        val daysUntil = ChronoUnit.DAYS.between(today, deadline)
                        daysUntil in 0..3
                    } catch (e: Exception) {
                        false
                    }
                }
                
                urgentTasks.forEach { task ->
                    if (!notifiedTaskIds.contains(task.id)) {
                        showNotification(context, task)
                        notifiedTaskIds.add(task.id)
                    }
                }

                reminderTasks.clear()
                reminderTasks.addAll(urgentTasks)
            }
    }

    private fun showNotification(context: Context, task: Task) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "urgent_tasks"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Urgent Tasks", 
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Urgent Task: ${task.title}")
            .setContentText("Due on ${task.deadline} in ${task.groupName}")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard reliable icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(task.id.hashCode(), notification)
    }

    fun markAsComplete(taskId: String) {
        db.collection("tasks").document(taskId).update("isCompleted", true)
    }

    override fun onCleared() {
        super.onCleared()
        tasksListener?.remove()
    }
}
