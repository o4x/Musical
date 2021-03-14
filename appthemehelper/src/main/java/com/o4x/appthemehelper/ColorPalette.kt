package com.o4x.appthemehelper

/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Color.parseColor
import androidx.preference.PreferenceManager


class ColorPalette(context: Context) {

        val randomColors: IntArray = context.resources.getIntArray(R.array.random_colors)

        val materialColors: Array<IntArray> = arrayOf(
                R.array.material_red,
                R.array.material_pink,
                R.array.material_purple,
                R.array.material_deep_purple,
                R.array.material_indigo,
                R.array.material_blue,
                R.array.material_light_blue,
                R.array.material_cyan,
                R.array.material_teal,
                R.array.material_green,
                R.array.material_light_green,
                R.array.material_lime,
                R.array.material_yellow,
                R.array.material_amber,
                R.array.material_orange,
                R.array.material_deep_orange,
                R.array.material_brown,
                R.array.material_gray,
                R.array.material_blue_gray)
                .map { res ->
                        context.resources.getIntArray(res)
                }.toTypedArray()

        val materialColorsPrimary: IntArray = materialColors.map { colorList -> colorList[6] }.toIntArray()
}