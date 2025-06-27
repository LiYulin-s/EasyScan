package io.github.liyulin.easyscan.domain

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 网络请求结果的封装类
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

/**
 * 自定义网络异常类
 */
sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * 网络连接异常
     */
    class NetworkConnectionException(
        message: String = "网络连接失败，请检查网络设置",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 服务器异常
     */
    class ServerException(
        val code: Int,
        message: String = "服务器响应异常 (错误代码: $code)",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 请求超时异常
     */
    class TimeoutException(
        message: String = "请求超时，请稍后重试",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 数据解析异常
     */
    class ParseException(
        message: String = "数据解析失败",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 客户端请求异常
     */
    class ClientException(
        val code: Int,
        message: String = "请求参数错误 (错误代码: $code)",
        cause: Throwable? = null
    ) : NetworkException(message, cause)

    /**
     * 未知异常
     */
    class UnknownException(
        message: String = "发生未知错误，请稍后重试",
        cause: Throwable? = null
    ) : NetworkException(message, cause)
}

/**
 * 异常转换器，将标准异常转换为自定义网络异常
 */
object NetworkExceptionMapper {

    fun mapException(throwable: Throwable): NetworkException {
        return when (throwable) {
            is NetworkException -> throwable

            // 网络连接相关异常
            is ConnectException,
            is UnknownHostException -> NetworkException.NetworkConnectionException(
                cause = throwable
            )

            // 超时相关异常
            is SocketTimeoutException,
            is TimeoutCancellationException -> NetworkException.TimeoutException(
                cause = throwable
            )

            // HTTP客户端异常 (4xx)
            is ClientRequestException -> NetworkException.ClientException(
                code = throwable.response.status.value,
                message = "请求错误: ${throwable.response.status.description}",
                cause = throwable
            )

            // HTTP服务器异常 (5xx)
            is ServerResponseException -> NetworkException.ServerException(
                code = throwable.response.status.value,
                message = "服务器错误: ${throwable.response.status.description}",
                cause = throwable
            )

            // HTTP重定向异常 (3xx)
            is RedirectResponseException -> NetworkException.ServerException(
                code = throwable.response.status.value,
                message = "重定向错误: ${throwable.response.status.description}",
                cause = throwable
            )

            // 序列化异常
            is SerializationException -> NetworkException.ParseException(
                message = "数据格式错误: ${throwable.message}",
                cause = throwable
            )

            // 其他未知异常
            else -> NetworkException.UnknownException(
                message = throwable.message ?: "发生未知错误",
                cause = throwable
            )
        }
    }
}

/**
 * 安全的网络请求执行器
 */
suspend fun <T> safeNetworkCall(
    call: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (e: Exception) {
        NetworkResult.Error(NetworkExceptionMapper.mapException(e))
    }
}

/**
 * 带重试机制的网络请求执行器
 */
suspend fun <T> safeNetworkCallWithRetry(
    maxRetries: Int = 3,
    retryDelayMillis: Long = 1000,
    call: suspend () -> T
): NetworkResult<T> {
    repeat(maxRetries) { attempt ->
        when (val result = safeNetworkCall(call)) {
            is NetworkResult.Success -> return result
            is NetworkResult.Error -> {
                // 如果是最后一次尝试，或者是不应该重试的异常，直接返回错误
                if (attempt == maxRetries - 1 || !shouldRetry(result.exception)) {
                    return result
                }
                // 等待后重试
                kotlinx.coroutines.delay(retryDelayMillis * (attempt + 1))
            }
            is NetworkResult.Loading -> {
                // 等待后继续下一次重试
                kotlinx.coroutines.delay(retryDelayMillis)
            }
        }
    }
    return NetworkResult.Error(NetworkException.UnknownException("重试次数已达上限"))
}

/**
 * 判断异常是否应该重试
 */
private fun shouldRetry(exception: NetworkException): Boolean {
    return when (exception) {
        is NetworkException.NetworkConnectionException,
        is NetworkException.TimeoutException,
        is NetworkException.ServerException -> true
        is NetworkException.ClientException,
        is NetworkException.ParseException,
        is NetworkException.UnknownException -> false
    }
}
