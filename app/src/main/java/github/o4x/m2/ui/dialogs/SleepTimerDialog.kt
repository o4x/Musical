package github.o4x.m2.ui.dialogs

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import github.o4x.m2.R
import github.o4x.m2.databinding.DialogSleepTimerBinding
import github.o4x.m2.helper.MusicPlayerRemote
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.service.MusicService
import github.o4x.m2.views.timepicker.HmsPicker

class SleepTimerDialog : AbsBlurDialogFragment() {

    private var timerUpdater: TimerUpdater? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var _binding: DialogSleepTimerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val materialDialog = MaterialDialog(requireContext())
            .title(R.string.sleep_timer)
            .customView(R.layout.dialog_sleep_timer, horizontalPadding = true)
            .negativeButton(R.string.cancel)

        _binding = DialogSleepTimerBinding.bind(materialDialog.getCustomView())

        binding.shouldFinishLastSong.isChecked = PreferenceUtil.sleepTimerFinishMusic

        val nextSleepTimerElapsed =
            PreferenceUtil.nextSleepTimerElapsedRealTime - SystemClock.elapsedRealtime()
        if (
            makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE) != null
            && nextSleepTimerElapsed > 0 && PreferenceUtil.isSleepTimerEnable
        ) {
            binding.timePicker.setMillis(nextSleepTimerElapsed, false)
            timerUpdater = TimerUpdater(nextSleepTimerElapsed)
            timerUpdater?.start()
            materialDialog.positiveButton(R.string.stop) { stopTimer() }
        } else {
            binding.timePicker.setMillis(PreferenceUtil.lastSleepTimerValue, false)
            materialDialog.positiveButton(R.string.start) { startTimer() }
        }

        return materialDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        timerUpdater = null
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun startTimer() {
        val millis = binding.timePicker.getMillis()
        PreferenceUtil.lastSleepTimerValue = millis
        PreferenceUtil.sleepTimerFinishMusic = binding.shouldFinishLastSong.isChecked

        val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
        val nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + millis
        PreferenceUtil.setNextSleepTimerElapsedRealtime(nextSleepTimerElapsedTime)
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pi?.let {
            am[AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime] = it
        }

        PreferenceUtil.isSleepTimerEnable = true
    }

    private fun stopTimer() {
        val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
        if (previous != null) {
            val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(previous)
            previous.cancel()
        }
        val musicService = MusicPlayerRemote.musicService
        if (musicService != null && musicService.pendingQuit) {
            musicService.pendingQuit = false
        }

        PreferenceUtil.isSleepTimerEnable = false
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(
            requireContext(), 0, makeTimerIntent(), flag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(requireContext(), MusicService::class.java)
        return if (binding.shouldFinishLastSong.isChecked) {
            intent.setAction(MusicService.ACTION_PENDING_QUIT)
        } else intent.setAction(MusicService.ACTION_QUIT)
    }

    private fun HmsPicker.getMillis(): Long {
        var millis = 0L
        millis += seconds * 1000
        millis += minutes * 1000 * 60
        millis += hours * 1000 * 60 * 60
        return millis
    }

    private fun HmsPicker.setMillis(m: Long, smooth: Boolean) {
        val seconds = ((m / 1000) % 60).toInt()
        val minutes = ((m / 1000 / 60) % 60).toInt()
        val hours = ((m / 1000 / 60 / 60) % 24).toInt()

        this.setTime(hours, minutes, seconds, smooth)
    }

    private inner class TimerUpdater(millisInFuture: Long) :
        CountDownTimer(millisInFuture, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            _binding?.timePicker?.setMillis(millisUntilFinished, true)
        }

        override fun onFinish() {
            dismissAllowingStateLoss()
        }
    }

    companion object {
        @JvmStatic
        fun create(): SleepTimerDialog {
            return SleepTimerDialog()
        }
    }
}
