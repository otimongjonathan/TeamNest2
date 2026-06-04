package com.example.teamnest.ui.theme.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun sendTaskEmail(context: Context, email: String, title: String, groupName: String, deadline: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email.trim()))
        putExtra(Intent.EXTRA_SUBJECT, "New Task Assigned: $title")
        putExtra(Intent.EXTRA_TEXT, "Hello!\n\nYou have been assigned a new task: '$title' in the group '$groupName'.\nDeadline: $deadline\n\nPlease check the TeamNest app for more details.")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Send Task Notification Email"))
    } catch (ex: Exception) {
        Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
    }
}
