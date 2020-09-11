package com.o4x.musical.ui.activities

import android.R.attr.bitmap
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.retromusic.extensions.findNavController
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.o4x.musical.R
import com.o4x.musical.appshortcuts.DynamicShortcutManager
import com.o4x.musical.extensions.applyToolbar
import com.o4x.musical.ui.activities.base.AbsBaseActivity
import com.o4x.musical.util.ViewUtil
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.ByteArrayOutputStream


class SettingsActivity : AbsBaseActivity(), ColorChooserDialog.ColorCallback {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        navController = findNavController(R.id.contentFrame)

        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setLightNavigationBar(true)
        setupToolbar()
    }

    private fun setupToolbar() {
        applyToolbar(toolbar)
        navController.addOnDestinationChangedListener { _, _, _ ->
            toolbar.title = navController.currentDestination?.label
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onColorSelection(dialog: ColorChooserDialog, selectedColor: Int) {
        when (dialog.title) {
            R.string.theme_color -> {
                if (ThemeStore.themeColor(this) != selectedColor)
                ThemeStore.editTheme(this).themeColor(selectedColor).commit()
            }
        }
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (navController.currentDestination?.id == R.id.mainSettings) {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateTheme() {
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}