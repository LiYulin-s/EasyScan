package io.github.liyulin.easyscan.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.liyulin.easyscan.domain.NetworkResult
import io.github.liyulin.easyscan.domain.newScan
import io.github.liyulin.easyscan.ui.components.SimpleErrorMessage
import io.github.liyulin.easyscan.ui.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch

/**
 * 扫描前的屏幕显示。
 * 包含一个输入框用于输入名称，一个"扫描"按钮，并显示扫描历史记录。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeforeScanScreen(
    historyViewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
    onStartScan: (key: String, scanName: String) -> Unit = { _, _ -> }
) {
    var scanName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val historyList: State<Collection<HistoryViewModel.HistoryItem>> =
        historyViewModel.history.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // 创建扫描任务的函数
    fun createScanTask() {
        if (scanName.isNotBlank() && !isLoading) {
            scope.launch {
                isLoading = true
                errorMessage = null

                when (val result = newScan(scanName)) {
                    is NetworkResult.Success -> {
                        historyViewModel.saveHistory(HistoryViewModel.HistoryItem(result.data, scanName))
                        onStartScan(result.data, scanName)
                        scanName = ""
                    }

                    is NetworkResult.Error -> {
                        errorMessage = result.exception.message
                    }

                    is NetworkResult.Loading -> {
                        // 这种情况不应该发生，因为我们已经在函数内部处理了
                    }
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 标题部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "扫描图标",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "EasyScan",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "轻松扫描，轻松生活",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                // 输入和扫描部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "新建扫描",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // 输入框，用于输入扫描名称
                        OutlinedTextField(
                            value = scanName,
                            onValueChange = {
                                scanName = it
                                errorMessage = null // 清除错误信息
                            },
                            label = { Text("输入扫描名称") },
                            placeholder = { Text("例: 潮流金属导论") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索图标"
                                )
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { createScanTask() }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 错误信息显示
                        errorMessage?.let { message ->
                            SimpleErrorMessage(
                                message = message,
                                onDismiss = { errorMessage = null }
                            )
                        }

                        // "扫描"按钮
                        Button(
                            onClick = { createScanTask() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = scanName.isNotBlank() && !isLoading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "扫描图标",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isLoading) "创建中..." else "开始扫描",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                // 历史记录部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "历史图标",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "扫描历史",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        historyViewModel.clearHistory()
                                    }
                                },
                                enabled = historyList.value.isNotEmpty()
                            ) {
                                Text("清除历史")
                            }
                        }
                        History(
                            history = historyList.value,
                            onHistoryItemClick = { historyItem ->
                                onStartScan(historyItem.key, historyItem.name)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 显示历史记录项的 FlowRow 布局。
 * @param history 历史记录项的列表。
 */
@Composable
fun History(
    history: Collection<HistoryViewModel.HistoryItem>,
    onHistoryItemClick: (HistoryViewModel.HistoryItem) -> Unit = {}
) {
    if (history.isEmpty()) {
        // 空状态显示
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "无历史记录",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "暂无扫描记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "开始您的第一次扫描吧！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            history.forEach { historyItem ->
                ElevatedButton(
                    onClick = { onHistoryItemClick(historyItem) },
                    modifier = Modifier,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 1.dp,
                        pressedElevation = 3.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "扫描记录",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = historyItem.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
