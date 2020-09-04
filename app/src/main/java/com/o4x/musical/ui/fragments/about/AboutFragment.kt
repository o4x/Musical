package com.o4x.musical.ui.fragments.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.internal.ThemeSingleton
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.ui.activities.PurchaseActivity
import com.o4x.musical.ui.activities.intro.AppIntroActivity
import com.o4x.musical.ui.dialogs.ChangelogDialog
import com.o4x.musical.ui.dialogs.DonationsDialog
import de.psdev.licensesdialog.LicensesDialog

class AboutFragment : Fragment(R.layout.fragment_about), View.OnClickListener {

    private val GITHUB = "https://github.com/kabouzeid/Musical"
    private val TWITTER = "https://twitter.com/swiftkarim"
    private val WEBSITE = "https://kabouzeid.com/"
    private val TRANSLATE = "https://phonograph.oneskyapp.com/collaboration/project?id=26521"
    private val RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.o4x.musical"
    private val AIDAN_FOLLESTAD_GITHUB = "https://github.com/afollestad"
    private val MICHAEL_COOK_WEBSITE = "https://cookicons.co/"
    private val MAARTEN_CORPEL_WEBSITE = "https://maartencorpel.com/"
    private val MAARTEN_CORPEL_TWITTER = "https://twitter.com/maartencorpel"
    private val ALEKSANDAR_TESIC_TWITTER = "https://twitter.com/djsalezmaj"
    private val EUGENE_CHEUNG_GITHUB = "https://github.com/arkon"
    private val EUGENE_CHEUNG_WEBSITE = "https://echeung.me/"
    private val ADRIAN_TWITTER = "https://twitter.com/froschgames"

    @JvmField
    @BindView(R.id.app_version)
    var appVersion: TextView? = null
    @JvmField
    @BindView(R.id.changelog)
    var changelog: LinearLayout? = null
    @JvmField
    @BindView(R.id.intro)
    var intro: LinearLayout? = null
    @JvmField
    @BindView(R.id.licenses)
    var licenses: LinearLayout? = null
    @JvmField
    @BindView(R.id.write_an_email)
    var writeAnEmail: LinearLayout? = null
    @JvmField
    @BindView(R.id.follow_on_twitter)
    var followOnTwitter: LinearLayout? = null
    @JvmField
    @BindView(R.id.fork_on_github)
    var forkOnGitHub: LinearLayout? = null
    @JvmField
    @BindView(R.id.visit_website)
    var visitWebsite: LinearLayout? = null
    @JvmField
    @BindView(R.id.report_bugs)
    var reportBugs: LinearLayout? = null
    @JvmField
    @BindView(R.id.translate)
    var translate: LinearLayout? = null
    @JvmField
    @BindView(R.id.donate)
    var donate: LinearLayout? = null
    @JvmField
    @BindView(R.id.rate_on_google_play)
    var rateOnGooglePlay: LinearLayout? = null
    @JvmField
    @BindView(R.id.aidan_follestad_git_hub)
    var aidanFollestadGitHub: AppCompatButton? = null
    @JvmField
    @BindView(R.id.michael_cook_website)
    var michaelCookWebsite: AppCompatButton? = null
    @JvmField
    @BindView(R.id.maarten_corpel_website)
    var maartenCorpelWebsite: AppCompatButton? = null
    @JvmField
    @BindView(R.id.maarten_corpel_twitter)
    var maartenCorpelTwitter: AppCompatButton? = null
    @JvmField
    @BindView(R.id.aleksandar_tesic_twitter)
    var aleksandarTesicTwitter: AppCompatButton? = null
    @JvmField
    @BindView(R.id.eugene_cheung_git_hub)
    var eugeneCheungGitHub: AppCompatButton? = null
    @JvmField
    @BindView(R.id.eugene_cheung_website)
    var eugeneCheungWebsite: AppCompatButton? = null
    @JvmField
    @BindView(R.id.adrian_twitter)
    var adrianTwitter: AppCompatButton? = null

