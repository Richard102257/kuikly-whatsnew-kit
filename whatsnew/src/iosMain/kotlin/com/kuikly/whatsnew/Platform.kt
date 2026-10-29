package com.kuikly.whatsnew

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual fun getCurrentVersion(): WhatsNewVersion {
    return try {
        val versionString = NSBundle.mainBundle.infoDictionary
            ?.get("CFBundleShortVersionString") as? String ?: "1.0.0"
        WhatsNewVersion.parse(versionString)
    } catch (e: Exception) {
        WhatsNewVersion(1, 0, 0)
    }
}

actual fun createPersistentWhatsNewVersionStore(): WhatsNewVersionStore {
    return IOSWhatsNewVersionStore(NSUserDefaults.standardUserDefaults)
}

class IOSWhatsNewVersionStore(
    private val defaults: NSUserDefaults
) : WhatsNewVersionStore {

    override fun savePresentedVersion(version: String) {
        defaults.setBool(true, "presented_$version")
        val current = defaults.stringForKey("presented_all") ?: ""
        val updated = if (current.isEmpty()) version else "$current,$version"
        defaults.setObject(updated, "presented_all")
    }

    override fun getPresentedVersions(): Set<String> {
        val stored = defaults.stringForKey("presented_all") ?: ""
        if (stored.isEmpty()) return emptySet()
        return stored.split(",").toSet()
    }

    override fun hasPresented(version: String): Boolean {
        return if (defaults.objectForKey("presented_$version") != null) {
            defaults.boolForKey("presented_$version")
        } else false
    }

    override fun clear() {
        val domain = NSUserDefaults.standardUserDefaults.volatileDomainForName("kuikly_whatsnew")
        defaults.removePersistentDomainForName("kuikly_whatsnew")
    }
}
