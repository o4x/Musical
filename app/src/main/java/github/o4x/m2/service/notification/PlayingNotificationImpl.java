package github.o4x.m2.service.notification;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaStyleNotificationHelper;

import github.o4x.m2.R;
import github.o4x.m2.helper.MyPalette;
import github.o4x.m2.imageloader.glide.loader.GlideLoader;
import github.o4x.m2.imageloader.glide.targets.CustomBitmapTarget;
import github.o4x.m2.imageloader.glide.targets.palette.PaletteTargetListener;
import github.o4x.m2.model.Song;
import github.o4x.m2.service.MusicService;
import github.o4x.m2.ui.activities.MainActivity;

import org.jetbrains.annotations.NotNull;

import static github.o4x.m2.service.MusicService.ACTION_REWIND;
import static github.o4x.m2.service.MusicService.ACTION_SKIP;
import static github.o4x.m2.service.MusicService.ACTION_TOGGLE_PAUSE;

@androidx.annotation.OptIn(markerClass = UnstableApi.class)
public class PlayingNotificationImpl extends PlayingNotification {

    // These PendingIntents never change, but building each one is a binder round
    // trip to the system. update() runs on the main thread on every song change
    // (twice: META + PLAY_STATE), so recreating them was jank on skip. Build once.
    private PendingIntent clickIntent;
    private PendingIntent deleteIntent;
    private PendingIntent previousIntent;
    private PendingIntent playPauseIntent;
    private PendingIntent nextIntent;

    private void ensurePendingIntents() {
        if (clickIntent != null) return;

        Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(MusicService.ACTION_QUIT);
        intent.setComponent(serviceName);
        deleteIntent = PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        previousIntent = retrievePlaybackAction(ACTION_REWIND);
        playPauseIntent = retrievePlaybackAction(ACTION_TOGGLE_PAUSE);
        nextIntent = retrievePlaybackAction(ACTION_SKIP);
    }

    @Override
    public synchronized void update() {
        if (stopped)
            return;

        ensurePendingIntents();

        final Song song = service.getCurrentSong();

        final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        GlideLoader.with(service)
                .withListener(new PaletteTargetListener(service) {
                    @Override
                    public void onColorReady(@NotNull MyPalette colors, @Nullable Bitmap resource) {
                        update(resource, colors.getBackgroundColor());
                    }

                    void update(Bitmap bitmap, int color) {
                        final boolean isPlaying = service.isPlaying();
                        final int playButtonResId = isPlaying
                                ? R.drawable.ic_pause : R.drawable.ic_play_arrow;
                        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                                service.getString(R.string.action_play_pause),
                                playPauseIntent);
                        NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous,
                                service.getString(R.string.action_previous),
                                previousIntent);
                        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next,
                                service.getString(R.string.action_next),
                                nextIntent);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setSubText(song.getAlbumName())
                                .setLargeIcon(bitmap)
                                .setContentIntent(clickIntent)
                                .setDeleteIntent(deleteIntent)
                                .setContentTitle(song.getTitle())
                                .setContentText(song.getArtistName())
                                .setOngoing(isPlaying)
                                .setShowWhen(false)
                                .addAction(previousAction)
                                .addAction(playPauseAction)
                                .addAction(nextAction);

                        builder.setStyle(new MediaStyleNotificationHelper.MediaStyle(service.getMediaSession()).setShowActionsInCompactView(0, 1, 2))
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                        if (stopped)
                            return; // notification has been stopped before loading was finished
                        updateNotifyModeAndPostNotification(builder.build());
                    }
                })
                .load(song)
                .into(new CustomBitmapTarget(
                        bigNotificationImageSize, bigNotificationImageSize));
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
