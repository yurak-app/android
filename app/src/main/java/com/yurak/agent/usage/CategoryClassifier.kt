package com.yurak.agent.usage

import androidx.compose.ui.graphics.Color

object CategoryClassifier {

    fun classify(packageName: String, appName: String): AppCategoryBadge {
        val lower = (packageName + " " + appName).lowercase()
        return when {
            lower.contains("zoom") || lower.contains("meet") || lower.contains("classroom") ->
                AppCategoryBadge("Ta'lim", Color(0xFFF3E8FF), Color(0xFF7E22CE))

            lower.contains("duolingo") || lower.contains("anki") || lower.contains("coursera") || lower.contains("edx") ->
                AppCategoryBadge("O'rganish", Color(0xFFDCFCE7), Color(0xFF15803D))

            lower.contains("telegram") || lower.contains("whatsapp") || lower.contains("viber") || lower.contains("messenger") ->
                AppCategoryBadge("Cheklanmagan", Color(0xFFF3E8FF), Color(0xFF7C3AED))

            lower.contains("instagram") || lower.contains("tiktok") || lower.contains("facebook") || lower.contains("twitter") || lower.contains("threads") ->
                AppCategoryBadge("Cheklovga yaqin", Color(0xFFFFEDD5), Color(0xFFC2410C))

            lower.contains("youtube") || lower.contains("netflix") || lower.contains("spotify") || lower.contains("music") ->
                AppCategoryBadge("Cheklangan", Color(0xFFFFE4E6), Color(0xFFE11D48))

            lower.contains("chrome") || lower.contains("browser") || lower.contains("firefox") || lower.contains("opera") ->
                AppCategoryBadge("Brauzer", Color(0xFFE0F2FE), Color(0xFF0369A1))

            lower.contains("game") || lower.contains("pubg") || lower.contains("brawl") || lower.contains("chess") ->
                AppCategoryBadge("O'yin", Color(0xFFFEF3C7), Color(0xFFB45309))

            else ->
                AppCategoryBadge("Ilova", Color(0xFFF1F5F9), Color(0xFF475569))
        }
    }
}

