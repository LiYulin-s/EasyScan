package io.github.liyulin.easyscan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.liyulin.easyscan.ui.screen.BeforeScanScreen
import io.github.liyulin.easyscan.ui.screen.ScanScreen
import io.github.liyulin.easyscan.ui.viewmodel.HistoryViewModel

sealed class Screen {
    object Home : Screen()
    data class Scan(val key: String, val scanName: String) : Screen()
}

class NavigationState {
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    fun navigateToScan(key: String, scanName: String) {
        currentScreen = Screen.Scan(key, scanName)
    }

    fun navigateBack() {
        currentScreen = Screen.Home
    }
}

@Composable
fun AppNavigation(
    navigationState: NavigationState,
    historyViewModel: HistoryViewModel
) {
    when (val screen = navigationState.currentScreen) {
        is Screen.Home -> {
            BeforeScanScreen(
                historyViewModel = historyViewModel,
                onStartScan = { key, scanName ->
                    navigationState.navigateToScan(key, scanName)
                }
            )
        }
        is Screen.Scan -> {
            ScanScreen(
                key = screen.key,
                scanName = screen.scanName,
                onBack = {
                    navigationState.navigateBack()
                }
            )
        }
    }
}
