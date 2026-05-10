package com.example.teamnest

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val tasks = mutableStateListOf<Task>()
    val groups = mutableStateListOf<Group>()
    val invitations = mutableStateListOf<Invitation>()
    val recentFiles = mutableStateListOf<SharedFile>()
    
    // UI Status Indicator
    val syncStatus = mutableStateOf("Initializing...")

    private var tasksListener: ListenerRegistration? = null
    private var groupsListener: ListenerRegistration? = null
    private var invitationsListener: ListenerRegistration? = null
    private var filesListener: ListenerRegistration? = null
    private var currentListeningEmail: String? = null

    fun startListening(context: Context? = null) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return
        syncStatus.value = "Connecting as $userEmail..."
        
        Log.e("TEAMNEST_DEBUG", ">>> HOME SYNC START for $userEmail <<<")

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
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        syncStatus.value = "Group Error: ${e.code}"
                        Log.e("TEAMNEST_DEBUG", "Group listener error: ${e.message}")
                        return@addSnapshotListener
                    }
                    val fetchedGroups = snapshot?.documents?.mapNotNull { it.toObject(Group::class.java) } ?: emptyList()
                    groups.clear()
                    groups.addAll(fetchedGroups.sortedByDescending { it.timestamp }.take(3))
                    
                    listenToRecentFiles(fetchedGroups.map { it.id }, context = context)
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

    private fun listenToRecentFiles(groupIds: List<String>, useOrdering: Boolean = true, context: Context? = null) {
        filesListener?.remove()
        if (groupIds.isEmpty()) {
            recentFiles.clear()
            syncStatus.value = "No groups found."
            return
        }

        syncStatus.value = "Fetching group files..."
        val limitedGroupIds = groupIds.take(30)
        
        var query = db.collection("shared_files").whereIn("groupId", limitedGroupIds)
        if (useOrdering) {
            query = query.orderBy("timestamp", Query.Direction.DESCENDING).limit(5)
        }
        
        filesListener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION && useOrdering) {
                    syncStatus.value = "Index building... (fallback active)"
                    Log.e("TEAMNEST_DEBUG", "HOME INDEX MISSING! Falling back...")
                    listenToRecentFiles(groupIds, false, context)
                } else {
                    syncStatus.value = "File Error: ${e.code}"
                    Log.e("TEAMNEST_DEBUG", "HOME FILES ERROR: ${e.message}")
                }
                return@addSnapshotListener
            }
            val fetched = snapshot?.toObjects(SharedFile::class.java) ?: emptyList()
            syncStatus.value = "Connected: ${fetched.size} files found"
            Log.e("TEAMNEST_DEBUG", "HOME SYNC SUCCESS: Received ${fetched.size} files")
            
            recentFiles.clear()
            recentFiles.addAll(if (!useOrdering) fetched.sortedByDescending { it.timestamp }.take(5) else fetched)
        }
    }

    fun downloadFile(context: Context, file: SharedFile) {
        try {
            val request = DownloadManager.Request(file.url.toUri())
                .setTitle(file.name)
                .setDescription("Downloading from TeamNest")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.name)
            
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopListening() {
        tasksListener?.remove()
        groupsListener?.remove()
        invitationsListener?.remove()
        filesListener?.remove()
        tasksListener = null
        groupsListener = null
        invitationsListener = null
        filesListener = null
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

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
