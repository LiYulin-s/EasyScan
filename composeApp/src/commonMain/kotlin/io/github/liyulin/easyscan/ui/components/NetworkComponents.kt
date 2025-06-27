package io.github.liyulin.easyscan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.liyulin.easyscan.domain.NetworkException

/**
 * 加载状态指示器
 */
@Composable
fun LoadingIndicator(
    message: String = "正在处理...",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 错误显示组件
 */
@Composable
fun ErrorCard(
    exception: NetworkException,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = when (exception) {
        is NetworkException.NetworkConnectionException ->
            Icons.Default.WifiOff to MaterialTheme.colorScheme.error
        is NetworkException.TimeoutException ->
            Icons.Default.Warning to MaterialTheme.colorScheme.error
        is NetworkException.ServerException ->
            Icons.Default.Error to MaterialTheme.colorScheme.error
        is NetworkException.ClientException ->
            Icons.Default.Warning to MaterialTheme.colorScheme.error
        is NetworkException.ParseException ->
            Icons.Default.Error to MaterialTheme.colorScheme.error
        is NetworkException.UnknownException ->
            Icons.Default.Error to MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "错误图标",
                modifier = Modifier.size(48.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = getErrorTitle(exception),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exception.message ?: "发生未知错误",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "重试",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重试")
                }
            }
        }
    }
}

/**
 * 获取错误标题
 */
private fun getErrorTitle(exception: NetworkException): String {
    return when (exception) {
        is NetworkException.NetworkConnectionException -> "网络连接失败"
        is NetworkException.TimeoutException -> "请求超时"
        is NetworkException.ServerException -> "服务器错误"
        is NetworkException.ClientException -> "请求错误"
        is NetworkException.ParseException -> "数据解析失败"
        is NetworkException.UnknownException -> "未知错误"
    }
}

/**
 * 网络状态的通用处理组件
 */
@Composable
fun <T> NetworkStateHandler(
    networkState: androidx.compose.runtime.State<io.github.liyulin.easyscan.domain.NetworkResult<T>>,
    loadingMessage: String = "正在加载...",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    when (val state = networkState.value) {
        is io.github.liyulin.easyscan.domain.NetworkResult.Loading -> {
            LoadingIndicator(
                message = loadingMessage,
                modifier = modifier
            )
        }
        is io.github.liyulin.easyscan.domain.NetworkResult.Error -> {
            ErrorCard(
                exception = state.exception,
                onRetry = onRetry,
                modifier = modifier
            )
        }
        is io.github.liyulin.easyscan.domain.NetworkResult.Success -> {
            content(state.data)
        }
    }
}

/**
 * 简化的错误提示组件
 */
@Composable
fun SimpleErrorMessage(
    message: String,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            if (onDismiss != null) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("关闭", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
