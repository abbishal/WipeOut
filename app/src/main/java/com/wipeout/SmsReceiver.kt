package com.wipeout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

/**
 * BroadcastReceiver that listens for incoming SMS messages.
 *
 * When an SMS is received the body of each message part is checked against the
 * stored secret code.  If a match is found [LockManager.lockDevice] is called
 * immediately, locking the screen so an unauthorised user cannot access the phone.
 *
 * The receiver is registered in AndroidManifest.xml with
 * `android:permission="android.permission.BROADCAST_SMS"` to ensure that only
 * the system can trigger it, preventing spoofed broadcasts from other apps.
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val secretCodeManager = SecretCodeManager(context)
        val lockManager = LockManager(context)

        for (message in messages) {
            val body = message.messageBody ?: continue
            Log.d(TAG, "SMS received – checking secret code")
            if (secretCodeManager.matchesSecretCode(body)) {
                Log.i(TAG, "Secret code matched – locking device")
                lockManager.lockDevice()
                break
            }
        }
    }
}
