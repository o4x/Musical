package com.o4x.musical.ui.fragments.settings.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import code.name.monkey.appthemehelper.extensions.backgroundColor
import code.name.monkey.appthemehelper.extensions.surfaceColor
import code.name.monkey.appthemehelper.extensions.textColorPrimary
import com.o4x.musical.App
import com.o4x.musical.R
import de.psdev.licensesdialog.LicensesDialog

class AboutFragment : PreferenceFragmentCompat() {

    companion object {
//        private const val TRANSLATE = "https://phonograph.oneskyapp.com/collaboration/project?id=26521"
        private const val EMAIL = "apps.musical@gmail.com"
        private const val TELEGRAM = "https://t.me/app_musical"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_about)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.setBackgroundColor(backgroundColor())
        setUpViews()
    }

    private fun setUpViews() {
        val appVersion = R.string.key_app_version.getPreference()
        appVersion.summary = getCurrentVersionName(requireContext())

        val changelog = R.string.key_changelog.getPreference()
        changelog.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_about_to_changes)
            return@setOnPreferenceClickListener true
        }

        val licenses = R.string.key_licenses.getPreference()
        licenses.setOnPreferenceClickListener {
            showLicenseDialog()
            return@setOnPreferenceClickListener true
        }

        val email = R.string.key_email.getPreference()
        email.summary = EMAIL
        email.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$EMAIL")
            intent.putExtra(Intent.EXTRA_EMAIL, EMAIL)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Musical")
            startActivity(Intent.createChooser(intent, "E-Mail"))
            return@setOnPreferenceClickListener true
        }

        val telegram = R.string.key_telegram.getPreference()
        telegram.summary = TELEGRAM
        telegram.setOnPreferenceClickListener {
            openUrl(TELEGRAM)
            return@setOnPreferenceClickListener true
        }
    }


    private fun Int.getPreference(): Preference =
        findPreference(getString(this))!!

    private fun openUrl(url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun getCurrentVersionName(context: Context): String? {
        try {
            return context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName + if (App.isCleanVersion()) "clean" else ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Unkown"
    }

    private fun showLicenseDialog() {
        LicensesDialog.Builder(requireContext())
            .setNotices(R.raw.notices)
            .setTitle(R.string.licenses)
            .setNoticesCssStyle(
                getString(R.string.license_dialog_style)
                    .replace("{bg-color}", color(backgroundColor()))
                    .replace("{text-color}", color(textColorPrimary()))
                    .replace("{license-bg-color}", color(surfaceColor()))
            )
            .setIncludeOwnLicense(true)
            .build()
            .show()
    }

    private fun color(intColor: Int): String {
        return String.format("%06X", 0xFFFFFF and intColor)
    }
}