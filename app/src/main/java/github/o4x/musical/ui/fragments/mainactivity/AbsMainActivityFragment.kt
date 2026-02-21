package github.o4x.musical.ui.fragments.mainactivity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import github.o4x.musical.ui.activities.MainActivity
import github.o4x.musical.ui.activities.MainActivity.MainActivityFragmentCallbacks
import github.o4x.musical.ui.fragments.AbsMusicServiceFragment

abstract class AbsMainActivityFragment(@LayoutRes layout: Int) :
    AbsMusicServiceFragment(layout), MainActivityFragmentCallbacks {

    val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }

    val libraryViewModel by lazy { mainActivity.libraryViewModel }
    val navController by lazy { mainActivity.navController }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mainActivity.backPressCallbacks.add(this)
    }

    override fun onPause() {
        super.onPause()
        mainActivity.backPressCallbacks.remove(this)
    }

    override fun handleBackPress(): Boolean {
        return false
    }
}
