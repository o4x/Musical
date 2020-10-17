package com.o4x.musical.ui.dialogs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.o4x.musical.App.Companion.isProVersion
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote.musicService
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.activities.PurchaseActivity
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil.lastSleepTimerValue
import com.o4x.musical.util.PreferenceUtil.nextSleepTimerElapsedRealTime
import com.o4x.musical.util.PreferenceUtil.setNextSleepTimerElapsedRealtime
import com.o4x.musical.util.PreferenceUtil.sleepTimerFinishMusic
import com.triggertrap.seekarc.SeekArc
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener
import kotlin.math.min

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class SleepTimerDialog : DialogFragment() {
    @BindView(R.id.seek_arc)
    lateinit var seekArc: SeekArc
    @BindView(R.id.timer_display)
    lateinit var timerDisplay: TextView
    @BindView(R.id.should_finish_last_song)
    lateinit var shouldFinishLastSong: CheckBox

    private var seekArcProgress = 0

    private lateinit var materialDialog: MaterialDialog
    private lateinit var timerUpdater: TimerUpdater

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        timerUpdater.cancel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        materialDialog = MaterialDialog(requireContext())
            .title(R.string.action_sleep_timer)
            .positiveButton(R.string.action_set) {
                if (activity == null) {
                    return@positiveButton
                }
                if (!isProVersion()) {
                    Toast.makeText(
                        activity,
                        getString(R.string.sleep_timer_is_a_pro_feature),
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(context, PurchaseActivity::class.java))
                    return@positiveButton
                }
                sleepTimerFinishMusic = shouldFinishLastSong.isChecked
                val minutes = seekArcProgress
                val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
                val nextSleepTimerElapsedTime = SystemClock.elapsedRealtime() + minutes * 60 * 1000
                setNextSleepTimerElapsedRealtime(nextSleepTimerElapsedTime)
                val am = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am[AlarmManager.ELAPSED_REALTIME_WAKEUP, nextSleepTimerElapsedTime] = pi
                Toast.makeText(
                    activity,
                    requireActivity().resources.getString(R.string.sleep_timer_set, minutes),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .neutralButton {
                if (activity == null) {
                    return@neutralButton
                }
                val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
                if (previous != null) {
                    val am = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    am.cancel(previous)
                    previous.cancel()
                    Toast.makeText(
                        activity,
                        requireActivity().resources.getString(R.string.sleep_timer_canceled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val musicService = musicService
                if (musicService != null && musicService.pendingQuit) {
                    musicService.pendingQuit = false
                    Toast.makeText(
                        activity,
                        requireActivity().resources.getString(R.string.sleep_timer_canceled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .customView(R.layout.dialog_sleep_timer)

        ButterKnife.bind(this, materialDialog.getCustomView())

        timerUpdater = TimerUpdater()

        if (makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE) != null) {
            timerUpdater.start()
        }

        if (activity == null) {
            return materialDialog
        }
        val finishMusic = sleepTimerFinishMusic
        shouldFinishLastSong.isChecked = finishMusic
//        seekArc!!.progressColor = ThemeSingleton.get().positiveColor.getDefaultColor()
//        seekArc!!.setThumbColor(ThemeSingleton.get().positiveColor.getDefaultColor())
        seekArc.post {
            val width = seekArc.width
            val height = seekArc.height
            val small = min(width, height)
            val layoutParams = FrameLayout.LayoutParams(
                seekArc.layoutParams
            )
            layoutParams.height = small
            seekArc.layoutParams = layoutParams
        }
        seekArcProgress = lastSleepTimerValue
        updateTimeDisplayTime()
        seekArc.progress = seekArcProgress
        seekArc.setOnSeekArcChangeListener(object : OnSeekArcChangeListener {
            override fun onProgressChanged(seekArc: SeekArc, i: Int, b: Boolean) {
                if (i < 1) {
                    seekArc.progress = 1
                    return
                }
                seekArcProgress = i
                updateTimeDisplayTime()
            }

            override fun onStartTrackingTouch(seekArc: SeekArc) {}
            override fun onStopTrackingTouch(seekArc: SeekArc) {
                lastSleepTimerValue = seekArcProgress
            }
        })
        return materialDialog
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimeDisplayTime() {
        timerDisplay.text = "$seekArcProgress min"
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(activity, 0, makeTimerIntent(), flag)
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(activity, MusicService::class.java)
        return if (shouldFinishLastSong.isChecked) {
            intent.setAction(MusicService.ACTION_PENDING_QUIT)
        } else intent.setAction(MusicService.ACTION_QUIT)
    }

    private fun updateCancelButton() {
        val musicService = musicService
        if (musicService != null && musicService.pendingQuit) {
            materialDialog.neutralButton(R.string.cancel_current_timer)
        } else {
            materialDialog.neutralButton(text = null)
        }
    }

    private inner class TimerUpdater :
        CountDownTimer(nextSleepTimerElapsedRealTime - SystemClock.elapsedRealtime(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            materialDialog.neutralButton(
                text =  materialDialog.context.getString(
                    R.string.cancel_current_timer
                ) + " (" + MusicUtil.getReadableDurationString(millisUntilFinished) + ")"
            )
        }

        override fun onFinish() {
            updateCancelButton()
        }
    }
}