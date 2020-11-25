package com.o4x.musical.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.ColorInt
import code.name.monkey.appthemehelper.util.ColorUtil.isColorLight
import com.o4x.musical.R
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import com.o4x.musical.ui.fragments.player.PlayerFragment

class PlayerActivity : AbsMusicServiceActivity() {

    private var navigationBarColor = 0
    private var taskColor = 0
    private var lightStatusBar = false
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

    override fun setNavigationBarColor(color: Int) {
        navigationBarColor = color
        super.setNavigationBarColor(color)
    }

    override fun setLightStatusBar(enabled: Boolean) {
        lightStatusBar = enabled
        super.setLightStatusBar(enabled)
    }

    override fun setTaskDescriptionColor(@ColorInt color: Int) {
        taskColor = color
        super.setTaskDescriptionColor(color)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}