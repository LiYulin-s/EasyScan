package io.github.liyulin.easyscan.domain

import io.github.liyulin.easyscan.config.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

val endpoint = BuildKonfig.URL

@Serializable
data class SetResponse(
    val key: String,
    val url: String,
    val name: String,
    val success: Boolean,
)

@Serializable
data class SetRequest(
    val url: String,
    val name: String? = null,
)

/**
 * 创建配置好的HTTP客户端
 */
private fun createHttpClient(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        // 配置超时时间
        engine {
            requestTimeout = 30000 // 30秒请求超时
        }
    }
}

/**
 * 创建新的扫描任务
 * @param name 扫描任务名称
 * @return NetworkResult<String> 包含扫描任务key的结果
 */
suspend fun newScan(name: String): NetworkResult<String> {
    return safeNetworkCallWithRetry(
        maxRetries = 3,
        retryDelayMillis = 1000
    ) {
        val client = createHttpClient()
        try {
            val response: SetResponse = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(SetRequest(
                    name = name,
                    url = ""
                ))
            }.body()

            if (response.success) {
                response.key
            } else {
                throw NetworkException.ServerException(
                    code = 500,
                    message = "创建扫描任务失败"
                )
            }
        } finally {
            client.close()
        }
    }
}

/**
 * 更新扫描任务
 * @param name 扫描任务名称（可选）
 * @param key 扫描任务key
 * @param url 扫描到的二维码内容
 * @return NetworkResult<Boolean> 更新是否成功的结果
 */
suspend fun updateScan(name: String?, key: String, url: String): NetworkResult<Boolean> {
    return safeNetworkCallWithRetry(
        maxRetries = 2,
        retryDelayMillis = 500
    ) {
        val client = createHttpClient()
        try {
            val response: SetResponse = client.post("$endpoint/$key") {
                contentType(ContentType.Application.Json)
                setBody(SetRequest(
                    name = name,
                    url = url
                ))
            }.body()

            response.success
        } finally {
            client.close()
        }
    }
}

/**
 * 兼容性包装函数，保持旧API的兼容性
 * 注意：这些函数会抛出异常，建议使用返回NetworkResult的新版本
 */
@Deprecated("使用返回NetworkResult的newScan版本", ReplaceWith("newScan(name)"))
suspend fun newScanLegacy(name: String): String {
    return when (val result = newScan(name)) {
        is NetworkResult.Success -> result.data
        is NetworkResult.Error -> throw result.exception
        is NetworkResult.Loading -> throw NetworkException.UnknownException("意外的加载状态")
    }
}

@Deprecated("使用返回NetworkResult的updateScan版本", ReplaceWith("updateScan(name, key, url)"))
suspend fun updateScanLegacy(name: String?, key: String, url: String): Boolean {
    return when (val result = updateScan(name, key, url)) {
        is NetworkResult.Success -> result.data
        is NetworkResult.Error -> throw result.exception
        is NetworkResult.Loading -> throw NetworkException.UnknownException("意外的加载状态")
    }
}