package com.example.csl_kotlin_projekt.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

object AppLog {
    private const val ROOT = "AppLifecycle"
    fun d(tag: String, message: String) {
        Log.d("$ROOT/$tag", message)
    }
    fun i(tag: String, message: String) {
        Log.i("$ROOT/$tag", message)
    }
    fun w(tag: String, message: String) {
        Log.w("$ROOT/$tag", message)
    }
    fun e(tag: String, message: String, tr: Throwable? = null) {
        if (tr != null) Log.e("$ROOT/$tag", message, tr) else Log.e("$ROOT/$tag", message)
    }
}

@Composable
fun LogComposableLifecycle(tag: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        AppLog.d(tag, "Composable ENTER (composition)")
        val observer = LifecycleEventObserver { _, event: Lifecycle.Event ->
            AppLog.d(tag, "Lifecycle ${event.name}")
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            AppLog.d(tag, "Composable EXIT (dispose)")
        }
    }
}

