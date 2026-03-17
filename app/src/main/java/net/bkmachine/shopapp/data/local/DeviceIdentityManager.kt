package net.bkmachine.shopapp.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class DeviceIdentityManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrCreateDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId!!
    }

    fun getSavedDisplayName(): String? {
        return prefs.getString(KEY_DISPLAY_NAME, null)
    }

    fun saveDisplayName(displayName: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, displayName).apply()
    }

    companion object {
        private const val PREFS_NAME = "device_identity_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DISPLAY_NAME = "display_name"
    }
}
