package io.github.Richard102257.whatsnew

import android.content.Context
import android.content.SharedPreferences

private lateinit var applicationContext: Context

fun initKuiklyWhatsNew(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getCurrentVersion(): WhatsNewVersion {
    return try {
        val packageInfo = applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName, 0
        )
        WhatsNewVersion.parse(packageInfo.versionName ?: "1.0.0")
    } catch (e: Exception) {
        WhatsNewVersion(1, 0, 0)
    }
}

actual fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore {
    val prefs = applicationContext.getSharedPreferences(
        "kuikly_whatsnew_store", Context.MODE_PRIVATE
    )
    return AndroidWhatsNewVersionStore(prefs)
}

class AndroidWhatsNewVersionStore(
    private val prefs: SharedPreferences
) : WhatsNewVersionStore {

    override fun savePresentedVersion(version: String) {
        val key = "presented_$version"
        prefs.edit().putBoolean(key, true).apply()
        val allKey = "presented_all"
        val current = prefs.getString(allKey, "") ?: ""
        val updated = if (current.isEmpty()) version else "$current,$version"
        prefs.edit().putString(allKey, updated).apply()
    }

    override fun getPresentedVersions(): Set<String> {
        val stored = prefs.getString("presented_all", "") ?: ""
        if (stored.isEmpty()) return emptySet()
        return stored.split(",").toSet()
    }

    override fun hasPresented(version: String): Boolean {
        return prefs.getBoolean("presented_$version", false)
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
