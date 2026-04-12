package com.wipeout

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.wipeout.databinding.ActivitySetupBinding

/**
 * Shown only on the very first launch.  It:
 *   1. Generates a new secret code and displays it prominently.
 *   2. Guides the user through granting SMS / notification permissions.
 *   3. Guides the user through activating device-admin privileges.
 *   4. Navigates to [MainActivity] once all steps are complete.
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var secretCodeManager: SecretCodeManager
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    // -------------------------------------------------------------------------
    // Permission / admin-activation launchers (modern Activity Result API)
    // -------------------------------------------------------------------------

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                Toast.makeText(this, R.string.permissions_granted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_LONG).show()
            }
        }

    private val adminActivationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (devicePolicyManager.isAdminActive(adminComponent)) {
                Toast.makeText(this, R.string.admin_enabled, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.admin_not_granted, Toast.LENGTH_LONG).show()
            }
        }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secretCodeManager = SecretCodeManager(this)
        devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, WipeOutDeviceAdminReceiver::class.java)

        // Generate (or retrieve) the secret code and display it.
        // getOrGenerateSecretCode() is used instead of generateSecretCode() so that
        // rotating the screen (which recreates the Activity) does not silently
        // create a new code and invalidate any code the user has already noted down.
        val code = secretCodeManager.getOrGenerateSecretCode()
        binding.tvSecretCode.text = code
        binding.tvSetupInfo.text = getString(R.string.setup_info_message)

        binding.btnRequestPermissions.setOnClickListener { requestRequiredPermissions() }
        binding.btnActivateAdmin.setOnClickListener { requestAdminPermission() }
        binding.btnFinishSetup.setOnClickListener { tryFinishSetup() }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        requestPermissionsLauncher.launch(permissions.toTypedArray())
    }

    private fun requestAdminPermission() {
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            Toast.makeText(this, R.string.admin_already_active, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.admin_explanation)
            )
        }
        adminActivationLauncher.launch(intent)
    }

    private fun allPermissionsGranted(): Boolean {
        val smsGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        return smsGranted && devicePolicyManager.isAdminActive(adminComponent)
    }

    private fun tryFinishSetup() {
        if (!allPermissionsGranted()) {
            Toast.makeText(this, R.string.setup_incomplete_warning, Toast.LENGTH_LONG).show()
            return
        }
        secretCodeManager.markSetupDone()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
