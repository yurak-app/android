package com.yurak.agent.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/**
 * Universal date and time utility operating strictly in UTC+5 (Asia/Tashkent).
 */
object TashkentTime {
    val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

    fun now(): ZonedDateTime = ZonedDateTime.now(ZONE)

    fun today(): LocalDate = LocalDate.now(ZONE)

    fun startOfDayMs(date: LocalDate): Long =
        date.atStartOfDay(ZONE).toInstant().toEpochMilli()

    fun endOfDayMs(date: LocalDate): Long =
        date.atTime(LocalTime.MAX).atZone(ZONE).toInstant().toEpochMilli()

    /**
     * Returns the 7 days of the week (Sunday to Saturday) matching the mockup layout:
     * Ya (Sun), Du (Mon), Se (Tue), Cho (Wed), Pay (Thu), Ju (Fri), Sha (Sat).
     */
    fun getWeekDays(anchorDate: LocalDate): List<LocalDate> {
        val sunday = anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        return (0..6).map { sunday.plusDays(it.toLong()) }
    }

    /**
     * Short day label for the chart in Uzbek:
     * Sunday -> Ya, Monday -> Du, Tuesday -> Se, etc.
     */
    fun shortDayLabel(date: LocalDate): String = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> "Ya"
        DayOfWeek.MONDAY -> "Du"
        DayOfWeek.TUESDAY -> "Se"
        DayOfWeek.WEDNESDAY -> "Cho"
        DayOfWeek.THURSDAY -> "Pay"
        DayOfWeek.FRIDAY -> "Ju"
        DayOfWeek.SATURDAY -> "Sha"
    }

    /**
     * Full day name in Uzbek.
     */
    fun fullDayName(date: LocalDate): String = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> "Yakshanba"
        DayOfWeek.MONDAY -> "Dushanba"
        DayOfWeek.TUESDAY -> "Seshanba"
        DayOfWeek.WEDNESDAY -> "Chorshanba"
        DayOfWeek.THURSDAY -> "Payshanba"
        DayOfWeek.FRIDAY -> "Juma"
        DayOfWeek.SATURDAY -> "Shanba"
    }

    /**
     * Short month name in Uzbek.
     */
    fun shortMonthName(month: Int): String = when (month) {
        1 -> "Yan"
        2 -> "Fev"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Iyun"
        7 -> "Iyul"
        8 -> "Avg"
        9 -> "Sen"
        10 -> "Okt"
        11 -> "Noy"
        12 -> "Dek"
        else -> ""
    }

    /**
     * Full month name in Uzbek.
     */
    fun fullMonthName(month: Int): String = when (month) {
        1 -> "yanvar"
        2 -> "fevral"
        3 -> "mart"
        4 -> "aprel"
        5 -> "may"
        6 -> "iyun"
        7 -> "iyul"
        8 -> "avgust"
        9 -> "sentabr"
        10 -> "oktabr"
        11 -> "noyabr"
        12 -> "dekabr"
        else -> ""
    }

    /**
     * Formats week range, e.g. "22 Avg - 28 Avg"
     */
    fun formatWeekRange(start: LocalDate, end: LocalDate): String {
        return "${start.dayOfMonth} ${shortMonthName(start.monthValue)} - ${end.dayOfMonth} ${shortMonthName(end.monthValue)}"
    }

    /**
     * Formats selected day text, e.g. "Payshanba (26-avgust)"
     */
    fun formatSelectedDay(date: LocalDate): String {
        return "${fullDayName(date)} (${date.dayOfMonth}-${fullMonthName(date.monthValue)})"
    }

    /**
     * Formats duration in seconds into human-readable Uzbek string:
     * e.g. 19200s -> "5s 20d", 2400s -> "40 daqiqa", 45s -> "45 soniya"
     */
    fun formatDuration(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "0 daqiqa"
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}s ${minutes}d"
            hours > 0 -> "${hours} soat"
            minutes > 0 -> "${minutes} daqiqa"
            else -> "${totalSeconds} soniya"
        }
    }
}
