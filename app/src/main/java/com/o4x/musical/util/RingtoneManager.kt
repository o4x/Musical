package com.o4x.musical.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R

class RingtoneManager {
    fun setRingtone(context: Context, id: Long) {
        val resolver = context.contentResolver
        val uri = MusicUtil.getSongFileUri(id)
        try {
            val values = ContentValues(2)
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1")
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1")
            resolver.update(uri, values, null, null)
        } catch (ignored: UnsupportedOperationException) {
            return
        }
        try {
            val cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.MediaColumns.TITLE),
                BaseColumns._ID + "=?", arrayOf(id.toString()),
                null
            )
            try {
                if (cursor != null && cursor.count == 1) {
                    cursor.moveToFirst()
                    Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString())
                    val message =
                        context.getString(R.string.x_has_been_set_as_ringtone, cursor.getString(0))
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } finally {
                cursor?.close()
            }
        } catch (ignored: SecurityException) {
        }
    }

    companion object {
        @JvmStatic
        fun requiresDialog(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                !Settings.System.canWrite(context)
            } else false
        }

        @JvmStatic
        fun showDialog(context: Context): MaterialDialog {
            return MaterialDialog(context)
                .title(R.string.dialog_ringtone_title)
                .message(R.string.dialog_ringtone_message)
                .positiveButton(android.R.string.ok) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(intent)
                }
                .negativeButton(android.R.string.cancel).also {
                    it.show()
                }
        }
    }
}