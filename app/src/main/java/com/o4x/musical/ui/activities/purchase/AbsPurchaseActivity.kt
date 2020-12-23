package com.o4x.musical.ui.activities.purchase

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import code.name.monkey.appthemehelper.extensions.accentColor
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.databinding.ActivityPurchaseBinding
import com.o4x.musical.ui.activities.base.AbsBaseActivity

open class AbsPurchaseActivity : AbsBaseActivity() {

    val binding by lazy { ActivityPurchaseBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setStatusBarColor(Color.TRANSPARENT)
        setLightStatusBar(false)
        setNavigationBarColor(Color.BLACK)
        setLightNavigationBar(false)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.navigationIcon?.setTint(Color.WHITE)

        binding.container.backgroundTintList =
            ColorStateList.valueOf(accentColor())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun toastPurchaseHistoryRestored() {
        if (App.isCleanVersion()) {
            Toast.makeText(
                this,
                R.string.restored_previous_purchase_please_restart,
                Toast.LENGTH_LONG
            ).show()
            setResult(RESULT_OK)
        } else {
            Toast.makeText(this, R.string.no_purchase_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun toastProductPurchased() {
        Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show()
    }

    fun toastRestoringPurchase() {
        Toast.makeText(this, R.string.restoring_purchase, Toast.LENGTH_SHORT)
            .show()
    }

    fun toastCantRestorePurchase() {
        Toast.makeText(
            this,
            R.string.could_not_restore_purchase,
            Toast.LENGTH_SHORT
        ).show()
    }
}