package github.o4x.m2.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem

import github.o4x.m2.R
import github.o4x.m2.ui.activities.base.AbsMusicServiceActivity
import github.o4x.m2.ui.fragments.player.PlayerFragment

class PlayerActivity : AbsMusicServiceActivity() {

    lateinit var playerFragment: PlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_player)

        playerFragment = PlayerFragment()
        // Don't force the transaction to run synchronously here — the framework
        // executes it before onStart, so the fragment is ready in time. Forcing it
        // adds an extra synchronous inflate/layout pass just as the open animation
        // starts, which shows up as jank.
        supportFragmentManager.beginTransaction()
            .replace(R.id.player_fragment_container, playerFragment).commit()
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