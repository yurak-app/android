package com.yurak.agent.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One durably-queued event, written the moment it's known and deleted only after the
 * backend has confirmed (2xx) it has it — the same discipline as the Windows agent's
 * OfflineEventStore (one file per segment) and the extension's storage.ts (one
 * chrome.storage key per segment), just backed by Room/SQLite instead of the
 * filesystem or an extension storage API, because Room is what's idiomatic here.
 */
@Entity(tableName = "pending_events")
data class PendingEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val appIdentifier: String?,
    val appDisplayName: String?,
    val startedAt: String,
    val endedAt: String?,
    val durationSeconds: Int?,
    val isCharging: Boolean?,
    val systemEventType: String?,
    val sessionStart: Boolean?,
    val wifiBytes: Long? = 0L,
    val cellularBytes: Long? = 0L,
    val hotspotBytes: Long? = 0L,
    val totalBytes: Long? = 0L,
)
