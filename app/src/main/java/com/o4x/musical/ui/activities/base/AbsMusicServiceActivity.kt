package com.o4x.musical.ui.activities.base

import android.Manifest
import android.content.*
import android.os.Bundle
import android.os.IBinder
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote.ServiceToken
import com.o4x.musical.helper.MusicPlayerRemote.bindToService
import com.o4x.musical.helper.MusicPlayerRemote.unbindFromService
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.viewmodel.PlayerViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsMusicServiceActivity : AbsBaseActivity(), MusicServiceEventListener {

    private val mMusicServiceEventListeners: MutableList<MusicServiceEventListener> = ArrayList()
    private var serviceToken: ServiceToken? = null
    private var musicStateReceiver: MusicStateReceiver? = null
    private var receiverRegistered = false

    val playerViewModel by viewModel<PlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceToken = bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                this@AbsMusicServiceActivity.onServiceConnected()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@AbsMusicServiceActivity.onServiceDisconnected()
            }
        })
        setPermissionDeniedMessage(getString(R.string.permission_external_storage_denied))
        addMusicServiceEventListener(playerViewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindFromService(serviceToken)
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
        removeMusicServiceEventListener(playerViewModel)
    }

    fun addMusicServiceEventListener(listener: MusicServiceEventListener?) {
        if (listener != null) {
            mMusicServiceEventListeners.add(listener)
        }
    }

    fun removeMusicServiceEventListener(listener: MusicServiceEventListener?) {
        if (listener != null) {
            mMusicServiceEventListeners.remove(listener)
        }
    }

    override fun onServiceConnected() {
        if (!receiverRegistered) {
            musicStateReceiver = MusicStateReceiver(this)
            val filter = IntentFilter()
            filter.addAction(MusicService.PLAY_STATE_CHANGED)
            filter.addAction(MusicService.SHUFFLE_MODE_CHANGED)
            filter.addAction(MusicService.REPEAT_MODE_CHANGED)
            filter.addAction(MusicService.META_CHANGED)
            filter.addAction(MusicService.QUEUE_CHANGED)
            filter.addAction(MusicService.MEDIA_STORE_CHANGED)
            registerReceiver(musicStateReceiver, filter)
            receiverRegistered = true
        }
        for (listener in mMusicServiceEventListeners) {
            listener.onServiceConnected()
        }
    }

    override fun onServiceDisconnected() {
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
        for (listener in mMusicServiceEventListeners) {
            listener.onServiceDisconnected()
        }
    }

    override fun onPlayingMetaChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged()
        }
    }

    override fun onQueueChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onQueueChanged()
        }
    }

    override fun onPlayStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayStateChanged()
        }
    }

    override fun onMediaStoreChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onMediaStoreChanged()
        }
    }

    override fun onRepeatModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onRepeatModeChanged()
        }
    }

    override fun onShuffleModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onShuffleModeChanged()
        }
    }

    private class MusicStateReceiver(activity: AbsMusicServiceActivity) : BroadcastReceiver() {

        private val reference: WeakReference<AbsMusicServiceActivity> = WeakReference(activity)

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val activity = reference.get()
            if (activity != null) {
                when (action) {
                    MusicService.META_CHANGED -> activity.onPlayingMetaChanged()
                    MusicService.QUEUE_CHANGED -> activity.onQueueChanged()
                    MusicService.PLAY_STATE_CHANGED -> activity.onPlayStateChanged()
                    MusicService.REPEAT_MODE_CHANGED -> activity.onRepeatModeChanged()
                    MusicService.SHUFFLE_MODE_CHANGED -> activity.onShuffleModeChanged()
                    MusicService.MEDIA_STORE_CHANGED -> activity.onMediaStoreChanged()
                }
            }
        }

    }

    override fun onHasPermissionsChanged(hasPermissions: Boolean) {
        super.onHasPermissionsChanged(hasPermissions)
        val intent = Intent(MusicService.MEDIA_STORE_CHANGED)
        intent.putExtra(
            "from_permissions_changed",
            true
        ) // just in case we need to know this at some point
        sendBroadcast(intent)
    }

    override fun getPermissionsToRequest(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}