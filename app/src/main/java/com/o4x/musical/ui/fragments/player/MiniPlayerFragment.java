package com.o4x.musical.ui.fragments.player;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.helper.MusicProgressViewUpdateHelper;
import com.o4x.musical.helper.PlayPauseButtonOnClickHandler;
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment;
import com.o4x.musical.util.color.MediaNotificationProcessor;
import com.o4x.musical.views.IconImageView;
import com.o4x.musical.drawables.PlayPauseDrawable;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.extensions.ColorExtKt;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MiniPlayerFragment extends AbsMusicServiceFragment implements MusicProgressViewUpdateHelper.Callback {

    public MiniPlayerFragment() {
        super(R.layout.fragment_mini_player);
    }

    private Unbinder unbinder;

    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.mini_player_image)
    IconImageView miniPlayerImage;
    @BindView(R.id.mini_player_title)
    TextView miniPlayerTitle;
    @BindView(R.id.mini_player_play_pause_button)
    ImageView miniPlayerPlayPauseButton;
    @BindView(R.id.progress_bar_container)
    FrameLayout progressBarContainer;
    @BindView(R.id.progress_bar)
    MaterialProgressBar progressBar;

    private PlayPauseDrawable miniPlayerPlayPauseDrawable;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        view.setOnTouchListener(new FlingPlayBackController(getActivity()));
        setUpMiniPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setUpMiniPlayer() {
        setUpPlayPauseButton();
        setProgressColor(ColorExtKt.accentColor(this));
    }

    private void setProgressColor(@ColorInt int color) {
        progressBarContainer.setBackgroundColor(ColorUtil.INSTANCE.withAlpha(color, .3f));
        progressBar.setSupportProgressTintList(ColorStateList.valueOf(color));
    }

    private void setUpPlayPauseButton() {
        miniPlayerPlayPauseDrawable = new PlayPauseDrawable(getActivity());
        miniPlayerPlayPauseButton.setImageDrawable(miniPlayerPlayPauseDrawable);
        miniPlayerPlayPauseButton.setColorFilter(ATHUtil.INSTANCE.resolveColor(getActivity(), R.attr.iconColor, ColorExtKt.textColorSecondary(getActivity())), PorterDuff.Mode.SRC_IN);
        miniPlayerPlayPauseButton.setOnClickListener(new PlayPauseButtonOnClickHandler());
    }

    private void updateSongTitle() {
        miniPlayerTitle.setText(MusicPlayerRemote.getCurrentSong().getTitle());
    }

    @Override
    public void onServiceConnected() {
        updateSongTitle();
        updatePlayPauseDrawableState(false);
        onUpdateProgressViews(MusicPlayerRemote.getSongProgressMillis(),
                MusicPlayerRemote.getSongDurationMillis());
    }

    @Override
    public void onPlayingMetaChanged() {
        updateSongTitle();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        progressBar.setMax(total);
        progressBar.setProgress(progress);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressViewUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        progressViewUpdateHelper.stop();
    }

    private static class FlingPlayBackController implements View.OnTouchListener {

        GestureDetector flingPlayBackController;

        public FlingPlayBackController(Context context) {
            flingPlayBackController = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSong();
                            return true;
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSong();
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return flingPlayBackController.onTouchEvent(event);
        }
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (MusicPlayerRemote.isPlaying()) {
            miniPlayerPlayPauseDrawable.setPause(animate);
        } else {
            miniPlayerPlayPauseDrawable.setPlay(animate);
        }
    }

    public void setColor(@NotNull MediaNotificationProcessor colors) {
        final int fg = colors.getPrimaryTextColor();

        container.setBackgroundColor(colors.getBackgroundColor());
        setProgressColor(fg);
        miniPlayerImage.setColorFilter(fg);
        miniPlayerPlayPauseButton.setColorFilter(fg, PorterDuff.Mode.SRC_IN);
        miniPlayerTitle.setTextColor(fg);

        // I want title slide show just when it colored
        miniPlayerTitle.setSelected(true);
    }
}
