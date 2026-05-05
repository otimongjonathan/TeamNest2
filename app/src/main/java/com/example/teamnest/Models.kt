package com.example.teamnest

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profilePicUrl: String = ""
)

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val leaderId: String = "", // This is the original creator
    val coLeaderEmails: List<String> = emptyList(),
    val memberEmails: List<String> = emptyList(),
    val joinCode: String = "",
    val timestamp: Long = 0
)

data class Task(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val title: String = "",
    val description: String = "",
    val deadline: String = "",
    val assigneeEmail: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    val timestamp: Long = 0
)

data class Idea(
    val id: String = "",
    val groupId: String = "",
    val content: String = "",
    val author: String = "",
    val timestamp: Long = 0,
    var likes: List<String> = emptyList()
)

data class Invitation(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val inviterEmail: String = "",
    val inviteeEmail: String = "",
    val status: String = "PENDING" // PENDING, ACCEPTED, DECLINED
)

data class SharedFile(
    val id: String = "",
    val groupId: String = "",
    val name: String = "",
    val url: String = "",
    val type: String = "", // PDF, PPT, IMAGE
    val sender: String = "",
    val timestamp: Long = 0
)

data class OnboardingPage(val title: String, val description: String, val icon: ImageVector)
