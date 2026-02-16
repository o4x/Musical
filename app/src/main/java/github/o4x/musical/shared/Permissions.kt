package github.o4x.musical.shared

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object Permissions {

    private const val READ_CODE = 100

    private const val READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private const val READ_AUDIO = Manifest.permission.READ_MEDIA_AUDIO

    @JvmStatic
    fun checkWriteCode(code: Int): Boolean {
        return code == READ_CODE
    }

    @JvmStatic
    fun canReadStorage(context: Context): Boolean {
        return hasPermission(
            context,
            READ_STORAGE
        ) || hasPermission(context, READ_AUDIO)
    }

    @JvmStatic
    fun requestReadStorage(fragment: Fragment) {
        requestPermissions(
            fragment,
            READ_STORAGE,
            READ_CODE
        )
    }

    @JvmStatic
    fun hasUserDisabledReadStorage(fragment: Fragment): Boolean {
        return hasUserDisabledPermission(
            fragment,
            READ_STORAGE
        )
    }

    @JvmStatic
    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    private fun requestPermissions(fragment: Fragment, permission: String, requestCode: Int) {
        fragment.requestPermissions(arrayOf(permission), requestCode)
    }

    @JvmStatic
    private fun hasUserDisabledPermission(fragment: Fragment, permission: String): Boolean {
        return !fragment.shouldShowRequestPermissionRationale(permission)
    }

}