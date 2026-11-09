package com.yurak.agent.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit

/** What a call actually resolved to — the same tri-state every client in this project
 * uses (see the Windows agent's ApiClient.PostEventsAsync doc comment, and the
 * extension's api.ts PostEventsResult): a rejected token needs the caller to log out;
 * anything else that failed should just be retried later, nothing's lost either way
 * since callers only delete their local copy after a confirmed Ok. */
sealed class ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>()
    data object Unauthorized : ApiResult<Nothing>()
    data class Failure(val message: String) : ApiResult<Nothing>()
}

class ApiException(message: String) : Exception(message)

private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

/**
 * Thin wrapper around the raw Retrofit [ApiService]: adds the Bearer header, classifies
 * failures into [ApiResult], and can be repointed at a different backend URL (Settings)
 * without recreating the whole app — same idea as the Windows agent's
 * ApiClient.SetBaseUrl and the extension's storage.getConfig().backendUrl.
 */
class ApiClient(baseUrl: String) {
    private var service: ApiService = buildService(baseUrl)

    fun setBaseUrl(baseUrl: String) {
        service = buildService(baseUrl)
    }

    /** Throws [ApiException] on any non-2xx response or network failure — the login
     * screen is expected to catch this and show the message, same as every other
     * client's login call. */
    suspend fun login(email: String, password: String, deviceName: String): LoginResponseDto {
        val response = try {
            service.login(LoginRequestDto(email = email, password = password, device = LoginDeviceDto(platform = "android", name = deviceName)))
        } catch (e: Exception) {
            throw ApiException("Couldn't reach the server. Check your connection and try again.")
        }
        if (!response.isSuccessful) throw ApiException(readError(response))
        return response.body() ?: throw ApiException("Empty response from server")
    }

    suspend fun postEvents(apiToken: String, events: List<EventDto>): ApiResult<IngestEventsResponseDto> =
        safeCall { service.postEvents(bearer(apiToken), IngestEventsRequestDto(events)) }

    suspend fun putBattery(apiToken: String, level: Int, charging: Boolean): ApiResult<Unit> =
        safeCall { service.putBattery(bearer(apiToken), BatteryStatusDto(level, charging)) }

    suspend fun putTimezone(apiToken: String, offsetMinutes: Int): ApiResult<Unit> =
        safeCall { service.putTimezone(bearer(apiToken), TimezoneDto(offsetMinutes)) }

    suspend fun getDailyStats(apiToken: String, date: String? = null, deviceId: String? = null): ApiResult<DailyStatsResponseDto> =
        safeCall { service.getDailyStats(bearer(apiToken), date, deviceId) }

    suspend fun getRangeStats(apiToken: String, start: String, end: String, deviceId: String? = null): ApiResult<RangeStatsResponseDto> =
        safeCall { service.getRangeStats(bearer(apiToken), start, end, deviceId) }

    suspend fun getDevices(apiToken: String): ApiResult<DevicesResponseDto> =
        safeCall { service.getDevices(bearer(apiToken)) }

    suspend fun getMe(apiToken: String): ApiResult<UserProfileResponseDto> =
        safeCall { service.getMe(bearer(apiToken)) }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): ApiResult<T> {
        val response = try {
            call()
        } catch (e: Exception) {
            android.util.Log.e("ApiClient", "safeCall exception: ${e.message}", e)
            return ApiResult.Failure(e.message ?: "Network error")
        }
        android.util.Log.i("ApiClient", "safeCall code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
        return when {
            response.code() == 401 -> ApiResult.Unauthorized
            response.isSuccessful -> {
                @Suppress("UNCHECKED_CAST")
                val value = response.body() ?: (Unit as T)
                ApiResult.Ok(value)
            }
            else -> {
                val err = readError(response)
                android.util.Log.e("ApiClient", "safeCall error body: $err")
                ApiResult.Failure(err)
            }
        }
    }

    private fun bearer(apiToken: String) = "Bearer $apiToken"

    private fun readError(response: Response<*>): String {
        val raw = response.errorBody()?.string()
        val parsed = raw?.let { runCatching { json.decodeFromString(ErrorDto.serializer(), it) }.getOrNull() }
        return parsed?.error ?: "Request failed (${response.code()})"
    }

    private fun buildService(baseUrl: String): ApiService {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val http = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .build()
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(http)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ApiService::class.java)
    }
}
