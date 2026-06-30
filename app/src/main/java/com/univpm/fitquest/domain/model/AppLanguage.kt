package com.univpm.fitquest.domain.model

enum class AppLanguage(val code: String) {
    English("en"),
    Italian("it");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: English
    }
}
