package com.fabsimple.app

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Global static holder for Android Context to allow non-UI classes to trigger platform services.
 */
object AppContext {
    private var contextRef: WeakReference<Context>? = null

    var context: Context?
        get() = contextRef?.get()
        set(value) {
            contextRef = value?.let { WeakReference(it) }
        }
}
