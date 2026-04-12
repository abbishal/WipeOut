package com.wipeout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receives [Intent.ACTION_BOOT_COMPLETED] so that WipeOut's protection is
 * active as soon as the device finishes booting, without the user needing to
 * open the app.
 *
 * All protection components (SmsReceiver, AirplaneModeReceiver,
 * WipeOutDeviceAdminReceiver) are declared in AndroidManifest.xml and are
 * therefore already active after boot.  This receiver exists as an explicit
 * hook for any additional post-boot initialisation that may be added in the
 * future (e.g. showing a persistent status-bar notification).
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed – WipeOut protection is active")
        }
    }
}
