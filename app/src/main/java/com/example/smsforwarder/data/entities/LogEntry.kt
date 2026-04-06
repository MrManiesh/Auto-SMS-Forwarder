package com.example.smsforwarder.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val sender: String,
    val messageBody: String,
    val forwardStatus: String // Success, Failed SMS, Failed Email, etc.
)
