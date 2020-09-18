package com.o4x.musical.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.o4x.musical.R;

public class DiscardTagsDialog extends DialogFragment {


    public static DiscardTagsDialog create() {
        return new DiscardTagsDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.discard_changes)
                        .neutralText(android.R.string.cancel)
                        .positiveText(android.R.string.ok)
                        .onNeutral((dialog, which) -> {})
                        .onPositive((dialog, which) -> getActivity().finish());
        return builder.build();
    }

}
