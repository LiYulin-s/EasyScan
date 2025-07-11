package io.github.liyulin.easyscan

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

actual fun getDataStorePath(): String {
    val currentDir = System.getProperty("user.dir")
    return File(currentDir, "easyscan_settings.preferences_pb").absolutePath
}

@Composable
actual fun BackHandler(onBack: () -> Unit) {
}

actual fun shareLink(link: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(link), null)
}