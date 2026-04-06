package com.example.smsforwarder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.smsforwarder.data.entities.LogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogEntry)

    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<LogEntry>>

    @Query("DELETE FROM logs WHERE id NOT IN (SELECT id FROM logs ORDER BY timestamp DESC LIMIT 50)")
    suspend fun trimOldLogs()
}
