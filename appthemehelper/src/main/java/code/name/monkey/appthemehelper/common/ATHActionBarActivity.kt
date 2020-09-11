package code.name.monkey.appthemehelper.common

import androidx.appcompat.widget.Toolbar

import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper

abstract class ATHActionBarActivity : ATHToolbarActivity() {

    override fun getATHToolbar(): Toolbar? {
        return ToolbarContentTintHelper.getSupportActionBarView(supportActionBar)
    }
}
