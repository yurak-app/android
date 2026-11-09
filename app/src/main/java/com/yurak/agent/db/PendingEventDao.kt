package com.yurak.agent.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PendingEventDao {
    @Insert
    suspend fun insertAll(events: List<PendingEventEntity>)

    /** Oldest-first, same "roughly chronological" ordering the Windows agent's
     * timestamp-prefixed filenames give it for free. */
    @Query("SELECT * FROM pending_events ORDER BY id ASC")
    suspend fun listAll(): List<PendingEventEntity>

    @Query("SELECT COUNT(*) FROM pending_events")
    suspend fun count(): Int

    @Delete
    suspend fun delete(events: List<PendingEventEntity>)

    @Query("DELETE FROM pending_events")
    suspend fun deleteAll()

    /** The oldest [count] row ids — paired with [deleteByIds] in SyncWorker to trim the
     * table down to a cap (mirrors OfflineEventStore's own cap on Windows: an extended
     * outage or a burst shouldn't grow this forever). Two simple queries rather than one
     * clever one with a subquery inside LIMIT — easier to be confident compiles correctly
     * under Room's query validation, and this isn't a hot path worth the cleverness. */
    @Query("SELECT id FROM pending_events ORDER BY id ASC LIMIT :count")
    suspend fun oldestIds(count: Int): List<Long>

    @Query("DELETE FROM pending_events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
