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
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.ui.activities.PurchaseActivity
import com.o4x.musical.ui.dialogs.ChangelogDialog
import com.o4x.musical.ui.dialogs.DonationsDialog
import com.o4x.musical.util.PreferenceUtil.isDarkMode
import de.psdev.licensesdialog.LicensesDialog

class AboutFragment : PreferenceFragmentCompat() {

    companion object {
        private const val TRANSLATE = "https://phonograph.oneskyapp.com/collaboration/project?id=26521"
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
            ChangelogDialog.create().show(requireActivity().supportFragmentManager,
                "CHANGELOG_DIALOG")
            return@setOnPreferenceClickListener true
        }

        val licenses = R.string.key_licenses.getPreference()
        licenses.setOnPreferenceClickListener {
            showLicenseDialog()
            return@setOnPreferenceClickListener true
        }

        val writeAnEmail = R.string.key_write_an_email.getPreference()
        writeAnEmail.summary = "contact@musical.com"
        writeAnEmail.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:contact@musical.com")
            intent.putExtra(Intent.EXTRA_EMAIL, "contact@musical.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Musical")
            startActivity(Intent.createChooser(intent, "E-Mail"))
            return@setOnPreferenceClickListener true
        }

        val reportBugs = R.string.key_report_bugs.getPreference()
        reportBugs.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_about_to_bug)
            return@setOnPreferenceClickListener true
        }

        val translate = R.string.key_translate.getPreference()
        translate.setOnPreferenceClickListener {
            openUrl(TRANSLATE)
            return@setOnPreferenceClickListener true
        }

        val donate = R.string.key_donate.getPreference()
        donate.setOnPreferenceClickListener {
            if (App.isProVersion()) {
                DonationsDialog.create().show(requireActivity().supportFragmentManager,
                    "DONATION_DIALOG")
            } else {
                startActivity(Intent(requireContext(), PurchaseActivity::class.java))
            }
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
            return context.packageManager.getPackageInfo(context.packageName,
                0).versionName + if (App.isProVersion()) " Pro" else ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Unkown"
    }

    private fun showLicenseDialog() {
        LicensesDialog.Builder(requireContext())
            .setNotices(R.raw.notices)
            .setTitle(R.string.licenses)
            .setNoticesCssStyle(getString(R.string.license_dialog_style)
                .replace("{bg-color}", if (isDarkMode) "424242" else "ffffff")
                .replace("{text-color}", if (isDarkMode) "ffffff" else "000000")
                .replace("{license-bg-color}",
                    if (isDarkMode) "535353" else "eeeeee")
            )
            .setIncludeOwnLicense(true)
            .build()
            .show()
    }
}