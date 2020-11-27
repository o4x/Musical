package com.o4x.musical.ui.fragments.mainactivity.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentTimerBinding
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.views.timepicker.ScrollHmsPicker

class TimerFragment: AbsMainActivityFragment(R.layout.fragment_timer) {

    private var timerUpdater: TimerUpdater? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        setAppbarPadding(binding.root)
        return binding.root
    }

    override fun onDestroyView() {
        timerUpdater?.cancel()
        timerUpdater = null
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.start.setOnClickListener {

            PreferenceUtil.sleepTimerFinishMusic =
                binding.shouldFinishLastSong.isChecked

            val millis = binding.timePicker.getMillis()
            val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
            val nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + millis
            PreferenceUtil.setNextSleepTimerElapsedRealtime(nextSleepTimerElapsedTime)
            val am = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am[AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime] = pi

            PreferenceUtil.lastSleepTimerValue = millis
            timerUpdater = TimerUpdater(millis)
            timerUpdater?.start()
        }

        binding.stop.setOnClickListener {
            val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
            if (previous != null) {
                val am = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.cancel(previous)
                previous.cancel()
            }
            val musicService = MusicPlayerRemote.musicService
            if (musicService != null && musicService.pendingQuit) {
                musicService.pendingQuit = false
            }

            Toast.makeText(
                activity,
                requireActivity().resources.getString(R.string.sleep_timer_canceled),
                Toast.LENGTH_SHORT
            ).show()

            timerUpdater?.cancel()
        }


        if (makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE) != null) {
            val nextSleepTimerElapsed =
                PreferenceUtil.nextSleepTimerElapsedRealTime - SystemClock.elapsedRealtime()
            binding.timePicker.setMillis(nextSleepTimerElapsed, false)
            timerUpdater = TimerUpdater(nextSleepTimerElapsed)
            timerUpdater?.start()
        } else {
            binding.timePicker.setMillis(PreferenceUtil.lastSleepTimerValue, false)
        }

        val finishMusic = PreferenceUtil.sleepTimerFinishMusic
        binding.shouldFinishLastSong.isChecked = finishMusic
        binding.timePicker.setMillis(PreferenceUtil.lastSleepTimerValue, false)
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(activity, 0, makeTimerIntent(), flag)
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(activity, MusicService::class.java)
        return if (binding.shouldFinishLastSong.isChecked) {
            intent.setAction(MusicService.ACTION_PENDING_QUIT)
        } else intent.setAction(MusicService.ACTION_QUIT)
    }

    private fun ScrollHmsPicker.getMillis(): Long {
        var millis = 0L
        millis += seconds * 1000
        millis += minutes * 1000 * 60
        millis += hours * 1000 * 60 * 60
        return millis
    }

    private fun ScrollHmsPicker.setMillis(m: Long, smooth: Boolean) {
        val seconds = ((m / 1000) % 60).toInt()
        val minutes = ((m / 1000 / 60) % 60).toInt()
        val hours = ((m / 1000 / 60 / 60) % 24).toInt()

        binding.timePicker.setTime(
            hours, minutes, seconds, smooth)
    }

    private inner class TimerUpdater(millisInFuture: Long) :
        CountDownTimer(millisInFuture, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            binding.timePicker.setMillis(millisUntilFinished, true)
        }

        override fun onFinish() {
//            updateCancelButton()
        }
    }
}