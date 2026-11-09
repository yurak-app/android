package com.yurak.agent.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the Windows agent's Models/Dtos.cs and the extension's src/types.ts — same
// backend contract, three clients. Keep in sync with backend/src/modules/*/**.schemas.ts.

@Serializable
data class LoginDeviceDto(
    val platform: String = "android",
    val name: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
    val device: LoginDeviceDto,
)

@Serializable
data class LoginUserDto(
    val id: String,
    val email: String,
)

@Serializable
data class LoginResponseDto(
    val apiToken: String,
    val deviceId: String,
    val user: LoginUserDto,
)

/** One tracked segment or discrete event. `startedAt`/`endedAt` are ISO-8601 instants
 * (java.time.Instant.toString()) — the backend's Zod schema coerces these with `new Date()`,
 * same as every other client. */
@Serializable
data class EventDto(
    val eventType: String, // "app_usage" | "idle" | "system"
    val appIdentifier: String? = null,
    val appDisplayName: String? = null,
    val domain: String? = null,
    val startedAt: String,
    val endedAt: String? = null,
    val durationSeconds: Int? = null,
    val isCharging: Boolean? = null,
    val systemEventType: String? = null, // "lock" | "unlock" — only on eventType "system"
    val sessionStart: Boolean? = null, // only on eventType "app_usage"
    val wifiBytes: Long? = null,
    val cellularBytes: Long? = null,
    val hotspotBytes: Long? = null,
    val totalBytes: Long? = null,
)

@Serializable
data class IngestEventsRequestDto(
    val events: List<EventDto>,
)

@Serializable
data class IngestEventsResponseDto(
    val inserted: Int,
    val duplicates: Int = 0,
)

@Serializable
data class BatteryStatusDto(
    val level: Int,
    val charging: Boolean,
)

@Serializable
data class TimezoneDto(
    val offsetMinutes: Int,
)

@Serializable
data class ErrorDto(
    val error: String,
)

// ---- Stats & Dashboard DTOs ----

@Serializable
data class DailyAppStatDto(
    val appId: String? = null,
    val identifier: String,
    val displayName: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val platform: String? = null,
    val totalSeconds: Int = 0,
    val sessionCount: Int = 0,
    val wifiBytes: Long = 0,
    val cellularBytes: Long = 0,
    val hotspotBytes: Long = 0,
    val totalBytes: Long = 0,
)

@Serializable
data class DailyStatsResponseDto(
    val date: String,
    val apps: List<DailyAppStatDto> = emptyList(),
    val totalWifiBytes: Long = 0,
    val totalCellularBytes: Long = 0,
    val totalHotspotBytes: Long = 0,
    val totalBytes: Long = 0,
    val screenSeconds: Int? = null,
    val idleSeconds: Int? = null,
    val switchCount: Int? = null,
    val sessionCount: Int? = null,
)

@Serializable
data class DayAggregateDto(
    val date: String,
    val totalSeconds: Int = 0,
    val wifiBytes: Long = 0,
    val cellularBytes: Long = 0,
    val hotspotBytes: Long = 0,
    val totalBytes: Long = 0,
)

@Serializable
data class RangeStatsResponseDto(
    val start: String,
    val end: String,
    val daily: List<DayAggregateDto> = emptyList(),
    val topApps: List<DailyAppStatDto> = emptyList(),
)

@Serializable
data class DeviceItemDto(
    val id: String,
    val name: String,
    val platform: String,
    val batteryLevel: Int? = null,
    val isCharging: Boolean? = null,
    val lastSeenAt: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class DevicesResponseDto(
    val devices: List<DeviceItemDto> = emptyList(),
)

@Serializable
data class UserProfileDto(
    val id: String,
    val email: String,
    val name: String? = null,
)

@Serializable
data class UserProfileResponseDto(
    val user: UserProfileDto,
)

