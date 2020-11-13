package com.o4x.musical.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.o4x.musical.R
import com.o4x.musical.util.PreferenceUtil.isDarkMode
import com.o4x.musical.util.PreferenceUtil.setLastChangeLogVersion
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author Aidan Follestad (afollestad)
 */
class ChangelogDialog : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val customView: View
        customView = try {
            LayoutInflater.from(activity).inflate(R.layout.dialog_web_view, null)
        } catch (e: InflateException) {
            e.printStackTrace()
            return MaterialDialog(requireContext())
                .title(android.R.string.dialog_alert_title)
                .message(text = "This device doesn't support web view, which is necessary to view the change log. It is missing a system component.")
                .positiveButton(android.R.string.ok)
        }
        val dialog = MaterialDialog(requireContext())
            .title(R.string.changelog)
            .customView(view = customView, scrollable = true)
            .positiveButton(android.R.string.ok)
            .show {
                if (activity != null) setChangelogRead(
                    requireContext()
                )
            }

        val webView = customView.findViewById<WebView>(R.id.web_view)
        try {
            // Load from phonograph-changelog.html in the assets folder
            val buf = StringBuilder()
            val json = requireActivity().assets.open("phonograph-changelog.html")
            val `in` = BufferedReader(InputStreamReader(json, "UTF-8"))
            var str: String?
            while (`in`.readLine().also { str = it } != null) buf.append(str)
            `in`.close()

            // Inject color values for WebView body background and links
            val backgroundColor = colorToCSS(
                resolveColor(
                    requireActivity(),
                    R.attr.backgroundColor,
                    Color.parseColor(if (isDarkMode) "#424242" else "#ffffff")
                )
            )
            val contentColor =
                colorToCSS(Color.parseColor(if (isDarkMode) "#ffffff" else "#000000"))
            val changeLog = buf.toString()
                .replace(
                    "{style-placeholder}",
                    String.format(
                        "body { background-color: %s; color: %s; }",
                        backgroundColor,
                        contentColor
                    )
                )
//                .replace(
//                    "{link-color}",
//                    colorToCSS(ThemeSingleton.get().positiveColor.getDefaultColor())
//                )
//                .replace(
//                    "{link-color-active}",
//                    colorToCSS(lightenColor(ThemeSingleton.get().positiveColor.getDefaultColor()))
//                )
            webView.loadData(changeLog, "text/html", "UTF-8")
        } catch (e: Throwable) {
            webView.loadData(
                "<h1>Unable to load</h1><p>" + e.localizedMessage + "</p>",
                "text/html",
                "UTF-8"
            )
        }
        return dialog
    }

    companion object {
        fun create(): ChangelogDialog {
            return ChangelogDialog()
        }

        fun setChangelogRead(context: Context) {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersion = pInfo.versionCode
                setLastChangeLogVersion(currentVersion)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        private fun colorToCSS(color: Int): String {
            return String.format(
                "rgb(%d, %d, %d)",
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            ) // on API 29, WebView doesn't load with hex colors
        }
    }
}