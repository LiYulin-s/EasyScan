package io.github.liyulin.easyscan

import android.content.Context
import java.lang.ref.WeakReference

object ContextHandler {
    private var context: WeakReference<Context?>? = null
    val currentContext: Context?
        get() {
            return context?.get()
        }

    fun initialize(context: Context) {
        this.context = WeakReference(context)
    }
}