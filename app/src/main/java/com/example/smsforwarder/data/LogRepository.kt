package com.example.smsforwarder.data

import com.example.smsforwarder.data.entities.LogEntry
import kotlinx.coroutines.flow.Flow

class LogRepository(private val logDao: LogDao) {

    val recentLogs: Flow<List<LogEntry>> = logDao.getRecentLogs()

    suspend fun insertLog(sender: String, messageBody: String, forwardStatus: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            sender = sender,
            messageBody = messageBody,
            forwardStatus = forwardStatus
        )
        logDao.insert(entry)
        logDao.trimOldLogs()
    }
}
