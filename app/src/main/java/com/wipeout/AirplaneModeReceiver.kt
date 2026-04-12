package com.wipeout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver that monitors changes to airplane (flight) mode.
 *
 * On modern Android it is not possible for a third-party app to programmatically
 * disable airplane mode.  Instead, when airplane mode is detected we immediately
 * notify the user via a high-priority notification so they are aware that
 * SMS-based remote locking will be unavailable until airplane mode is disabled.
 *
 * This acts as a deterrent: anyone who puts the phone in flight mode to block
 * the remote-lock SMS will see a visible alert if/when they unlock the phone.
 */
class AirplaneModeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AirplaneModeReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Intent.ACTION_AIRPLANE_MODE_CHANGED resolves to the string
        // "android.intent.action.AIRPLANE_MODE" (see android.content.Intent source).
        // The manifest filter uses the same string value.
        if (intent.action != Intent.ACTION_AIRPLANE_MODE_CHANGED) return

        val isAirplaneModeOn = intent.getBooleanExtra("state", false)
        if (isAirplaneModeOn) {
            Log.w(TAG, "Airplane mode enabled – notifying user")
            LockManager(context).onAirplaneModeDetected()
        }
    }
}
