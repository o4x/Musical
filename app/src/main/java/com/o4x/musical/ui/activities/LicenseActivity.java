package com.o4x.musical.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.o4x.musical.R;
import com.o4x.musical.ui.activities.base.AbsBaseActivity;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.o4x.appthemehelper.ThemeStore;
import com.o4x.appthemehelper.extensions.ColorExtKt;
import com.o4x.appthemehelper.util.ATHUtil;
import com.o4x.appthemehelper.util.ColorUtil;
import com.o4x.appthemehelper.util.ToolbarContentTintHelper;

public class LicenseActivity extends AbsBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setDrawUnderStatusBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        setStatusBarColorAuto();
        setNavigationBarColorAuto();
        setLightNavigationBar(true);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ToolbarContentTintHelper.colorBackButton(toolbar);
        toolbar.setBackgroundColor(ATHUtil.INSTANCE.resolveColor(this, R.attr.colorSurface));
        WebView webView = findViewById(R.id.license);
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = getAssets().open("oldindex.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();

            // Inject color values for WebView body background and links
            final boolean isDark = ATHUtil.INSTANCE.isWindowBackgroundDark(this);
            final String backgroundColor = colorToCSS(ATHUtil.INSTANCE.resolveColor(this, R.attr.colorSurface,
                    Color.parseColor(isDark ? "#424242" : "#ffffff")));
            final String contentColor = colorToCSS(Color.parseColor(isDark ? "#ffffff" : "#000000"));
            final String changeLog = buf.toString()
                    .replace("{style-placeholder}",
                            String.format("body { background-color: %s; color: %s; }", backgroundColor, contentColor))
                    .replace("{link-color}", colorToCSS(ColorExtKt.accentColor(this)))
                    .replace("{link-color-active}",
                            colorToCSS(ColorUtil.INSTANCE.lightenColor(ColorExtKt.accentColor(this))));

            webView.loadData(changeLog, "text/html", "UTF-8");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String colorToCSS(int color) {
        return String.format("rgb(%d, %d, %d)", Color.red(color), Color.green(color),
                Color.blue(color)); // on API 29, WebView doesn't load with hex colors
    }
}
