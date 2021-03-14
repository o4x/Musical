package com.o4x.musical.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import com.o4x.appthemehelper.util.ColorUtil.isColorLight
import com.o4x.musical.R
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import com.o4x.musical.ui.fragments.player.PlayerFragment

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
        setNavigationBarColor(Color.TRANSPARENT)
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
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}