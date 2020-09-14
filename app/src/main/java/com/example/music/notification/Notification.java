package com.example.music.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.music.ActivityMusic;
import com.example.music.R;

public class Notification {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private NotificationManager mNotifyManager;
    private Context mContext;

    public Notification(Context context) {
        mContext = context;
    }

    public void createChannel() {
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Music Notification",
                    NotificationManager.IMPORTANCE_HIGH);

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    public void createNotification() {
        RemoteViews normalLayout = new RemoteViews(mContext.getPackageName(), R.layout.custom_normal_notification);
        RemoteViews expandedLayout = new RemoteViews(mContext.getPackageName(), R.layout.custom_expanded_notification);

        Intent intent = new Intent(mContext, ActivityMusic.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(normalLayout)
                .setCustomBigContentView(expandedLayout)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("You've been notified!")
                .setContentText("This is your notification text.")
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }
}
