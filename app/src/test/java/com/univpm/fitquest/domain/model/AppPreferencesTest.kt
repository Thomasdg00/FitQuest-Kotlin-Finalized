package com.univpm.fitquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPreferencesTest {
    @Test
    fun languageFallsBackToEnglishForUnknownStorageValue() {
        assertEquals(AppLanguage.English, AppLanguage.fromCode("unknown"))
    }

    @Test
    fun themeModeFallsBackToSystemForUnknownStorageValue() {
        assertEquals(ThemeMode.System, ThemeMode.fromStorageValue("unknown"))
    }
}
