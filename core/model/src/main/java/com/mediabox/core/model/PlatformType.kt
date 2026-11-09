package com.mediabox.core.model

enum class PlatformType(val displayName: String) {
    YOUTUBE("YouTube"),
    TIKTOK("TikTok"),
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook"),
    TWITTER("X (Twitter)"),
    TELEGRAM("Telegram"),
    SNAPCHAT("Snapchat"),
    GENERIC("منصة عامة");

    companion object {
        fun detectFromUrl(url: String): PlatformType {
            val lower = url.lowercase()
            return when {
                lower.contains("youtube.com") || lower.contains("youtu.be") -> YOUTUBE
                lower.contains("tiktok.com") -> TIKTOK
                lower.contains("instagram.com") -> INSTAGRAM
                lower.contains("facebook.com") || lower.contains("fb.watch") -> FACEBOOK
                lower.contains("twitter.com") || lower.contains("x.com") -> TWITTER
                lower.contains("t.me") || lower.contains("telegram.org") -> TELEGRAM
                lower.contains("snapchat.com") -> SNAPCHAT
                else -> GENERIC
            }
        }
    }
}
