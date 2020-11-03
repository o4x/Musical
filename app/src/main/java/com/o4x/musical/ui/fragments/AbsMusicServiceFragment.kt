package com.o4x.musical.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import java.lang.ClassCastException
import java.lang.RuntimeException

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
open class AbsMusicServiceFragment(@LayoutRes layout: Int) : Fragment(layout), MusicServiceEventListener {
    private var activity: AbsMusicServiceActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = try {
            context as AbsMusicServiceActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(context.javaClass.simpleName + " must be an instance of " + AbsMusicServiceActivity::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMusicServiceEventListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.removeMusicServiceEventListener(this)
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