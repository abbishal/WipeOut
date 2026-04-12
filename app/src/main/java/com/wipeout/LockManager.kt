package com.wipeout

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * Centralises all screen-locking and notification logic so it can be invoked
 * from both [SmsReceiver] and [AirplaneModeReceiver] without duplicating code.
 */
class LockManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "wipeout_channel"
        private const val NOTIFICATION_ID_LOCK = 1001
        private const val NOTIFICATION_ID_AIRPLANE = 1002
    }

    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent = ComponentName(context, WipeOutDeviceAdminReceiver::class.java)

    /** Returns true if the app holds device-admin privileges. */
    fun isAdminActive(): Boolean = devicePolicyManager.isAdminActive(adminComponent)

    /**
     * Locks the screen immediately if device-admin is active.
     * If the admin is not active a high-priority notification is shown so the
     * user knows the remote-lock attempt was received but could not be executed.
     */
    fun lockDevice() {
        if (isAdminActive()) {
            devicePolicyManager.lockNow()
        } else {
            showNotification(
                notificationId = NOTIFICATION_ID_LOCK,
                title = context.getString(R.string.notification_lock_failed_title),
                message = context.getString(R.string.notification_lock_failed_message)
            )
        }
    }

    /**
     * Called when airplane mode is detected.  We cannot turn airplane mode off
     * programmatically on modern Android, so we alert the user via a
     * high-priority notification.
     */
    fun onAirplaneModeDetected() {
        showNotification(
            notificationId = NOTIFICATION_ID_AIRPLANE,
            title = context.getString(R.string.notification_airplane_title),
            message = context.getString(R.string.notification_airplane_message)
        )
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun showNotification(notificationId: Int, title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
