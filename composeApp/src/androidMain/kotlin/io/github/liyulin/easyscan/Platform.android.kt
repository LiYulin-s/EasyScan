package io.github.liyulin.easyscan

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()


actual fun getDataStorePath(): String {
    val context: Context = ContextHandler.currentContext!!
    return context.filesDir.resolve("easyscan_settings.preferences_pb").absolutePath
}

@Composable
actual fun BackHandler(onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)
}