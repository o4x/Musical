package com.kabouzeid.appthemehelper.common.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressLint("AppCompatCustomView")
public class ATESecondaryTextView extends TextView {

    public ATESecondaryTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ATESecondaryTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATESecondaryTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ATESecondaryTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setTextColor(ThemeStore.textColorSecondary(context));
    }
}
