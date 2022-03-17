package github.o4x.musical.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import github.o4x.musical.extensions.findNavController
import github.o4x.musical.R
import github.o4x.musical.databinding.ActivitySettingsBinding
import github.o4x.musical.extensions.applyToolbar
import github.o4x.musical.ui.activities.base.AbsBaseActivity


class SettingsActivity : AbsBaseActivity() {

    lateinit var navController: NavController

    private val binding by lazy { ActivitySettingsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        navController = findNavController(R.id.contentFrame)

        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        setLightNavigationBar(true)
        setupToolbar()
    }

    private fun setupToolbar() {
        applyToolbar(binding.toolbar)
        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.toolbar.title = navController.currentDestination?.label
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
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }
}