package github.o4x.musical.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback

import github.o4x.musical.R
import github.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import github.o4x.musical.ui.fragments.player.PlayerFragment

class PlayerActivity : AbsMusicServiceActivity() {

    lateinit var playerFragment: PlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_player)
        setDrawUnderBar()

        playerFragment = PlayerFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.player_fragment_container, playerFragment).commit()
        supportFragmentManager.executePendingTransactions()
        setLightStatusBar(false)
        setNavigationBarColor(Color.TRANSPARENT, force = true)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })
    }

    override fun setTheme() {
        setTheme(R.style.Theme_Musical_Black)
    }

    override fun onStart() {
        super.onStart()
        // setting fragments values
        playerFragment.setMenuVisibility(true)
        playerFragment.userVisibleHint = true
    }

    override fun onStop() {
        super.onStop()
        playerFragment.setMenuVisibility(false)
        playerFragment.userVisibleHint = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}