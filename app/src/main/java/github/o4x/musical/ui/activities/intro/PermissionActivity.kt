package github.o4x.musical.ui.activities.intro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import github.o4x.musical.databinding.ActivityPermissionBinding
import github.o4x.musical.ui.activities.base.AbsMusicServiceActivity

class PermissionActivity : AbsMusicServiceActivity() {

    private val binding by lazy { ActivityPermissionBinding.inflate(layoutInflater) }

    // Define permissions based on Android Version
    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) and above: Granular Media + Notifications
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // Android 12 and below: Legacy Storage
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // UI Setup
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        setLightNavigationBar(true)
        setTaskDescriptionColorAuto()

        update()

        binding.storagePermission.setOnClickListener {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    requiredPermissions,
                    STORAGE_PERMISSION_CODE
                )
            }
        }

        binding.finish.setOnClickListener {
            if (allPermissionsGranted()) {
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if permissions were granted
        if (requestCode == STORAGE_PERMISSION_CODE) {
            update()
        }
    }

    private fun update() {
        val granted = allPermissionsGranted()
        binding.finish.isEnabled = granted
        binding.storagePermission.isEnabled = !granted

        // Optional: Change text of button to "Granted" or similar if needed
        if (granted) {
            binding.storagePermission.alpha = 0.5f
        } else {
            binding.storagePermission.alpha = 1.0f
        }
    }

    private fun allPermissionsGranted(): Boolean {
        // We iterate through the permissions required for THIS device version
        // and check if they are granted.
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Special case: Notification permission is optional for app functionality,
                // but Audio is mandatory. You might want to allow finish() if only Audio is granted.
                // For strict checking:
                if (permission == Manifest.permission.POST_NOTIFICATIONS) {
                    continue // Don't block the user if they denied notifications
                }
                return false
            }
        }
        return true
    }
}
