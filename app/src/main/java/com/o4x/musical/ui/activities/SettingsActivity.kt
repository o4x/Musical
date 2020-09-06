package com.o4x.musical.ui.activities

import android.R.attr.bitmap
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setLightNavigationBar(true)
        setupToolbar()
    }

    private fun setupToolbar() {
        applyToolbar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val navController: NavController = findNavController(R.id.contentFrame)
        val animation: Animation = AnimationUtils.loadAnimation(applicationContext,
            R.anim.slide_in_left)
        navController.addOnDestinationChangedListener { _, _, _ ->
            toolbar_title.startAnimation(animation)
            toolbar_title.text = navController.currentDestination?.label
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.contentFrame).navigateUp() || super.onSupportNavigateUp()
    }

    override fun onColorSelection(dialog: ColorChooserDialog, selectedColor: Int) {
        when (dialog.title) {
            R.string.theme_color -> {
                ThemeStore.editTheme(this).themeColor(selectedColor).commit()
                if (VersionUtils.hasNougatMR())
                    DynamicShortcutManager(this).updateDynamicShortcuts()
            }
        }
        recreate()
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (findNavController(R.id.contentFrame).currentDestination?.id == R.id.mainSettings) {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}