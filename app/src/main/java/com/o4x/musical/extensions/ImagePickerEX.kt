package com.o4x.musical.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.FileUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.o4x.musical.App.Companion.getContext
import java.io.File

private val dir = File(getContext().filesDir, "/temp/")

fun AppCompatActivity.startImagePicker(requestCode: Int) {
    // for clean
    FileUtils.delete(dir)

    ImagePicker.with(this)
        .saveDir(dir)
        .crop()
        .cropSquare()
        .maxResultSize(2048, 2048)
        .start(requestCode)
}

fun Fragment.startImagePicker(requestCode: Int) {
    // for clean
    FileUtils.delete(dir)

    ImagePicker.with(this)
        .saveDir(dir)
        .crop()
        .cropSquare()
        .maxResultSize(2048, 2048)
        .start(requestCode)
}

private val headerDir = File(getContext().filesDir, "/home_header/")

fun Fragment.startHomeHeaderImagePicker(requestCode: Int) {
    // for clean
    FileUtils.delete(headerDir)

    ImagePicker.with(this)
        .saveDir(headerDir)
        .crop()
        .start(requestCode)
}