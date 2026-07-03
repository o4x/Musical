package github.o4x.m2.service.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.ServiceCompat;

import github.o4x.m2.R;
import github.o4x.m2.service.MusicService;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class PlayingNotification {

    private static final int NOTIFICATION_ID = 1;
    static final String NOTIFICATION_CHANNEL_ID = "playing_notification";

    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 0;

    private int notifyMode = NOTIFY_MODE_BACKGROUND;

    private NotificationManager notificationManager;
    protected MusicService service;
    boolean stopped = true;

    public synchronized void init(MusicService service) {
        this.service = service;
        notificationManager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    public abstract void update();

    public void start() {
        if (service.isPlaying()) {
            stopped = false;
        }
    }

    public synchronized void stop() {
        stopped = true;
        ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_REMOVE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    void updateNotifyModeAndPostNotification(Notification notification) {
        int newNotifyMode;
        if (service.isPlaying()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }

        if (notifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            ServiceCompat.stopForeground(service, ServiceCompat.STOP_FOREGROUND_DETACH);
        }

        if (newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            ServiceCompat.startForeground(service, NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        notifyMode = newNotifyMode;
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, service.getString(R.string.playing_notification_name), NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(service.getString(R.string.playing_notification_description));
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
