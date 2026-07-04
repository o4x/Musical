package github.o4x.m2.ui.fragments.settings.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import github.o4x.m2.R
import github.o4x.m2.util.ViewInsetsUtils.applyAppBarPadding
import github.o4x.m2.util.backgroundColor

class AboutFragment : PreferenceFragmentCompat() {

    companion object {
        private const val GITHUB = "https://github.com/o4x/Musical"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_about)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The blurred app bar overlays the list, so pad the list below it.
        listView.clipToPadding = false
        listView.applyAppBarPadding(withNavBarInset = true)
        setUpViews()
    }

    private fun setUpViews() {
        val appVersion = R.string.key_app_version.getPreference()
        appVersion.summary = getCurrentVersionName(requireContext())

        val github = R.string.key_github.getPreference()
        github.setOnPreferenceClickListener {
            openUrl(GITHUB)
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
            ).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Unkown"
    }

    private fun color(intColor: Int): String {
        return String.format("%06X", 0xFFFFFF and intColor)
    }
}