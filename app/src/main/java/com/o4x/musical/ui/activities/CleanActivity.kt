package com.o4x.musical.ui.activities

import android.os.Bundle
import com.o4x.musical.R
import com.o4x.musical.databinding.ActivityCleanBinding
import com.o4x.musical.ui.activities.base.AbsBaseActivity

class CleanActivity : AbsBaseActivity() {

    val binding by lazy { ActivityCleanBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setTitle(R.string.musical_clean)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}