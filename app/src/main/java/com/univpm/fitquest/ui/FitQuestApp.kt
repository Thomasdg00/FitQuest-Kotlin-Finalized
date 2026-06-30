package com.univpm.fitquest.ui

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import com.univpm.fitquest.di.AppContainer
import com.univpm.fitquest.domain.model.AppLanguage
import com.univpm.fitquest.domain.model.ThemeMode
import com.univpm.fitquest.ui.navigation.FitQuestNavHost
import com.univpm.fitquest.ui.theme.FitQuestTheme
import java.util.Locale

@Composable
fun FitQuestApp(appContainer: AppContainer) {
    val settings by appContainer.userSettingsRepository.observeSettings().collectAsState(initial = null)
    val resolvedSettings = settings ?: UserSettingsEntity()
    val appLanguage = AppLanguage.fromCode(resolvedSettings.languageCode)
    val themeMode = ThemeMode.fromStorageValue(resolvedSettings.themeMode)
    val baseContext = LocalContext.current
    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val localizedContext = remember(baseContext, appLanguage) {
        baseContext.withAppLanguage(appLanguage)
    }

    SideEffect {
        Locale.setDefault(Locale.forLanguageTag(appLanguage.code))
    }

    FitQuestTheme(themeMode = themeMode) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalActivityResultRegistryOwner provides checkNotNull(activityResultRegistryOwner) {
                "FitQuestApp must be hosted by an ActivityResultRegistryOwner"
            },
            LocalLifecycleOwner provides lifecycleOwner,
            LocalSavedStateRegistryOwner provides savedStateRegistryOwner,
        ) {
            FitQuestNavHost(appContainer = appContainer)
        }
    }
}

private fun Context.withAppLanguage(appLanguage: AppLanguage): Context {
    val locale = Locale.forLanguageTag(appLanguage.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocales(LocaleList(locale))
    return createConfigurationContext(configuration)
}
