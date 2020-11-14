package com.o4x.musical.service.notification;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.o4x.musical.R;
import com.o4x.musical.helper.MyPalette;
import com.o4x.musical.imageloader.glide.loader.GlideLoader;
import com.o4x.musical.imageloader.glide.targets.CustomBitmapTarget;
import com.o4x.musical.imageloader.glide.targets.palette.PaletteTargetListener;
import com.o4x.musical.model.Song;
import com.o4x.musical.service.MusicService;
import com.o4x.musical.ui.activities.MainActivity;
import com.o4x.musical.util.PreferenceUtil;

import org.jetbrains.annotations.NotNull;

import static com.o4x.musical.service.MusicService.ACTION_REWIND;
import static com.o4x.musical.service.MusicService.ACTION_SKIP;
import static com.o4x.musical.service.MusicService.ACTION_TOGGLE_PAUSE;

public class PlayingNotificationImpl24 extends PlayingNotification {

    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = service.getCurrentSong();

        final boolean isPlaying = service.isPlaying();

        final int playButtonResId = isPlaying
                ? R.drawable.ic_pause : R.drawable.ic_play_arrow;

        Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, 0);

        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(MusicService.ACTION_QUIT);
        intent.setComponent(serviceName);
        final PendingIntent deleteIntent = PendingIntent.getService(service, 0, intent, 0);

        final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        service.runOnNewThread(() ->
                GlideLoader.with(service)
                        .load(song)
                        .into(new CustomBitmapTarget(
                                bigNotificationImageSize, bigNotificationImageSize))
                        .setListener(new PaletteTargetListener(service) {
                            @Override
                            public void onColorReady(@NotNull MyPalette colors, @Nullable Bitmap resource) {
                                update(resource, colors.getBackgroundColor());
                            }

                            void update(Bitmap bitmap, int color) {
                                if (bitmap == null)
                                    bitmap = BitmapFactory.decodeResource(service.getResources(), R.drawable.default_album_art);
                                NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                                        service.getString(R.string.action_play_pause),
                                        retrievePlaybackAction(ACTION_TOGGLE_PAUSE));
                                NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous,
                                        service.getString(R.string.action_previous),
                                        retrievePlaybackAction(ACTION_REWIND));
                                NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next,
                                        service.getString(R.string.action_next),
                                        retrievePlaybackAction(ACTION_SKIP));
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

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder.setStyle(new MediaStyle().setMediaSession(service.getMediaSession().getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && PreferenceUtil.isColoredNotification())
                                        builder.setColor(color);
                                }

                                if (stopped)
                                    return; // notification has been stopped before loading was finished
                                updateNotifyModeAndPostNotification(builder.build());
                            }
                        })
        );
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(service, 0, intent, 0);
    }
}
