package com.o4x.musical.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.o4x.musical.R;

public class SetTagsDialog extends DialogFragment {


    public static SetTagsDialog create() {
        return new SetTagsDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.download_tags)
                        .content(R.string.download_tags_message)
                        .neutralText(android.R.string.cancel)
                        .negativeText("Just image")
                        .positiveText("All tags")
                        .onNeutral((dialog, which) -> {})
                        .onNegative((dialog, which) -> {})
                        .onPositive((dialog, which) -> {});
        return builder.build();
    }
}
