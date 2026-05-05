package com.example.teamnest

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class GroupDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    val group = mutableStateOf<Group?>(null)
    val tasks = mutableStateListOf<Task>()
    val files = mutableStateListOf<SharedFile>()
    val ideas = mutableStateListOf<Idea>()
    val isUploading = mutableStateOf(false)
    val isPostingIdea = mutableStateOf(false)

    private var groupListener: ListenerRegistration? = null
    private var tasksListener: ListenerRegistration? = null
    private var filesListener: ListenerRegistration? = null
    private var ideasListener: ListenerRegistration? = null

    fun listenToGroup(groupId: String) {
        groupListener?.remove()
        groupListener = db.collection("groups").document(groupId).addSnapshotListener { s, _ ->
            group.value = s?.toObject(Group::class.java)
        }

        tasksListener?.remove()
        tasksListener = db.collection("tasks").whereEqualTo("groupId", groupId).addSnapshotListener { s, _ ->
            tasks.clear()
            val sortedTasks = s?.toObjects(Task::class.java)?.sortedByDescending { it.timestamp } ?: emptyList()
            tasks.addAll(sortedTasks)
        }

        filesListener?.remove()
        filesListener = db.collection("shared_files").whereEqualTo("groupId", groupId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { s, e ->
                if (e != null) {
                    Log.e("FirestoreError", "Files query failed: ${e.message}")
                    return@addSnapshotListener
                }
                files.clear()
                s?.toObjects(SharedFile::class.java)?.let { files.addAll(it) }
            }

        // Using in-memory sorting to avoid Firestore Index requirement for simpler setup
        ideasListener?.remove()
        ideasListener = db.collection("ideas").whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirestoreError", "Ideas query failed: ${e.message}")
                    return@addSnapshotListener
                }
                val sortedIdeas = snapshot?.documents?.mapNotNull { it.toObject(Idea::class.java) }
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                ideas.clear()
                ideas.addAll(sortedIdeas)
            }
    }

    fun updateGroupDetails(context: Context, groupId: String, newName: String, newDescription: String, onComplete: () -> Unit) {
        if (newName.isBlank()) {
            Toast.makeText(context, "Group name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("groups").document(groupId)
            .update(mapOf(
                "name" to newName.trim(),
                "description" to newDescription.trim()
            ))
            .addOnSuccessListener {
                Toast.makeText(context, "Group updated successfully", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error updating group: ${it.message}", Toast.LENGTH_SHORT).show()
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

    fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        db.collection("tasks").document(taskId).update("isCompleted", isCompleted)
    }

    fun deleteTask(taskId: String) {
        db.collection("tasks").document(taskId).delete()
    }

    fun uploadFile(groupId: String, uri: Uri, context: Context) {
        isUploading.value = true
        val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "file_${System.currentTimeMillis()}"
        val ref = storage.reference.child("group_files/$groupId/$fileName")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                val fileRef = db.collection("shared_files").document()
                val type = context.contentResolver.getType(uri) ?: "unknown"
                val sharedFile = SharedFile(
                    fileRef.id, groupId, fileName, url.toString(), type,
                    auth.currentUser?.email ?: "", System.currentTimeMillis()
                )
                fileRef.set(sharedFile)
                isUploading.value = false
            }
        }.addOnFailureListener { isUploading.value = false }
    }

    fun postIdea(context: Context, groupId: String, content: String, authorName: String, onSuccess: () -> Unit) {
        if (content.isBlank()) return
        isPostingIdea.value = true
        val ref = db.collection("ideas").document()
        val idea = Idea(
            id = ref.id,
            groupId = groupId,
            content = content.trim(),
            author = authorName,
            timestamp = System.currentTimeMillis(),
            likes = emptyList()
        )
        ref.set(idea).addOnSuccessListener {
            isPostingIdea.value = false
            onSuccess()
            Toast.makeText(context, "Idea shared successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            isPostingIdea.value = false
            Log.e("FirestoreError", "Error sharing idea", it)
            Toast.makeText(context, "Failed to share idea: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleLike(idea: Idea) {
        val userId = auth.currentUser?.uid ?: return
        val newLikes = if (idea.likes.contains(userId)) idea.likes - userId else idea.likes + userId
        db.collection("ideas").document(idea.id).update("likes", newLikes)
    }

    override fun onCleared() {
        super.onCleared()
        groupListener?.remove()
        tasksListener?.remove()
        filesListener?.remove()
        ideasListener?.remove()
    }
}
