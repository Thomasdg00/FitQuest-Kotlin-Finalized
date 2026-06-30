package com.univpm.fitquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.univpm.fitquest.domain.model.AppLanguage
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.ThemeMode

/**
 * Single-row app settings table. Keep id = 1 for the current user.
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = DEFAULT_ID,
    val useMetricUnits: Boolean = true,
    val preferredSport: String = Sport.Walking.routeValue,
    val name: String = "",
    val surname: String = "",
    val heightCm: Int = 0,
    val bodyWeightKg: Double = 70.0,
    val ageYears: Int = 0,
    val sex: String = "",
    val weeklyGoalReminderEnabled: Boolean = true,
    val languageCode: String = AppLanguage.English.code,
    val themeMode: String = ThemeMode.System.storageValue,
) {
    companion object {
        const val DEFAULT_ID = 1
    }
}