    private lateinit var unbinder: Unbinder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view: View? = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            unbinder = ButterKnife.bind(this, view)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }


    private fun setUpViews() {
        setUpAppVersion()
        setUpOnClickListeners()
    }

    private fun setUpAppVersion() {
        appVersion!!.text = getCurrentVersionName(requireContext())
    }

    private fun setUpOnClickListeners() {
        changelog!!.setOnClickListener(this)
        intro!!.setOnClickListener(this)
        licenses!!.setOnClickListener(this)
        followOnTwitter!!.setOnClickListener(this)
        forkOnGitHub!!.setOnClickListener(this)
        visitWebsite!!.setOnClickListener(this)
        reportBugs!!.setOnClickListener(this)
        writeAnEmail!!.setOnClickListener(this)
        translate!!.setOnClickListener(this)
        rateOnGooglePlay!!.setOnClickListener(this)
        donate!!.setOnClickListener(this)
        aidanFollestadGitHub!!.setOnClickListener(this)
        michaelCookWebsite!!.setOnClickListener(this)
        maartenCorpelWebsite!!.setOnClickListener(this)
        maartenCorpelTwitter!!.setOnClickListener(this)
        aleksandarTesicTwitter!!.setOnClickListener(this)
        eugeneCheungGitHub!!.setOnClickListener(this)
        eugeneCheungWebsite!!.setOnClickListener(this)
        adrianTwitter!!.setOnClickListener(this)
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

    override fun onClick(v: View) {
        if (v === changelog) {
            ChangelogDialog.create().show(requireActivity().supportFragmentManager,
                "CHANGELOG_DIALOG")
        } else if (v === licenses) {
            showLicenseDialog()
        } else if (v === intro) {
            startActivity(Intent(requireContext(), AppIntroActivity::class.java))
        } else if (v === followOnTwitter) {
            openUrl(TWITTER)
        } else if (v === forkOnGitHub) {
            openUrl(GITHUB)
        } else if (v === visitWebsite) {
            openUrl(WEBSITE)
        } else if (v === reportBugs) {
            findNavController().navigate(R.id.action_about_to_bug)
        } else if (v === writeAnEmail) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:contact@kabouzeid.com")
            intent.putExtra(Intent.EXTRA_EMAIL, "contact@kabouzeid.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Musical")
            startActivity(Intent.createChooser(intent, "E-Mail"))
        } else if (v === translate) {
            openUrl(TRANSLATE)
        } else if (v === rateOnGooglePlay) {
            openUrl(RATE_ON_GOOGLE_PLAY)
        } else if (v === donate) {
            if (App.isProVersion()) {
                DonationsDialog.create().show(requireActivity().getSupportFragmentManager(),
                    "DONATION_DIALOG")
            } else {
                startActivity(Intent(requireContext(), PurchaseActivity::class.java))
            }
        } else if (v === aidanFollestadGitHub) {
            openUrl(AIDAN_FOLLESTAD_GITHUB)
        } else if (v === michaelCookWebsite) {
            openUrl(MICHAEL_COOK_WEBSITE)
        } else if (v === maartenCorpelWebsite) {
            openUrl(MAARTEN_CORPEL_WEBSITE)
        } else if (v === maartenCorpelTwitter) {
            openUrl(MAARTEN_CORPEL_TWITTER)
        } else if (v === aleksandarTesicTwitter) {
            openUrl(ALEKSANDAR_TESIC_TWITTER)
        } else if (v === eugeneCheungGitHub) {
            openUrl(EUGENE_CHEUNG_GITHUB)
        } else if (v === eugeneCheungWebsite) {
            openUrl(EUGENE_CHEUNG_WEBSITE)
        } else if (v === adrianTwitter) {
            openUrl(ADRIAN_TWITTER)
        }
    }
    
    private fun openUrl(url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun showLicenseDialog() {
        LicensesDialog.Builder(requireContext())
            .setNotices(R.raw.notices)
            .setTitle(R.string.licenses)
            .setNoticesCssStyle(getString(R.string.license_dialog_style)
                .replace("{bg-color}", if (ThemeSingleton.get().darkTheme) "424242" else "ffffff")
                .replace("{text-color}", if (ThemeSingleton.get().darkTheme) "ffffff" else "000000")
                .replace("{license-bg-color}",
                    if (ThemeSingleton.get().darkTheme) "535353" else "eeeeee")
            )
            .setIncludeOwnLicense(true)
            .build()
            .show()
    }
}
