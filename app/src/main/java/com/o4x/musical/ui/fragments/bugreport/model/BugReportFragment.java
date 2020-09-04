package com.o4x.musical.ui.fragments.bugreport.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.o4x.musical.R;
import com.o4x.musical.misc.DialogAsyncTask;
import com.o4x.musical.ui.fragments.bugreport.model.github.ExtraInfo;
import com.o4x.musical.ui.fragments.bugreport.model.github.GithubLogin;
import com.o4x.musical.ui.fragments.bugreport.model.github.GithubTarget;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.TintHelper;

import static android.content.Context.CLIPBOARD_SERVICE;

public class BugReportFragment extends Fragment {

    private static final int STATUS_BAD_CREDENTIALS = 401;
    private static final int STATUS_ISSUES_NOT_ENABLED = 410;

    @StringDef({RESULT_OK, RESULT_BAD_CREDENTIALS, RESULT_INVALID_TOKEN, RESULT_ISSUES_NOT_ENABLED,
            RESULT_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Result {
    }

    private static final String RESULT_OK = "RESULT_OK";
    private static final String RESULT_BAD_CREDENTIALS = "RESULT_BAD_CREDENTIALS";
    private static final String RESULT_INVALID_TOKEN = "RESULT_INVALID_TOKEN";
    private static final String RESULT_ISSUES_NOT_ENABLED = "RESULT_ISSUES_NOT_ENABLED";
    private static final String RESULT_UNKNOWN = "RESULT_UNKNOWN";

    private static final String ISSUE_TRACKER_LINK = "https://github.com/kabouzeid/Musical";


    private DeviceInfo deviceInfo;

    @BindView(R.id.input_layout_title)
    TextInputLayout inputLayoutTitle;
    @BindView(R.id.input_title)
    TextInputEditText inputTitle;
    @BindView(R.id.input_layout_description)
    TextInputLayout inputLayoutDescription;
    @BindView(R.id.input_description)
    TextInputEditText inputDescription;
    @BindView(R.id.air_textDeviceInfo)
    TextView textDeviceInfo;

    @BindView(R.id.input_layout_username)
    TextInputLayout inputLayoutUsername;
    @BindView(R.id.input_username)
    TextInputEditText inputUsername;
    @BindView(R.id.input_layout_password)
    TextInputLayout inputLayoutPassword;
    @BindView(R.id.input_password)
    TextInputEditText inputPassword;
    @BindView(R.id.option_use_account)
    RadioButton optionUseAccount;
    @BindView(R.id.option_anonymous)
    RadioButton optionManual;

    @BindView(R.id.button_send)
    FloatingActionButton sendFab;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bug_report, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();

        deviceInfo = new DeviceInfo(requireContext());
        textDeviceInfo.setText(deviceInfo.toString());
    }

    private void initViews() {
        final int themeColor = ThemeStore.Companion.themeColor(getContext());

        optionUseAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputTitle.setEnabled(true);
                inputDescription.setEnabled(true);
                inputUsername.setEnabled(true);
                inputPassword.setEnabled(true);

                optionManual.setChecked(false);
                sendFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        sendFab.setImageResource(R.drawable.ic_send_white_24dp);
                        sendFab.show();
                    }
                });
            }
        });
        TintHelper.setTintAuto(optionManual, themeColor, false);
        optionManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputTitle.setEnabled(false);
                inputDescription.setEnabled(false);
                inputUsername.setEnabled(false);
                inputPassword.setEnabled(false);

                optionUseAccount.setChecked(false);
                sendFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        sendFab.setImageResource(R.drawable.ic_open_in_browser_white_24dp);
                        sendFab.show();
                    }
                });
            }
        });

        inputPassword.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                reportIssue();
                return true;
            }
            return false;
        });

        textDeviceInfo.setOnClickListener(v -> copyDeviceInfoToClipBoard());

        TintHelper.setTintAuto(sendFab, themeColor, true);
        sendFab.setOnClickListener(v -> reportIssue());

        TintHelper.setTintAuto(inputTitle, themeColor, false);
        TintHelper.setTintAuto(inputDescription, themeColor, false);
        TintHelper.setTintAuto(inputUsername, themeColor, false);
        TintHelper.setTintAuto(inputPassword, themeColor, false);
    }

    private void reportIssue() {
        if (optionUseAccount.isChecked()) {
            if (!validateInput()) return;
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();
            sendBugReport(new GithubLogin(username, password));
        } else {
            copyDeviceInfoToClipBoard();

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(ISSUE_TRACKER_LINK));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    private void copyDeviceInfoToClipBoard() {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.device_info), deviceInfo.toMarkdown());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(requireContext(), R.string.copied_device_info_to_clipboard, Toast.LENGTH_LONG).show();
    }

    private boolean validateInput() {
        boolean hasErrors = false;

        if (optionUseAccount.isChecked()) {
            if (TextUtils.isEmpty(inputUsername.getText())) {
                setError(inputLayoutUsername, R.string.bug_report_no_username);
                hasErrors = true;
            } else {
                removeError(inputLayoutUsername);
            }

            if (TextUtils.isEmpty(inputPassword.getText())) {
                setError(inputLayoutPassword, R.string.bug_report_no_password);
                hasErrors = true;
            } else {
                removeError(inputLayoutPassword);
            }
        }

        if (TextUtils.isEmpty(inputTitle.getText())) {
            setError(inputLayoutTitle, R.string.bug_report_no_title);
            hasErrors = true;
        } else {
            removeError(inputLayoutTitle);
        }

        if (TextUtils.isEmpty(inputDescription.getText())) {
            setError(inputLayoutDescription, R.string.bug_report_no_description);
            hasErrors = true;
        } else {
            removeError(inputLayoutDescription);
        }

        return !hasErrors;
    }

    private void setError(TextInputLayout editTextLayout, @StringRes int errorRes) {
        editTextLayout.setError(getString(errorRes));
    }

    private void removeError(TextInputLayout editTextLayout) {
        editTextLayout.setError(null);
    }

    private void sendBugReport(GithubLogin login) {
        if (!validateInput()) return;

        String bugTitle = inputTitle.getText().toString();
        String bugDescription = inputDescription.getText().toString();

        com.o4x.musical.ui.fragments.bugreport.model.Report report = new com.o4x.musical.ui.fragments.bugreport.model.Report(bugTitle, bugDescription, deviceInfo, new ExtraInfo());
        GithubTarget target = new GithubTarget("kabouzeid", "Musical");

        ReportIssueAsyncTask.report(requireActivity(), report, target, login);
    }

    private static class ReportIssueAsyncTask extends DialogAsyncTask<Void, Void, String> {
        private final com.o4x.musical.ui.fragments.bugreport.model.Report report;
        private final GithubTarget target;
        private final GithubLogin login;

        public static void report(Activity activity, com.o4x.musical.ui.fragments.bugreport.model.Report report, GithubTarget target,
                                  GithubLogin login) {
            new ReportIssueAsyncTask(activity, report, target, login).execute();
        }

        private ReportIssueAsyncTask(Activity activity, Report report, GithubTarget target,
                                     GithubLogin login) {
            super(activity);
            this.report = report;
            this.target = target;
            this.login = login;
        }

        @Override
        protected Dialog createDialog(@NonNull Context context) {
            return new MaterialDialog.Builder(context)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .title(R.string.bug_report_uploading)
                    .show();
        }

        @Override
        @Result
        protected String doInBackground(Void... params) {
            GitHubClient client;
            if (login.shouldUseApiToken()) {
                client = new GitHubClient().setOAuth2Token(login.getApiToken());
            } else {
                client = new GitHubClient().setCredentials(login.getUsername(), login.getPassword());
            }

            Issue issue = new Issue().setTitle(report.getTitle()).setBody(report.getDescription());
            try {
                new IssueService(client).createIssue(target.getUsername(), target.getRepository(), issue);
                return RESULT_OK;
            } catch (RequestException e) {
                switch (e.getStatus()) {
                    case STATUS_BAD_CREDENTIALS:
                        if (login.shouldUseApiToken())
                            return RESULT_INVALID_TOKEN;
                        return RESULT_BAD_CREDENTIALS;
                    case STATUS_ISSUES_NOT_ENABLED:
                        return RESULT_ISSUES_NOT_ENABLED;
                    default:
                        e.printStackTrace();
                        return RESULT_UNKNOWN;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return RESULT_UNKNOWN;
            }
        }

        @Override
        protected void onPostExecute(@Result String result) {
            super.onPostExecute(result);

            Context context = getContext();
            if (context == null) return;

            switch (result) {
                case RESULT_OK:
                    tryToFinishActivity();
                    break;
                case RESULT_BAD_CREDENTIALS:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_wrong_credentials)
                            .positiveText(android.R.string.ok)
                            .show();
                    break;
                case RESULT_INVALID_TOKEN:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_invalid_token)
                            .positiveText(android.R.string.ok)
                            .show();
                    break;
                case RESULT_ISSUES_NOT_ENABLED:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_issues_not_available)
                            .positiveText(android.R.string.ok)
                            .show();
                    break;
                default:
                    new MaterialDialog.Builder(context)
                            .title(R.string.bug_report_failed)
                            .content(R.string.bug_report_failed_unknown)
                            .positiveText(android.R.string.ok)
                            .onPositive((dialog, which) -> tryToFinishActivity())
                            .cancelListener(dialog -> tryToFinishActivity())
                            .show();
                    break;
            }
        }

        private void tryToFinishActivity() {
            Context context = getContext();
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                ((Activity) context).finish();
            }
        }
    }
}
