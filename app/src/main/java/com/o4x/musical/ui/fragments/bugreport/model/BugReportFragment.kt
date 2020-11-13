package com.o4x.musical.ui.fragments.bugreport.model

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringDef
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import code.name.monkey.appthemehelper.extensions.accentColor
import code.name.monkey.appthemehelper.util.TintHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton.OnVisibilityChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.o4x.musical.R
import com.o4x.musical.misc.DialogAsyncTask
import com.o4x.musical.ui.fragments.bugreport.model.github.ExtraInfo
import com.o4x.musical.ui.fragments.bugreport.model.github.GithubLogin
import com.o4x.musical.ui.fragments.bugreport.model.github.GithubTarget
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.IssueService
import java.io.IOException
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class BugReportFragment : Fragment() {
    @StringDef(
        RESULT_OK,
        RESULT_BAD_CREDENTIALS,
        RESULT_INVALID_TOKEN,
        RESULT_ISSUES_NOT_ENABLED,
        RESULT_UNKNOWN
    )
    @Retention(
        RetentionPolicy.SOURCE
    )
    private annotation class Result

    private var deviceInfo: DeviceInfo? = null

    @JvmField
    @BindView(R.id.input_layout_title)
    var inputLayoutTitle: TextInputLayout? = null

    @JvmField
    @BindView(R.id.input_title)
    var inputTitle: TextInputEditText? = null

    @JvmField
    @BindView(R.id.input_layout_description)
    var inputLayoutDescription: TextInputLayout? = null

    @JvmField
    @BindView(R.id.input_description)
    var inputDescription: TextInputEditText? = null

    @JvmField
    @BindView(R.id.air_textDeviceInfo)
    var textDeviceInfo: TextView? = null

    @JvmField
    @BindView(R.id.input_layout_username)
    var inputLayoutUsername: TextInputLayout? = null

    @JvmField
    @BindView(R.id.input_username)
    var inputUsername: TextInputEditText? = null

    @JvmField
    @BindView(R.id.input_layout_password)
    var inputLayoutPassword: TextInputLayout? = null

    @JvmField
    @BindView(R.id.input_password)
    var inputPassword: TextInputEditText? = null

    @JvmField
    @BindView(R.id.option_use_account)
    var optionUseAccount: RadioButton? = null

    @JvmField
    @BindView(R.id.option_anonymous)
    var optionManual: RadioButton? = null

    @JvmField
    @BindView(R.id.button_send)
    var sendFab: FloatingActionButton? = null
    private var unbinder: Unbinder? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bug_report, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        deviceInfo = DeviceInfo(requireContext())
        textDeviceInfo!!.text = deviceInfo.toString()
    }

    private fun initViews() {
        val themeColor = accentColor()
        optionUseAccount!!.setOnClickListener {
            inputTitle!!.isEnabled = true
            inputDescription!!.isEnabled = true
            inputUsername!!.isEnabled = true
            inputPassword!!.isEnabled = true
            optionManual!!.isChecked = false
            sendFab!!.hide(object : OnVisibilityChangedListener() {
                override fun onHidden(fab: FloatingActionButton) {
                    super.onHidden(fab)
                    sendFab!!.setImageResource(R.drawable.ic_send)
                    sendFab!!.show()
                }
            })
        }
        TintHelper.setTintAuto(optionManual!!, themeColor, false)
        optionManual!!.setOnClickListener {
            inputTitle!!.isEnabled = false
            inputDescription!!.isEnabled = false
            inputUsername!!.isEnabled = false
            inputPassword!!.isEnabled = false
            optionUseAccount!!.isChecked = false
            sendFab!!.hide(object : OnVisibilityChangedListener() {
                override fun onHidden(fab: FloatingActionButton) {
                    super.onHidden(fab)
                    sendFab!!.setImageResource(R.drawable.ic_open_in_browser)
                    sendFab!!.show()
                }
            })
        }
        inputPassword!!.setOnEditorActionListener { textView: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                reportIssue()
                return@setOnEditorActionListener true
            }
            false
        }
        textDeviceInfo!!.setOnClickListener { v: View? -> copyDeviceInfoToClipBoard() }
        TintHelper.setTintAuto(sendFab!!, themeColor, true)
        sendFab!!.setOnClickListener { v: View? -> reportIssue() }
        TintHelper.setTintAuto(inputTitle!!, themeColor, false)
        TintHelper.setTintAuto(inputDescription!!, themeColor, false)
        TintHelper.setTintAuto(inputUsername!!, themeColor, false)
        TintHelper.setTintAuto(inputPassword!!, themeColor, false)
    }

    private fun reportIssue() {
        if (optionUseAccount!!.isChecked) {
            if (!validateInput()) return
            val username = inputUsername!!.text.toString()
            val password = inputPassword!!.text.toString()
            sendBugReport(GithubLogin(username, password))
        } else {
            copyDeviceInfoToClipBoard()
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(ISSUE_TRACKER_LINK)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }
    }

    private fun copyDeviceInfoToClipBoard() {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.device_info), deviceInfo!!.toMarkdown())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            requireContext(),
            R.string.copied_device_info_to_clipboard,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun validateInput(): Boolean {
        var hasErrors = false
        if (optionUseAccount!!.isChecked) {
            if (TextUtils.isEmpty(inputUsername!!.text)) {
                setError(inputLayoutUsername, R.string.bug_report_no_username)
                hasErrors = true
            } else {
                removeError(inputLayoutUsername)
            }
            if (TextUtils.isEmpty(inputPassword!!.text)) {
                setError(inputLayoutPassword, R.string.bug_report_no_password)
                hasErrors = true
            } else {
                removeError(inputLayoutPassword)
            }
        }
        if (TextUtils.isEmpty(inputTitle!!.text)) {
            setError(inputLayoutTitle, R.string.bug_report_no_title)
            hasErrors = true
        } else {
            removeError(inputLayoutTitle)
        }
        if (TextUtils.isEmpty(inputDescription!!.text)) {
            setError(inputLayoutDescription, R.string.bug_report_no_description)
            hasErrors = true
        } else {
            removeError(inputLayoutDescription)
        }
        return !hasErrors
    }

    private fun setError(editTextLayout: TextInputLayout?, @StringRes errorRes: Int) {
        editTextLayout!!.error = getString(errorRes)
    }

    private fun removeError(editTextLayout: TextInputLayout?) {
        editTextLayout!!.error = null
    }

    private fun sendBugReport(login: GithubLogin) {
        if (!validateInput()) return
        val bugTitle = inputTitle!!.text.toString()
        val bugDescription = inputDescription!!.text.toString()
        val report = Report(bugTitle, bugDescription, deviceInfo, ExtraInfo())
        val target = GithubTarget("kabouzeid", "Musical")
        ReportIssueAsyncTask.report(requireActivity(), report, target, login)
    }

    private class ReportIssueAsyncTask private constructor(
        activity: Activity, private val report: Report, private val target: GithubTarget,
        private val login: GithubLogin
    ) : DialogAsyncTask<Void?, Void?, String?>(activity) {
        override fun createDialog(context: Context): Dialog {
            return MaterialDialog(context)
                //                    .progress(true, 0)
                //                    .progressIndeterminateStyle(true)
                .title(R.string.bug_report_uploading)
        }

        @Result
        protected override fun doInBackground(vararg params: Void?): String {
            val client: GitHubClient
            client = if (login.shouldUseApiToken()) {
                GitHubClient().setOAuth2Token(login.apiToken)
            } else {
                GitHubClient().setCredentials(login.username, login.password)
            }
            val issue = Issue().setTitle(report.title).setBody(report.description)
            return try {
                IssueService(client).createIssue(target.username, target.repository, issue)
                RESULT_OK
            } catch (e: RequestException) {
                when (e.status) {
                    STATUS_BAD_CREDENTIALS -> {
                        if (login.shouldUseApiToken()) RESULT_INVALID_TOKEN else RESULT_BAD_CREDENTIALS
                    }
                    STATUS_ISSUES_NOT_ENABLED -> RESULT_ISSUES_NOT_ENABLED
                    else -> {
                        e.printStackTrace()
                        RESULT_UNKNOWN
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                RESULT_UNKNOWN
            }
        }

        protected override fun onPostExecute(@Result result: String?) {
            super.onPostExecute(result)
            val context = context ?: return
            when (result) {
                RESULT_OK -> tryToFinishActivity()
                RESULT_BAD_CREDENTIALS -> MaterialDialog(context)
                    .title(R.string.bug_report_failed)
                    .message(R.string.bug_report_failed_wrong_credentials)
                    .positiveButton(android.R.string.ok)
                    .show()
                RESULT_INVALID_TOKEN -> MaterialDialog(context)
                    .title(R.string.bug_report_failed)
                    .message(R.string.bug_report_failed_invalid_token)
                    .positiveButton(android.R.string.ok)
                    .show()
                RESULT_ISSUES_NOT_ENABLED -> MaterialDialog(context)
                    .title(R.string.bug_report_failed)
                    .message(R.string.bug_report_failed_issues_not_available)
                    .positiveButton(android.R.string.ok)
                    .show()
                else -> MaterialDialog(context)
                    .title(R.string.bug_report_failed)
                    .message(R.string.bug_report_failed_unknown)
                    .positiveButton(android.R.string.ok) {
                        tryToFinishActivity()
                    }
                    .also {
                        it.setOnCancelListener {
                            tryToFinishActivity()
                        }
                    }
                    .show()
            }
        }

        private fun tryToFinishActivity() {
            val context = context
            if (context is Activity && !context.isFinishing) {
                context.finish()
            }
        }

        companion object {
            fun report(
                activity: Activity, report: Report, target: GithubTarget,
                login: GithubLogin
            ) {
                ReportIssueAsyncTask(activity, report, target, login).execute()
            }
        }
    }

    companion object {
        private const val STATUS_BAD_CREDENTIALS = 401
        private const val STATUS_ISSUES_NOT_ENABLED = 410
        private const val RESULT_OK = "RESULT_OK"
        private const val RESULT_BAD_CREDENTIALS = "RESULT_BAD_CREDENTIALS"
        private const val RESULT_INVALID_TOKEN = "RESULT_INVALID_TOKEN"
        private const val RESULT_ISSUES_NOT_ENABLED = "RESULT_ISSUES_NOT_ENABLED"
        private const val RESULT_UNKNOWN = "RESULT_UNKNOWN"
        private const val ISSUE_TRACKER_LINK = "https://github.com/kabouzeid/Musical"
    }
}