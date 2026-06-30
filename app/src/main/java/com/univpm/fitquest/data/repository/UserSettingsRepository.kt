package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.local.dao.UserSettingsDao
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

class UserSettingsRepository(
    private val userSettingsDao: UserSettingsDao
) {
    fun observeSettings(): Flow<UserSettingsEntity?> =
        userSettingsDao.observe()

    suspend fun getSettingsOrNull(): UserSettingsEntity? =
        userSettingsDao.get()

    suspend fun getSettings(): UserSettingsEntity =
        userSettingsDao.get() ?: UserSettingsEntity()

    suspend fun saveSettings(settings: UserSettingsEntity) =
        userSettingsDao.upsert(settings)
}
