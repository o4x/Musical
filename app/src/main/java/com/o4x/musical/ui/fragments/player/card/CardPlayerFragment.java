package com.o4x.musical.ui.fragments.player.card;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.helper.menu.SongMenuHelper;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.ui.dialogs.SongShareDialog;
import com.o4x.musical.ui.fragments.player.AbsPlayerFragment;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.Util;
import com.o4x.musical.util.ViewUtil;
import com.o4x.musical.views.WidthFitSquareLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class CardPlayerFragment extends AbsPlayerFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Util.isLandscape(getResources())) {
            impl = new LandscapeImpl(this);
        } else {
            impl = new PortraitImpl(this);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_card_player;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // for some reason the xml attribute doesn't get applied here.
        playingQueueCard.setCardBackgroundColor(ATHUtil.resolveColor(getActivity(), R.attr.cardBackgroundColor));
    }

    @Override
    public void onPanelSlide(View view, float slide) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float density = getResources().getDisplayMetrics().density;

            float cardElevation = (6 * slide + 2) * density;
            if (!isValidElevation(cardElevation)) return; // we have received some crash reports in setCardElevation()
            playingQueueCard.setCardElevation(cardElevation);

            float buttonElevation = (2 * Math.max(0, (1 - (slide * 16))) + 2) * density;
            if (!isValidElevation(buttonElevation)) return;
            playbackControlsFragment.playPauseButton.setElevation(buttonElevation);
        }
    }

    private boolean isValidElevation(float elevation) {
        return elevation >= -Float.MAX_VALUE && elevation <= Float.MAX_VALUE;
    }

    private static abstract class BaseImpl implements Impl {
        protected CardPlayerFragment fragment;

        public BaseImpl(CardPlayerFragment fragment) {
            this.fragment = fragment;
        }

        public AnimatorSet createDefaultColorChangeAnimatorSet(int newColor) {
            Animator backgroundAnimator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //noinspection ConstantConditions
                int x = (int) (fragment.playbackControlsFragment.playPauseButton.getX() + fragment.playbackControlsFragment.playPauseButton.getWidth() / 2 + fragment.playbackControlsFragment.getView().getX());
                int y = (int) (fragment.playbackControlsFragment.playPauseButton.getY() + fragment.playbackControlsFragment.playPauseButton.getHeight() / 2 + fragment.playbackControlsFragment.getView().getY() + fragment.playbackControlsFragment.progressSlider.getHeight());
                float startRadius = Math.max(fragment.playbackControlsFragment.playPauseButton.getWidth() / 2, fragment.playbackControlsFragment.playPauseButton.getHeight() / 2);
                float endRadius = Math.max(fragment.colorBackground.getWidth(), fragment.colorBackground.getHeight());
                fragment.colorBackground.setBackgroundColor(newColor);
                backgroundAnimator = ViewAnimationUtils.createCircularReveal(fragment.colorBackground, x, y, startRadius, endRadius);
            } else {
                backgroundAnimator = ViewUtil.createBackgroundColorTransition(fragment.colorBackground, fragment.lastColor, newColor);
            }

            AnimatorSet animatorSet = new AnimatorSet();

            animatorSet.play(backgroundAnimator);

            if (!ATHUtil.isWindowBackgroundDark(fragment.getActivity())) {
                int adjustedLastColor = ColorUtil.isColorLight(fragment.lastColor) ? ColorUtil.darkenColor(fragment.lastColor) : fragment.lastColor;
                int adjustedNewColor = ColorUtil.isColorLight(newColor) ? ColorUtil.darkenColor(newColor) : newColor;
                Animator subHeaderAnimator = ViewUtil.createTextColorTransition(fragment.playerQueueSubHeader, adjustedLastColor, adjustedNewColor);
                animatorSet.play(subHeaderAnimator);
            }

            animatorSet.setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME);
            return animatorSet;
        }

        @Override
        public void animateColorChange(int newColor) {
            if (ATHUtil.isWindowBackgroundDark(fragment.getActivity())) {
                fragment.playerQueueSubHeader.setTextColor(ThemeStore.textColorSecondary(fragment.getActivity()));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class PortraitImpl extends BaseImpl {
        MediaEntryViewHolder currentSongViewHolder;
        Song currentSong = Song.EMPTY_SONG;

        public PortraitImpl(CardPlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {
            currentSongViewHolder = new MediaEntryViewHolder(fragment.getView().findViewById(R.id.current_song));

            currentSongViewHolder.separator.setVisibility(View.VISIBLE);
            currentSongViewHolder.shortSeparator.setVisibility(View.GONE);
            currentSongViewHolder.image.setScaleType(ImageView.ScaleType.CENTER);
            currentSongViewHolder.image.setColorFilter(ATHUtil.resolveColor(fragment.getActivity(), R.attr.iconColor, ThemeStore.textColorSecondary(fragment.getActivity())), PorterDuff.Mode.SRC_IN);
            currentSongViewHolder.image.setImageResource(R.drawable.ic_volume_up_white_24dp);
            currentSongViewHolder.itemView.setOnClickListener(v -> {
                // toggle the panel
                if (fragment.slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    fragment.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                } else if (fragment.slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    fragment.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            });
            currentSongViewHolder.menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu((AppCompatActivity) fragment.getActivity()) {
                @Override
                public Song getSong() {
                    return currentSong;
                }

                public int getMenuRes() {
                    return R.menu.menu_item_playing_queue_song;
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_remove_from_playing_queue:
                            MusicPlayerRemote.removeFromQueue(MusicPlayerRemote.getPosition());
                            return true;
                        case R.id.action_share:
                            SongShareDialog.create(getSong()).show(fragment.getFragmentManager(), "SONG_SHARE_DIALOG");
                            return true;
                    }
                    return super.onMenuItemClick(item);
                }
            });
        }

        @Override
        public void setUpPanelAndAlbumCoverHeight() {
            WidthFitSquareLayout albumCoverContainer = fragment.getView().findViewById(R.id.album_cover_container);

            final int availablePanelHeight = fragment.slidingUpPanelLayout.getHeight() - fragment.getView().findViewById(R.id.player_content).getHeight() + (int) ViewUtil.convertDpToPixel(8, fragment.getResources());
            final int minPanelHeight = (int) ViewUtil.convertDpToPixel(72 + 24, fragment.getResources());
            if (availablePanelHeight < minPanelHeight) {
                albumCoverContainer.getLayoutParams().height = albumCoverContainer.getHeight() - (minPanelHeight - availablePanelHeight);
                albumCoverContainer.forceSquare(false);
            }
            fragment.slidingUpPanelLayout.setPanelHeight(Math.max(minPanelHeight, availablePanelHeight));
        }

        @Override
        public void updateCurrentSong(Song song) {
            currentSong = song;
            currentSongViewHolder.title.setText(song.title);
            currentSongViewHolder.text.setText(MusicUtil.getSongInfoString(song));
        }

        @Override
        public void animateColorChange(int newColor) {
            super.animateColorChange(newColor);

            fragment.slidingUpPanelLayout.setBackgroundColor(fragment.lastColor);

            createDefaultColorChangeAnimatorSet(newColor).start();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class LandscapeImpl extends BaseImpl {
        public LandscapeImpl(CardPlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {

        }

        @Override
        public void setUpPanelAndAlbumCoverHeight() {
            int panelHeight = fragment.slidingUpPanelLayout.getHeight() - fragment.playbackControlsFragment.getView().getHeight();
            fragment.slidingUpPanelLayout.setPanelHeight(panelHeight);
        }

        @Override
        public void updateCurrentSong(Song song) {
            fragment.toolbar.setTitle(song.title);
            fragment.toolbar.setSubtitle(MusicUtil.getSongInfoString(song));
        }

        @Override
        public void animateColorChange(int newColor) {
            super.animateColorChange(newColor);

            fragment.slidingUpPanelLayout.setBackgroundColor(fragment.lastColor);

            AnimatorSet animatorSet = createDefaultColorChangeAnimatorSet(newColor);
            animatorSet.play(ViewUtil.createBackgroundColorTransition(fragment.toolbar, fragment.lastColor, newColor))
                    .with(ViewUtil.createBackgroundColorTransition(null /* statusBar */, ColorUtil.darkenColor(fragment.lastColor), ColorUtil.darkenColor(newColor)));
            animatorSet.start();
        }
    }
}
