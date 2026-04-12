package com.wipeout

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device-admin receiver required by Android to grant the app the ability to
 * call [android.app.admin.DevicePolicyManager.lockNow].
 *
 * The user must explicitly activate this receiver through the system settings
 * UI; it cannot be activated silently.
 */
class WipeOutDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, R.string.admin_enabled, Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, R.string.admin_disabled, Toast.LENGTH_SHORT).show()
    }
}
