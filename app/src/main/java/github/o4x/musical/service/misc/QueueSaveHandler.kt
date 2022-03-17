package github.o4x.musical.service.misc

import android.os.Handler
import android.os.Looper
import android.os.Message
import github.o4x.musical.service.MusicService
import java.lang.ref.WeakReference

class QueueSaveHandler(service: MusicService, looper: Looper) : Handler(looper) {
    private val mService: WeakReference<MusicService> = WeakReference(service)

    override fun handleMessage(msg: Message) {
        val service = mService.get()
        when (msg.what) {
            MusicService.SAVE_QUEUES -> service!!.saveQueuesImpl()
        }
    }
}