package github.o4x.musical.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import github.o4x.musical.interfaces.MusicServiceEventListener
import github.o4x.musical.ui.activities.MainActivity
import github.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import java.lang.ClassCastException
import java.lang.RuntimeException


open class AbsMusicServiceFragment(@LayoutRes layout: Int) : Fragment(layout), MusicServiceEventListener {

    val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }
    open val libraryViewModel by lazy { mainActivity.libraryViewModel }
    val navController by lazy { mainActivity.navController }
    lateinit var serviceActivity: AbsMusicServiceActivity
    val playerViewModel by lazy { serviceActivity.playerViewModel }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        serviceActivity = try {
            context as AbsMusicServiceActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(context.javaClass.simpleName + " must be an instance of " + AbsMusicServiceActivity::class.java.simpleName)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceActivity.addMusicServiceEventListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        serviceActivity.removeMusicServiceEventListener(this)
    }

    override fun onPlayingMetaChanged() {}
    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
    override fun onMediaStoreChanged() {}
}