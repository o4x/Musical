package com.o4x.musical.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.o4x.musical.R;

public class SetTagsDialog extends DialogFragment {

    private final On on;

    public SetTagsDialog(On on) {
        this.on = on;
    }

    public static class On {
        protected void allTags() {};
        protected void justImage() {};
    }


    public static SetTagsDialog create(On on) {
        return new SetTagsDialog(on);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.download_tags)
                        .content(R.string.download_tags_message)
                        .neutralText(android.R.string.cancel)
                        .negativeText(R.string.just_image)
                        .positiveText(R.string.all_tags)
                        .onNeutral((dialog, which) -> {})
                        .onNegative((dialog, which) -> on.justImage())
                        .onPositive((dialog, which) -> on.allTags());
        return builder.build();
    }
}
