package com.wipeout

import android.content.Context
import android.content.SharedPreferences
import java.security.SecureRandom

/**
 * Manages generation, storage and validation of the user's secret code.
 *
 * The code is persisted in private SharedPreferences so it survives app
 * restarts without requiring any network connectivity.
 */
class SecretCodeManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "WipeOutPrefs"
        private const val KEY_SECRET_CODE = "secret_code"
        private const val KEY_SETUP_DONE = "setup_done"
        private const val CODE_LENGTH = 8
        private const val CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns true if the user has already completed first-run setup. */
    fun isSetupDone(): Boolean = prefs.getBoolean(KEY_SETUP_DONE, false)

    /**
     * Generates a new cryptographically-random secret code, stores it, and
     * returns the plain-text value so it can be shown to the user once.
     */
    fun generateSecretCode(): String {
        val random = SecureRandom()
        val charsetLength = CHARSET.length
        val sb = StringBuilder(CODE_LENGTH)
        repeat(CODE_LENGTH) { sb.append(CHARSET[random.nextInt(charsetLength)]) }
        val code = sb.toString()
        prefs.edit().putString(KEY_SECRET_CODE, code).apply()
        return code
    }

    /**
     * Returns the existing secret code if one is already stored, or generates
     * and stores a new one.  Use this on first-run screens where the code must
     * be shown but should not change across configuration changes.
     */
    fun getOrGenerateSecretCode(): String = getSecretCode() ?: generateSecretCode()

    /** Returns the currently stored secret code, or null if none exists. */
    fun getSecretCode(): String? = prefs.getString(KEY_SECRET_CODE, null)

    /**
     * Returns true if [input] contains the stored secret code (case-insensitive).
     * This allows the user to send the code anywhere in an SMS message body.
     */
    fun matchesSecretCode(input: String): Boolean {
        val code = getSecretCode() ?: return false
        return input.contains(code, ignoreCase = true)
    }

    /** Marks first-run setup as complete. */
    fun markSetupDone() {
        prefs.edit().putBoolean(KEY_SETUP_DONE, true).apply()
    }
}
