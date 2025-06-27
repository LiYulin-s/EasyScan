package io.github.liyulin.easyscan.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.liyulin.easyscan.BackHandler
import io.github.liyulin.easyscan.domain.updateScan
import io.github.liyulin.easyscan.domain.NetworkResult
import io.github.liyulin.easyscan.ui.components.SimpleErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.qrcode.CodeType
import org.publicvalue.multiplatform.qrcode.ScannerWithPermissions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    key: String,
    scanName: String = "扫描任务",
    onBack: () -> Unit = {},
) {
    var code by remember { mutableStateOf("") }
    var scannedCodes by remember { mutableStateOf(listOf<String>()) }
    var updateError by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        onBack()
    }

    LaunchedEffect(code) {
        if (code.isNotEmpty()) {
            scope.launch {
                isUpdating = true
                updateError = null

                when (val result = updateScan(scanName, key, code)) {
                    is NetworkResult.Success -> {
                        if (result.data && code != scannedCodes.lastOrNull()) {
                            scannedCodes = scannedCodes + code
                        }
                    }
                    is NetworkResult.Error -> {
                        updateError = "扫描数据上传失败: ${result.exception.message}"
                    }
                    is NetworkResult.Loading -> {
                        // 已经在isUpdating中处理了
                    }
                }
                isUpdating = false
            }
        }
    }

    Box(
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
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = scanName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "已扫描 ${scannedCodes.size} 个二维码",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                if (isUpdating) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 网络错误提示
                updateError?.let { error ->
                    item {
                        SimpleErrorMessage(
                            message = error,
                            onDismiss = { updateError = null }
                        )
                    }
                }

                item {
                    // 扫描器卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "扫描图标",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "对准二维码进行扫描",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            ScannerWithPermissions(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(MaterialTheme.shapes.large),
                                onScanned = { scannedCode ->
                                    code = scannedCode
                                    false
                                },
                                types = listOf(CodeType.QR),
                            )
                        }
                    }
                }

                // 扫描结果展示
                if (scannedCodes.isNotEmpty()) {
                    item {
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
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "成功图标",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "扫描结果",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                scannedCodes.forEach { scannedCode ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text(
                                            text = scannedCode,
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        // 空状态提示
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "扫描提示",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "尚未扫描任何二维码",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "将摄像头对准二维码即可开始扫描",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}