package com.example.teamnest

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class GroupsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val groups = mutableStateListOf<Group>()
    private var groupsListener: ListenerRegistration? = null

    val groupMembers = mutableStateListOf<MemberDetail>()

    fun startListening() {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return
        groupsListener?.remove()
        groupsListener = db.collection("groups")
            .whereArrayContains("memberEmails", userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("GroupsViewModel", "Error listening to groups", e)
                    return@addSnapshotListener
                }
                groups.clear()
                val sortedGroups = snapshot?.documents?.mapNotNull { it.toObject(Group::class.java) }
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                groups.addAll(sortedGroups)
            }
    }

    fun fetchGroupMembers(memberEmails: List<String>, leaderId: String, coLeaderEmails: List<String> = emptyList()) {
        groupMembers.clear()
        if (memberEmails.isEmpty()) return

        val chunks = memberEmails.chunked(10)
        
        chunks.forEach { chunk ->
            db.collection("users")
                .whereIn("email", chunk.map { it.lowercase() })
                .get()
                .addOnSuccessListener { snapshot ->
                    val members = snapshot.documents.mapNotNull { doc ->
                        val profile = doc.toObject(UserProfile::class.java)
                        if (profile != null) {
                            val role = when {
                                profile.id == leaderId -> "Leader (Owner)"
                                coLeaderEmails.contains(profile.email.lowercase()) -> "Leader"
                                else -> "Member"
                            }
                            MemberDetail(
                                id = profile.id,
                                name = profile.name,
                                email = profile.email,
                                role = role
                            )
                        } else null
                    }
                    groupMembers.addAll(members)
                    val currentList = groupMembers.toList()
                    groupMembers.clear()
                    groupMembers.addAll(currentList.sortedWith(
                        compareByDescending<MemberDetail> { it.role.contains("Leader") }
                            .thenByDescending { it.role.contains("Owner") }
                            .thenBy { it.name }
                    ))
                }
                .addOnFailureListener { e ->
                    Log.e("GroupsViewModel", "Error fetching members chunk", e)
                }
        }
    }

    fun removeMember(groupId: String, memberEmail: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("groups").document(groupId).get().addOnSuccessListener { snapshot ->
            val group = snapshot.toObject(Group::class.java)
            if (group != null) {
                val updatedMembers = group.memberEmails.filter { it.lowercase() != memberEmail.lowercase() }
                val updatedCoLeaders = group.coLeaderEmails.filter { it.lowercase() != memberEmail.lowercase() }
                db.collection("groups").document(groupId).update(
                    "memberEmails", updatedMembers,
                    "coLeaderEmails", updatedCoLeaders
                )
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
        }.addOnFailureListener { onResult(false, it.message) }
    }

    fun promoteToLeader(groupId: String, memberEmail: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("groups").document(groupId).get().addOnSuccessListener { snapshot ->
            val group = snapshot.toObject(Group::class.java)
            if (group != null) {
                val updatedCoLeaders = (group.coLeaderEmails + memberEmail.lowercase()).distinct()
                db.collection("groups").document(groupId).update("coLeaderEmails", updatedCoLeaders)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
        }.addOnFailureListener { onResult(false, it.message) }
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

    fun joinGroupByCode(code: String, onResult: (Boolean, String?) -> Unit) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return
        db.collection("groups").whereEqualTo("joinCode", code).get().addOnSuccessListener { snapshot ->
            val doc = snapshot.documents.firstOrNull()
            if (doc != null) {
                val group = doc.toObject(Group::class.java)
                if (group != null) {
                    if (group.memberEmails.size >= 15) {
                        onResult(false, "Group is full (max 15 members)")
                    } else if (group.memberEmails.contains(userEmail)) {
                        onResult(false, "You are already a member")
                    } else {
                        db.collection("groups").document(group.id)
                            .update("memberEmails", group.memberEmails + userEmail)
                            .addOnSuccessListener { onResult(true, null) }
                            .addOnFailureListener { onResult(false, it.message) }
                    }
                } else {
                    onResult(false, "Invalid Group Data")
                }
            } else {
                onResult(false, "Invalid Join Code")
            }
        }.addOnFailureListener {
            onResult(false, it.message)
        }
    }

    fun createGroup(name: String, description: String) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: return
        val uid = auth.currentUser?.uid ?: return
        val newGroupRef = db.collection("groups").document()
        val joinCode = (100000..999999).random().toString()
        val group = Group(
            id = newGroupRef.id,
            name = name.uppercase(),
            description = description,
            leaderId = uid,
            memberEmails = listOf(userEmail),
            joinCode = joinCode,
            timestamp = System.currentTimeMillis()
        )
        newGroupRef.set(group)
    }

    fun deleteGroup(groupId: String, onResult: (Boolean, String?) -> Unit) {
        val batch = db.batch()
        
        // 1. Delete group document
        batch.delete(db.collection("groups").document(groupId))
        
        // We need to fetch and delete associated data
        // Using a sequential approach for simplicity in cleanup
        db.collection("tasks").whereEqualTo("groupId", groupId).get().addOnSuccessListener { taskSnap ->
            taskSnap.documents.forEach { batch.delete(it.reference) }
            
            db.collection("ideas").whereEqualTo("groupId", groupId).get().addOnSuccessListener { ideaSnap ->
                ideaSnap.documents.forEach { batch.delete(it.reference) }
                
                db.collection("shared_files").whereEqualTo("groupId", groupId).get().addOnSuccessListener { fileSnap ->
                    fileSnap.documents.forEach { batch.delete(it.reference) }
                    
                    db.collection("invitations").whereEqualTo("groupId", groupId).get().addOnSuccessListener { inviteSnap ->
                        inviteSnap.documents.forEach { batch.delete(it.reference) }
                        
                        // Execute batch
                        batch.commit()
                            .addOnSuccessListener { onResult(true, null) }
                            .addOnFailureListener { onResult(false, it.message) }
                    }
                }
            }
        }.addOnFailureListener { onResult(false, it.message) }
    }

    override fun onCleared() {
        super.onCleared()
        groupsListener?.remove()
    }
}

data class MemberDetail(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)
