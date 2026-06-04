package com.android.xrayfa

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.appcompat.app.AppCompatDelegate
import com.android.xrayfa.XrayAppCompatFactory.Companion.TAG
import com.android.xrayfa.XrayAppCompatFactory.Companion.xrayPATH
import com.android.xrayfa.common.GEO_IP
import com.android.xrayfa.common.GEO_SITE
import com.android.xrayfa.common.repository.LanguageMode
import com.android.xrayfa.common.repository.SettingsKeys
import com.android.xrayfa.common.repository.Theme
import com.android.xrayfa.common.repository.dataStore
import com.android.xrayfa.common.utils.SocksConfigGenerator
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class XrayFAApplication: Application() {

    private val _isDarkTheme = MutableStateFlow(Theme.DARK_MODE)
    val isDarkTheme: StateFlow<Int> get() = _isDarkTheme

    var contextAvailableCallback: ContextAvailableCallback? = null

    private val appCoroutineScope = CoroutineScope(Dispatchers.IO)

    private fun observeDarkMode() {
        appCoroutineScope.launch {
            dataStore.data
                .map { prefs ->
                    prefs[SettingsKeys.DARK_MODE] ?: Theme.DARK_MODE
                }
                .collect { value ->
                    _isDarkTheme.value = value
                }
        }
    }

    private fun observeLanguageMode() {
        appCoroutineScope.launch {
            dataStore.data
                .map { prefs -> prefs[SettingsKeys.LANGUAGE_MODE] ?: LanguageMode.AUTO }
                .collect { mode ->
                    LocaleHelper.saveMode(applicationContext, mode)
                    val locales = LocaleListCompat.forLanguageTags(LocaleHelper.resolveLanguageTag(mode))
                    withContext(Dispatchers.Main) {
                        AppCompatDelegate.setApplicationLocales(locales)
                    }
                }
        }
    }

    override fun onCreate() {
        super.onCreate()
        contextAvailableCallback?.onContextAvailable(applicationContext)
        observeDarkMode()
        observeLanguageMode()
        initXrayFile()
        initSocksConfig()
    }

    private fun initXrayFile() {
        appCoroutineScope.launch {
            //init file
            val fileDir = filesDir
            val geoipFile = File(fileDir, GEO_IP)
            val geositeFile = File(fileDir, GEO_SITE)
            xrayPATH = filesDir.absolutePath
            if (!geoipFile.exists()) {
                Log.i(TAG, "copy geoip.dat")
                assets.open(GEO_IP).use { input ->
                    FileOutputStream(geoipFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            if (!geositeFile.exists()) {
                assets.open(GEO_SITE).use { input ->
                    FileOutputStream(geositeFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private fun initSocksConfig() {
        appCoroutineScope.launch {
            dataStore.edit {
                val port = it[SettingsKeys.SOCKS_PORT]
                if (port == null || port !in SocksConfigGenerator.portRange) {
                    it[SettingsKeys.SOCKS_PORT] = SocksConfigGenerator.generatePort()
                }
                val password = it[SettingsKeys.SOCKS_PASSWORD]
                if (password == null || password.isBlank()) {
                    it[SettingsKeys.SOCKS_PASSWORD] = SocksConfigGenerator.generatePassword()
                }
                val username = it[SettingsKeys.SOCKS_USERNAME]
                if (username == null || username.isBlank()) {
                    it[SettingsKeys.SOCKS_USERNAME] = SocksConfigGenerator.generateUsername()
                }
            }
        }
    }
}
