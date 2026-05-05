package com.example.teamnest

import androidx.compose.runtime.mutableStateListOf
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

    fun startListening() {
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
                
                reminderTasks.clear()
                reminderTasks.addAll(urgentTasks)
            }
    }

    fun markAsComplete(taskId: String) {
        db.collection("tasks").document(taskId).update("isCompleted", true)
    }

    override fun onCleared() {
        super.onCleared()
        tasksListener?.remove()
    }
}
