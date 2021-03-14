package com.o4x.musical.ui.activities.intro

import android.os.Bundle
import com.o4x.musical.databinding.ActivityPermissionBinding
import com.o4x.musical.shared.Permissions
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity

class PermissionActivity : AbsMusicServiceActivity() {

    val binding by lazy { ActivityPermissionBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        setLightNavigationBar(true)
        setTaskDescriptionColorAuto()

        update()

        binding.storagePermission.setOnClickListener {
            requestPermissions()
        }

        binding.finish.setOnClickListener {
            if (hasPermissions()) {
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        update()
    }

    private fun update() {
        binding.finish.isEnabled = Permissions.canReadStorage(this)
    }
}
