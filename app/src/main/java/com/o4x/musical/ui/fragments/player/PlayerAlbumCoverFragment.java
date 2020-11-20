package com.o4x.musical.ui.fragments.player;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.helper.MusicProgressViewUpdateHelper;
import com.o4x.musical.model.lyrics.AbsSynchronizedLyrics;
import com.o4x.musical.model.lyrics.Lyrics;
import com.o4x.musical.ui.adapter.AlbumCoverPagerAdapter;
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment;
import com.o4x.musical.util.PreferenceUtil;
import com.o4x.musical.util.color.MediaNotificationProcessor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayerAlbumCoverFragment extends AbsMusicServiceFragment implements ViewPager.OnPageChangeListener, MusicProgressViewUpdateHelper.Callback {

    public PlayerAlbumCoverFragment() {
        super(R.layout.fragment_player_album_cover);
    }

    public static final int VISIBILITY_ANIM_DURATION = 300;

    private Unbinder unbinder;

    @BindView(R.id.player_album_cover_viewpager)
    ViewPager viewPager;

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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
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

    private AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver colorReceiver =
            new AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver() {
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

//            if (position <= 1) { // [-1,1]
//                view.findViewById(R.id.player_image).setTranslationX(-position * pageWidth / 2);
//            }

            if(position <= -1.0F || position >= 1.0F) {
                view.setTranslationX(view.getWidth() * position);
                view.setAlpha(0.0F);
            } else if( position == 0.0F ) {
                view.setTranslationX(view.getWidth() * position);
                view.setAlpha(1.0F);
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                view.setTranslationX(view.getWidth() * -position);
                view.setAlpha(1.0F - Math.abs(position));
            }
        }
    }
}
