package com.kabouzeid.appthemehelper.common.prefs.supportv7;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import com.kabouzeid.appthemehelper.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATEDialogPreference extends DialogPreference {

    public ATEDialogPreference(Context context) {
        super(context);
        init(context, null);
    }

    public ATEDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATEDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ATEDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.ate_preference_custom_support);
    }
}