package github.o4x.musical.ui.activities.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import github.o4x.musical.R
import github.o4x.musical.util.accentColor

abstract class AbsBaseActivity : AppCompatActivity() {

    private var hadPermissions: Boolean = false
    private lateinit var permissions: Array<String>
    private var permissionDeniedMessage: String? = null

    open fun getPermissionsToRequest(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires READ_MEDIA_AUDIO
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // Android 12- requires READ_EXTERNAL_STORAGE
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    protected fun setPermissionDeniedMessage(message: String) {
        permissionDeniedMessage = message
    }

    fun getPermissionDeniedMessage(): String {
        return permissionDeniedMessage ?: getString(R.string.permissions_denied)
    }

    open val snackBarContainer: View
        get() = window.decorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC

        permissions = getPermissionsToRequest()
        hadPermissions = hasCriticalPermissions()
        permissionDeniedMessage = null

        if (!hadPermissions) {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        val hasPermissions = hasCriticalPermissions()
        if (hasPermissions != hadPermissions) {
            hadPermissions = hasPermissions
            onHasPermissionsChanged(hasPermissions)
        }
    }

    protected open fun onHasPermissionsChanged(hasPermissions: Boolean) {
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_MENU && event.action == KeyEvent.ACTION_UP) {
            showOverflowMenu()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showOverflowMenu() {
    }

    protected open fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
    }

    protected open fun hasCriticalPermissions(): Boolean {
        for (permission in permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                permission == Manifest.permission.POST_NOTIFICATIONS
            ) {
                continue
            }

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST) {
            var criticalPermissionDenied = false

            // Loop results to see if Audio/Storage was denied
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    // Ignore Notifications
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        permission == Manifest.permission.POST_NOTIFICATIONS
                    ) {
                        continue
                    }

                    criticalPermissionDenied = true
                    break
                }
            }

            if (criticalPermissionDenied) {
                // Check if we should show Rationale (User denied once, but not permanently)
                // We pick the first critical permission to check rationale against.
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
                )

                if (showRationale) {
                    // Case A: User clicked "Deny". Give them a chance to "Grant" again.
                    Snackbar.make(
                        snackBarContainer,
                        R.string.permissions_denied, // Ensure this string exists in strings.xml
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.action_grant) { requestPermissions() }
                        .setActionTextColor(accentColor()).show()
                } else {
                    // Case B: User clicked "Don't ask again" OR System blocked it (Manifest missing).
                    // We send them to SETTINGS.
                    Snackbar.make(
                        snackBarContainer,
                        R.string.permissions_denied,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.action_settings) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }.setActionTextColor(accentColor()).show()
                }
            } else {
                // Success
                hadPermissions = true
                onHasPermissionsChanged(true)
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST = 100
    }
}
