package com.example.csl_kotlin_projekt.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

object AppLog {
    private const val ROOT = "AL"  // Short tag for Android compatibility
    private fun tag(name: String): String {
        // Ensure tag is max 23 chars for Android < 8.0 compatibility
        val fullTag = "$ROOT/$name"
        return if (fullTag.length <= 23) fullTag else fullTag.substring(0, 23)
    }

    fun d(tag: String, message: String) {
        val t = tag(tag)
        Log.d(t, message)
        Log.i(t, message)  // Also log at INFO so it appears with level:info filter
    }
    fun i(tag: String, message: String) {
        Log.i(tag(tag), message)
    }
    fun w(tag: String, message: String) {
        Log.w(tag(tag), message)
    }
    fun e(tag: String, message: String, tr: Throwable? = null) {
        if (tr != null) Log.e(tag(tag), message, tr) else Log.e(tag(tag), message)
    }
}

@Composable
fun LogComposableLifecycle(tag: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        AppLog.i("AL/$tag", "Composable ENTER (composition)")
        val observer = LifecycleEventObserver { _, event: Lifecycle.Event ->
            AppLog.i("AL/$tag", "Lifecycle ${event.name}")
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            AppLog.i("AL/$tag", "Composable EXIT (dispose)")
        }
    }
}

