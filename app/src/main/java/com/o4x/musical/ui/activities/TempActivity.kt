package com.o4x.musical.ui.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.o4x.musical.R
import kotlinx.android.synthetic.main.image.*

class TempActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp)
        val bmp: Bitmap

        val byteArray = intent.getByteArrayExtra("image")
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        image.setImageBitmap(bmp);
    }
}