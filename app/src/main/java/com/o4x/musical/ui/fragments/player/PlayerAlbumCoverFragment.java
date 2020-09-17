package com.o4x.musical.ui.fragments.player;

import android.animation.Animator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.helper.MusicProgressViewUpdateHelper;
import com.o4x.musical.misc.SimpleAnimatorListener;
import com.o4x.musical.model.lyrics.AbsSynchronizedLyrics;
import com.o4x.musical.model.lyrics.Lyrics;
import com.o4x.musical.ui.adapter.AlbumCoverPagerAdapter;
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment;
import com.o4x.musical.util.PreferenceUtil;
import com.o4x.musical.util.ViewUtil;
import com.o4x.musical.util.color.MediaNotificationProcessor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayerAlbumCoverFragment extends AbsMusicServiceFragment implements ViewPager.OnPageChangeListener, MusicProgressViewUpdateHelper.Callback {

    public static final int VISIBILITY_ANIM_DURATION = 300;

    private Unbinder unbinder;

    @BindView(R.id.player_album_cover_viewpager)
    ViewPager viewPager;
    @BindView(R.id.player_favorite_icon)
    ImageView favoriteIcon;

    @BindView(R.id.player_lyrics)
    FrameLayout lyricsLayout;
    @BindView(R.id.player_lyrics_line1)
    TextView lyricsLine1;
    @BindView(R.id.player_lyrics_line2)
    TextView lyricsLine2;

    private Callbacks callbacks;
    private int currentPosition;

    private Lyrics lyrics;
    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_album_cover, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageTransformer(true, new ParallaxPageTransformer());
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this, 500, 1000);
        progressViewUpdateHelper.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.removeOnPageChangeListener(this);
        progressViewUpdateHelper.stop();
        unbinder.unbind();
    }

    @Override
    public void onServiceConnected() {
        updatePlayingQueue();
    }

    @Override
    public void onPlayingMetaChanged() {
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition(), true);
    }

    @Override
    public void onQueueChanged() {
        updatePlayingQueue();
    }

    private void updatePlayingQueue() {
        viewPager.setAdapter(new AlbumCoverPagerAdapter(getFragmentManager(), MusicPlayerRemote.getPlayingQueue()));
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
        onPageSelected(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        ((AlbumCoverPagerAdapter) viewPager.getAdapter()).receiveColor(colorReceiver, position);
        if (position != MusicPlayerRemote.getPosition()) {
            MusicPlayerRemote.setPosition(position);
        }
    }

    private AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver colorReceiver = new AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver() {
        @Override
        public void onColorReady(MediaNotificationProcessor colors, int requestCode) {
            if (currentPosition == requestCode) {
                notifyColorChange(colors);
            }
        }
    };

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void showHeartAnimation() {
        favoriteIcon.clearAnimation();

        favoriteIcon.setAlpha(0f);
        favoriteIcon.setScaleX(0f);
        favoriteIcon.setScaleY(0f);
        favoriteIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setPivotX(favoriteIcon.getWidth() / 2);
        favoriteIcon.setPivotY(favoriteIcon.getHeight() / 2);

        favoriteIcon.animate()
                .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        favoriteIcon.setVisibility(View.INVISIBLE);
                    }
                })
                .withEndAction(() -> favoriteIcon.animate()
                        .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                        .setInterpolator(new AccelerateInterpolator())
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .start())
                .start();
    }

    private boolean isLyricsLayoutVisible() {
        return lyrics != null && lyrics.isSynchronized() && lyrics.isValid() && PreferenceUtil.synchronizedLyricsShow();
    }

    private boolean isLyricsLayoutBound() {
        return lyricsLayout != null && lyricsLine1 != null && lyricsLine2 != null;
    }

    private void hideLyricsLayout() {
        lyricsLayout.animate().alpha(0f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION).withEndAction(() -> {
            if (!isLyricsLayoutBound()) return;
            lyricsLayout.setVisibility(View.GONE);
            lyricsLine1.setText(null);
            lyricsLine2.setText(null);
        });
    }

    public void setLyrics(Lyrics l) {
        lyrics = l;

        if (!isLyricsLayoutBound()) return;

        if (!isLyricsLayoutVisible()) {
            hideLyricsLayout();
            return;
        }

        lyricsLine1.setText(null);
        lyricsLine2.setText(null);

        lyricsLayout.setVisibility(View.VISIBLE);
        lyricsLayout.animate().alpha(1f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);
    }

    private void notifyColorChange(MediaNotificationProcessor colors) {
        if (callbacks != null) callbacks.onColorChanged(colors);
    }

    public void setCallbacks(Callbacks listener) {
        callbacks = listener;
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        if (!isLyricsLayoutBound()) return;

        if (!isLyricsLayoutVisible()) {
            hideLyricsLayout();
            return;
        }

        if (!(lyrics instanceof AbsSynchronizedLyrics)) return;
        AbsSynchronizedLyrics synchronizedLyrics = (AbsSynchronizedLyrics) lyrics;

        lyricsLayout.setVisibility(View.VISIBLE);
        lyricsLayout.setAlpha(1f);

        String oldLine = lyricsLine2.getText().toString();
        String line = synchronizedLyrics.getLine(progress);

        if (!oldLine.equals(line) || oldLine.isEmpty()) {
            lyricsLine1.setText(oldLine);
            lyricsLine2.setText(line);

            lyricsLine1.setVisibility(View.VISIBLE);
            lyricsLine2.setVisibility(View.VISIBLE);

            lyricsLine2.measure(View.MeasureSpec.makeMeasureSpec(lyricsLine2.getMeasuredWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
            int h = lyricsLine2.getMeasuredHeight();

            lyricsLine1.setAlpha(1f);
            lyricsLine1.setTranslationY(0f);
            lyricsLine1.animate().alpha(0f).translationY(-h).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);

            lyricsLine2.setAlpha(0f);
            lyricsLine2.setTranslationY(h);
            lyricsLine2.animate().alpha(1f).translationY(0f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);
        }
    }

    public interface Callbacks {
        void onColorChanged(MediaNotificationProcessor colors);

        void onFavoriteToggled();
    }

    public static class ParallaxPageTransformer implements ViewPager.PageTransformer {

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]

                view.findViewById(R.id.player_image).setTranslationX(-position * pageWidth / 2);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}
