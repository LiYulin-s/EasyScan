package io.github.liyulin.easyscan

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun getDataStorePath(): String

@Composable
expect fun BackHandler(onBack: () -> Unit)
expect fun shareLink(link: String)