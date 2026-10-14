package app.lawnchair.integrations

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import app.lawnchair.LawnchairLauncher
import app.lawnchair.preferences.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class D2LockBridge(private val launcher: LawnchairLauncher) {
    private var resuming = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            launcher.window.decorView.postDelayed({
                if (launcher.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) resumeIfLocked()
            }, 180L)
        }
    }

    fun register() {
        ContextCompat.registerReceiver(launcher, receiver, IntentFilter(Intent.ACTION_SCREEN_ON), ContextCompat.RECEIVER_EXPORTED)
    }

    fun close() { runCatching { launcher.unregisterReceiver(receiver) } }

    fun resumeIfLocked() {
        if (resuming) return
        resuming = true
        launcher.lifecycleScope.launch {
            try { requestLock(launcher, force = false) } finally { resuming = false }
        }
    }

    companion object {
        suspend fun requestLock(context: Context, force: Boolean): Boolean {
            if (!PreferenceManager.getInstance(context).d2LockIntegration.get()) return false
            val state = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.call(Uri.parse("content://app.d2lock.launcher"), "state", null, null) }.getOrNull()
            } ?: return false
            if (!force && context is LawnchairLauncher && !context.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return false
            if (state.getInt("protocol") != 1) return false
            if (!state.getBoolean("ready") || (!force && !state.getBoolean("locked"))) return false
            @Suppress("DEPRECATION")
            val pending = state.getParcelable<PendingIntent>(if (force) "lock" else "resume") ?: return false
            return runCatching {
                val options = ActivityOptions.makeBasic().apply {
                    if (Build.VERSION.SDK_INT >= 36) {
                        setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE)
                    } else if (Build.VERSION.SDK_INT >= 34) {
                        setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    }
                }
                pending.send(context, 0, null, null, null, null, options.toBundle())
                true
            }.getOrDefault(false)
        }
    }
}
