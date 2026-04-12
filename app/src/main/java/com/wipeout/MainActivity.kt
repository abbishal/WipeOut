package com.wipeout

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.wipeout.databinding.ActivityMainBinding

/**
 * Home screen shown after the user has completed first-run setup.
 *
 * Displays the current protection status (device-admin + SMS permission) and
 * gives the user options to:
 *   - View their secret code (so they can note it down or send it to themselves)
 *   - Regenerate the secret code (e.g. after a suspected compromise)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var secretCodeManager: SecretCodeManager
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secretCodeManager = SecretCodeManager(this)

        // Redirect to setup if first-run hasn't been completed yet
        if (!secretCodeManager.isSetupDone()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, WipeOutDeviceAdminReceiver::class.java)

        binding.btnShowCode.setOnClickListener { showSecretCode() }
        binding.btnRegenerateCode.setOnClickListener { confirmRegenerateCode() }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun updateStatus() {
        val adminActive = devicePolicyManager.isAdminActive(adminComponent)
        val smsGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        binding.tvAdminStatus.text = getString(
            if (adminActive) R.string.status_admin_active else R.string.status_admin_inactive
        )
        binding.tvSmsStatus.text = getString(
            if (smsGranted) R.string.status_sms_granted else R.string.status_sms_denied
        )
        binding.tvProtectionStatus.text = getString(
            if (adminActive && smsGranted) R.string.status_protection_active
            else R.string.status_protection_inactive
        )
    }

    private fun showSecretCode() {
        val code = secretCodeManager.getSecretCode() ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_secret_code_title)
            .setMessage(getString(R.string.dialog_secret_code_message, code))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun confirmRegenerateCode() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_regenerate_title)
            .setMessage(R.string.dialog_regenerate_message)
            .setPositiveButton(R.string.dialog_regenerate_confirm) { _, _ ->
                val newCode = secretCodeManager.generateSecretCode()
                AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_new_code_title)
                    .setMessage(getString(R.string.dialog_secret_code_message, newCode))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
