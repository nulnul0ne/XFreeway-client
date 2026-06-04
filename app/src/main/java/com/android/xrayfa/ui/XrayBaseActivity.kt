package com.android.xrayfa.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalConfiguration
import com.android.xrayfa.XrayFAApplication
import com.android.xrayfa.LocaleHelper
import com.android.xrayfa.common.repository.Theme
import com.android.xrayfa.ui.theme.V2rayForAndroidUITheme

abstract class XrayBaseActivity: ComponentActivity(){

    @Composable
    abstract fun Content(isLandscape: Boolean)

    override fun attachBaseContext(newBase: Context) {
        val localizedContext = LocaleHelper.wrap(newBase)
        val fixedConfig = Configuration(localizedContext.resources.configuration).apply {
            fontScale = 0.85f
        }
        super.attachBaseContext(localizedContext.createConfigurationContext(fixedConfig))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as XrayFAApplication
        setContent {
            val darkTheme = true

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            V2rayForAndroidUITheme(
                darkTheme = darkTheme
            ) {
                Content(false)
            }
        }
    }

}

@Deprecated("use scene instead")
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ShowContentWithOrientation(
    content: @Composable (isLandscape: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    content(isLandscape)
}
