package com.o4x.musical.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import com.o4x.musical.extensions.findNavController
import com.o4x.musical.R
import com.o4x.musical.extensions.applyToolbar
import com.o4x.musical.ui.activities.base.AbsBaseActivity
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AbsBaseActivity() {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        navController = findNavController(R.id.contentFrame)

        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
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