package com.pmv.agendaws.utils

import android.content.Context
import android.provider.Settings

object Device {
    @JvmStatic
    fun getSecureId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
