package github.o4x.musical.appwidgets;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import github.o4x.musical.R;
import github.o4x.musical.appwidgets.base.BaseAppWidget;
import github.o4x.musical.helper.MyPalette;
import github.o4x.musical.imageloader.glide.loader.GlideLoader;
import github.o4x.musical.imageloader.glide.targets.CustomBitmapTarget;
import github.o4x.musical.imageloader.glide.targets.palette.PaletteTargetListener;
import github.o4x.musical.model.Song;
import github.o4x.musical.service.MusicService;
import github.o4x.musical.ui.activities.MainActivity;
import github.o4x.musical.util.ImageUtil;

import org.jetbrains.annotations.NotNull;

import com.o4x.appthemehelper.util.MaterialValueHelper;

public class AppWidgetCard extends BaseAppWidget {
    public static final String NAME = "app_widget_card";

    private static AppWidgetCard mInstance;
    private static int imageSize = 0;
    private static float cardRadius = 0f;
    private Target<Bitmap> target; // for cancellation

    public static synchronized AppWidgetCard getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetCard();
        }
        return mInstance;
    }

    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    protected void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.app_widget_card);

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art);
        appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_next, MaterialValueHelper.getSecondaryTextColor(context, true))));
        appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_previous, MaterialValueHelper.getSecondaryTextColor(context, true))));
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_play_arrow, MaterialValueHelper.getSecondaryTextColor(context, true))));

        linkButtons(context, appWidgetView);
        pushUpdate(context, appWidgetIds, appWidgetView);
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(final MusicService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_card);

        final boolean isPlaying = service.isPlaying();
        final Song song = service.getCurrentSong();

        // Set the titles and artwork
        if (TextUtils.isEmpty(song.getTitle()) && TextUtils.isEmpty(song.getArtistName())) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
            appWidgetView.setTextViewText(R.id.album_name, song.getTitle());
            appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song));
        }

        // Set correct drawable for pause state
        int playPauseRes = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow;
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, playPauseRes, MaterialValueHelper.getSecondaryTextColor(service, true))));

        // Set prev/next button drawables
        appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_next, MaterialValueHelper.getSecondaryTextColor(service, true))));
        appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_previous, MaterialValueHelper.getSecondaryTextColor(service, true))));

        // Link actions buttons to intents
        linkButtons(service, appWidgetView);

        if (imageSize == 0)
            imageSize = service.getResources().getDimensionPixelSize(R.dimen.app_widget_card_image_size);
        if (cardRadius == 0f)
            cardRadius = service.getResources().getDimension(R.dimen.app_widget_card_radius);

        // Load the album cover async and push the update on completion
        service.runOnNewThread(new Runnable() {
            @Override
            public void run() {
                if (target != null) {
                    Glide.with(service).clear(target);
                }

                target = GlideLoader.with(service)
                        .withListener(new PaletteTargetListener(service) {
                            @Override
                            public void onColorReady(@NotNull MyPalette colors, @Nullable Bitmap resource) {
                                update(resource, colors.getBackgroundColor());
                            }

                            private void update(@Nullable Bitmap bitmap, int color) {
                                // Set correct drawable for pause state
                                int playPauseRes = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow;
                                appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, playPauseRes, color)));

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_next, color)));
                                appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_previous, color)));

                                final Drawable image = getAlbumArtDrawable(service.getResources(), bitmap);
                                final Bitmap roundedBitmap = createRoundedBitmap(image, imageSize, imageSize, cardRadius, 0, cardRadius, 0);
                                appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap);

                                pushUpdate(service, appWidgetIds, appWidgetView);
                            }
                        })
                        .load(song)
                        .into(new CustomBitmapTarget(imageSize, imageSize));
            }
        });
    }

    /**
     * Link up various button actions using {@link PendingIntent}.
     */
    private void linkButtons(final Context context, final RemoteViews views) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, MusicService.class);

        // Home
        action = new Intent(context, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(context, 0, action, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.image, pendingIntent);
        views.setOnClickPendingIntent(R.id.media_titles, pendingIntent);

        // Previous track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_REWIND, serviceName);
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_TOGGLE_PAUSE, serviceName);
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_SKIP, serviceName);
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }
}
