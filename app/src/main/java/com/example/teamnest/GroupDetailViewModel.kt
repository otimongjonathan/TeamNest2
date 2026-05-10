package com.example.teamnest

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val group = mutableStateOf<Group?>(null)
    val tasks = mutableStateListOf<Task>()
    val files = mutableStateListOf<SharedFile>()
    val ideas = mutableStateListOf<Idea>()
    val isUploading = mutableStateOf(false)
    val isPostingIdea = mutableStateOf(false)
    
    // UI Status
    val syncStatus = mutableStateOf("Initializing sync...")

    private var groupListener: ListenerRegistration? = null
    private var tasksListener: ListenerRegistration? = null
    private var filesListener: ListenerRegistration? = null
    private var ideasListener: ListenerRegistration? = null

    fun listenToGroup(groupId: String, context: Context? = null) {
        syncStatus.value = "Connecting to group..."
        Log.e("TEAMNEST_DEBUG", "STARTING SYNC: $groupId")
        
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

        startFilesListener(groupId, true)

        ideasListener?.remove()
        ideasListener = db.collection("ideas").whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val sortedIdeas = snapshot?.documents?.mapNotNull { it.toObject(Idea::class.java) }
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                ideas.clear()
                ideas.addAll(sortedIdeas)
            }
    }

    private fun startFilesListener(groupId: String, useOrdering: Boolean) {
        filesListener?.remove()
        val query = if (useOrdering) {
            db.collection("shared_files")
                .whereEqualTo("groupId", groupId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            db.collection("shared_files")
                .whereEqualTo("groupId", groupId)
        }

        filesListener = query.addSnapshotListener { s, e ->
            if (e != null) {
                if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION && useOrdering) {
                    syncStatus.value = "Indexing... (Fallback order)"
                    Log.e("TEAMNEST_DEBUG", "MISSING INDEX: Visit Firebase Console to fix.")
                    startFilesListener(groupId, false)
                } else {
                    syncStatus.value = "Sync Error: ${e.code}"
                    Log.e("TEAMNEST_DEBUG", "FILES ERROR: ${e.message}")
                }
                return@addSnapshotListener
            }
            
            val fetchedFiles = s?.toObjects(SharedFile::class.java) ?: emptyList()
            syncStatus.value = "Synced: ${fetchedFiles.size} files found"
            Log.e("TEAMNEST_DEBUG", "FOUND ${fetchedFiles.size} FILES")
            
            files.clear()
            if (!useOrdering) {
                files.addAll(fetchedFiles.sortedByDescending { it.timestamp })
            } else {
                files.addAll(fetchedFiles)
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) name = it.getString(nameIndex)
                }
            }
        }
        return name ?: uri.path?.substringAfterLast('/')
    }

    fun uploadFile(groupId: String, uri: Uri, context: Context) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: "Unknown"
        
        viewModelScope.launch {
            isUploading.value = true
            syncStatus.value = "Uploading to storage..."
            try {
                val rawFileName = getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
                val fileName = rawFileName.replace(":", "_").replace(" ", "_")
                
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Could not read file")
                inputStream.close()

                val bucket = SupabaseConfig.client.storage.from("teamnest-files")
                val path = "$groupId/$fileName"
                
                bucket.upload(path, bytes) { upsert = true }
                val publicUrl = bucket.publicUrl(path)
                
                syncStatus.value = "Sharing with group..."
                val fileRef = db.collection("shared_files").document()
                val sharedFile = SharedFile(
                    id = fileRef.id,
                    groupId = groupId,
                    name = fileName,
                    url = publicUrl,
                    type = context.contentResolver.getType(uri) ?: "unknown",
                    sender = userEmail,
                    timestamp = System.currentTimeMillis()
                )
                
                fileRef.set(sharedFile).addOnSuccessListener {
                    Toast.makeText(context, "File shared!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    syncStatus.value = "Sharing failed: ${it.message}"
                }
                
            } catch (e: Exception) {
                Log.e("TEAMNEST_DEBUG", "UPLOAD FAILED: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isUploading.value = false
            }
        }
    }

    fun downloadFile(context: Context, file: SharedFile) {
        try {
            val request = DownloadManager.Request(Uri.parse(file.url))
                .setTitle(file.name)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.name)
            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateGroupDetails(context: Context, groupId: String, newName: String, newDescription: String, onComplete: () -> Unit) {
        db.collection("groups").document(groupId).update(mapOf("name" to newName.trim(), "description" to newDescription.trim()))
            .addOnSuccessListener { onComplete() }
    }

    fun createTask(context: Context, group: Group, title: String, description: String, email: String, deadline: String, onComplete: () -> Unit) {
        val ref = db.collection("tasks").document()
        val newTask = Task(ref.id, group.id, group.name, title, description, deadline, email.trim().lowercase(), false, System.currentTimeMillis())
        ref.set(newTask).addOnSuccessListener { onComplete() }
    }

    fun updateTaskCompletion(taskId: String, isCompleted: Boolean) {
        db.collection("tasks").document(taskId).update("isCompleted", isCompleted)
    }

    fun deleteTask(taskId: String) {
        db.collection("tasks").document(taskId).delete()
    }

    fun postIdea(context: Context, groupId: String, content: String, authorName: String, onSuccess: () -> Unit) {
        val ref = db.collection("ideas").document()
        val idea = Idea(ref.id, groupId, content.trim(), authorName, System.currentTimeMillis(), emptyList())
        ref.set(idea).addOnSuccessListener { onSuccess() }
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
