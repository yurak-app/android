package com.yurak.agent.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/** Raw Retrofit endpoint definitions — see ApiClient for the wrapper every caller
 * actually uses (auth headers, error handling, retry-vs-give-up classification). */
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @POST("events")
    suspend fun postEvents(
        @Header("Authorization") auth: String,
        @Body body: IngestEventsRequestDto,
    ): Response<IngestEventsResponseDto>

    @PUT("devices/me/battery")
    suspend fun putBattery(
        @Header("Authorization") auth: String,
        @Body body: BatteryStatusDto,
    ): Response<Unit>

    @PUT("devices/me/timezone")
    suspend fun putTimezone(
        @Header("Authorization") auth: String,
        @Body body: TimezoneDto,
    ): Response<Unit>

    @GET("stats/daily")
    suspend fun getDailyStats(
        @Header("Authorization") auth: String,
        @Query("date") date: String? = null,
        @Query("deviceId") deviceId: String? = null,
    ): Response<DailyStatsResponseDto>

    @GET("stats/range")
    suspend fun getRangeStats(
        @Header("Authorization") auth: String,
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("deviceId") deviceId: String? = null,
    ): Response<RangeStatsResponseDto>

    @GET("devices")
    suspend fun getDevices(
        @Header("Authorization") auth: String,
    ): Response<DevicesResponseDto>

    @GET("auth/me")
    suspend fun getMe(
        @Header("Authorization") auth: String,
    ): Response<UserProfileResponseDto>
}
