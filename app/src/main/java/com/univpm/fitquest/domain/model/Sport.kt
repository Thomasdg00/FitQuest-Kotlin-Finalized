package com.univpm.fitquest.domain.model

enum class Sport(val routeValue: String, val displayName: String) {
    Walking("walking", "Walking"),
    Running("running", "Running"),
    Cycling("cycling", "Cycling");

    companion object {
        fun fromRouteValue(value: String?): Sport =
            entries.firstOrNull { it.routeValue == value } ?: Walking
    }
}
