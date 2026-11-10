package com.yurak.agent.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Serializable
data class GitHubReleaseAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long? = null,
)

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    val assets: List<GitHubReleaseAsset> = emptyList(),
)

data class UpdateInfo(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val releasePageUrl: String,
)

object AppUpdateChecker {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkUpdate(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/yurak-app/android/releases/latest")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Yurak-Android-App")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val release = json.decodeFromString<GitHubReleaseDto>(body)

            val latestTag = release.tagName.removePrefix("v").trim()
            val cleanCurrent = currentVersion.removePrefix("v").trim()

            val hasUpdate = isVersionNewer(cleanCurrent, latestTag)
            val apkAsset = release.assets.find { it.name.endsWith(".apk") }
            val downloadUrl = apkAsset?.browserDownloadUrl ?: release.htmlUrl

            UpdateInfo(
                hasUpdate = hasUpdate,
                currentVersion = cleanCurrent,
                latestVersion = latestTag,
                releaseTitle = release.name ?: "Yurak v$latestTag",
                releaseNotes = release.body ?: "Yangi versiya yaxshilanishlari",
                downloadUrl = downloadUrl,
                releasePageUrl = release.htmlUrl,
            )
        } catch (e: Exception) {
            null
        }
    }

    fun isVersionNewer(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until length) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
