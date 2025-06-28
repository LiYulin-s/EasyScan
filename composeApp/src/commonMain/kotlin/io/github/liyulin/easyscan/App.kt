package io.github.liyulin.easyscan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import org.jetbrains.compose.ui.tooling.preview.Preview

import io.github.liyulin.easyscan.ui.navigation.AppNavigation
import io.github.liyulin.easyscan.ui.navigation.NavigationState
import io.github.liyulin.easyscan.ui.viewmodel.HistoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.liyulin.easyscan.ui.theme.EasyScanTheme

@Composable
@Preview
fun App() {
    val historyViewModel = viewModel { HistoryViewModel() }
    val navigationState = remember { NavigationState() }

    EasyScanTheme {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Scaffold(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ) { paddingValues ->
                    AppNavigation(
                        navigationState = navigationState,
                        historyViewModel = historyViewModel
                    )
                }
            }
        }
    }
}