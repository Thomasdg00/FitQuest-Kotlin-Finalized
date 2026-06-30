package com.univpm.fitquest.ui.resources

import androidx.annotation.StringRes
import com.univpm.fitquest.R

@StringRes
fun weatherCodeToLabelRes(code: Int?): Int {
    return when (code) {
        0 -> R.string.weather_condition_clear_sky
        1 -> R.string.weather_condition_mainly_clear
        2 -> R.string.weather_condition_partly_cloudy
        3 -> R.string.weather_condition_overcast
        45, 48 -> R.string.weather_condition_fog
        51, 53, 55 -> R.string.weather_condition_drizzle
        61, 63, 65 -> R.string.weather_condition_rain
        71, 73, 75 -> R.string.weather_condition_snow
        80, 81, 82 -> R.string.weather_condition_showers
        95, 96, 99 -> R.string.weather_condition_thunderstorm
        else -> R.string.weather_condition_unknown
    }
}
