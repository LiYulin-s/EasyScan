package io.github.liyulin.easyscan

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}