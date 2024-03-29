package com.example.music.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.music.ActivityMusic;
import com.example.music.R;
import com.example.music.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MediaPlaybackService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    public static final String PRF_NAME = "PRF_NAME";
    public static final String PRF_ID = "PRF_ID";
    public static final String PRF_REPEAT = "PRF_REPEAT";
    public static final String PRF_SHUFFLE = "PRF_SHUFFLE";
    private final String ACTION_PLAY = "ACTION_PLAY";
    public final String ACTION_PLAY_PREV = "ACTION_PLAY_PREV";
    public final String ACTION_PLAY_NEXT = "ACTION_PLAY_NEXT";

    private MediaPlayer mPlayer;
    private Uri mUri;
    private ArrayList<Song> mArraySongs;
    private int mPosition;
    private boolean mIsShuffle;
    private String mRepeat;
    private Intent mIntent;
    private SharedPreferences mSharedPrf;
    private SharedPreferences.Editor mEditor;
    public final String FALSE = "false";
    public final String TRUE = "true";
    public final String REPEAT = "repeat";
    private Song mSong;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private Context mContext;

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        mIsShuffle = mSharedPrf.getBoolean(PRF_SHUFFLE, false);
        mRepeat = mSharedPrf.getString(PRF_REPEAT, FALSE);

        if (mRepeat.equals(FALSE)) {
            if (!mIsShuffle) {
                if (mPosition != mArraySongs.size() - 1) {
                    playNext();

                }
            } else {
                playNext();
            }
        } else if (mRepeat.equals(TRUE)) {
            playNext();
        } else if (mRepeat.equals(REPEAT)) {
            playSong();
        }
        startForeground(NOTIFICATION_ID, createNotification(isPlaying()));

        sendBroadcastMessage(ActivityMusic.MESSAGE_BROADCAST_UPDATE_UI);

        mEditor.putInt(PRF_ID, mSong.getmId());
        mEditor.commit();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        if (mPosition != -1) {
            mPlayer.reset();
            mUri = Uri.parse(mArraySongs.get(mPosition).getmResource());

            try {
                mPlayer.setDataSource(getApplicationContext(), mUri);
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public class LocalBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
        mIntent = new Intent();
        mSharedPrf = getSharedPreferences(PRF_NAME, MODE_PRIVATE);
        mEditor = mSharedPrf.edit();
        mContext = getApplicationContext();
        createChannel();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    if (isPlaying())
                        pauseSong();
                    else
                        resumeSong();
                    break;
                case ACTION_PLAY_NEXT:
                    playNext();
                    break;
                case ACTION_PLAY_PREV:
                    playPrev();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = null;
    }

    public void setArraySongs(ArrayList arrayList) {
        mArraySongs = arrayList;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void playSong() {
        mPlayer.reset();
        mSong = mArraySongs.get(mPosition);
        mUri = Uri.parse(mSong.getmResource());
        try {
            mPlayer.setDataSource(getApplicationContext(), mUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mEditor.putInt(PRF_ID, mSong.getmId());
        mEditor.commit();
        startForeground(NOTIFICATION_ID, createNotification(isPlaying()));
        sendBroadcastMessage(ActivityMusic.MESSAGE_BROADCAST_UPDATE_UI);
    }

    public void playerSeekTo(int i) {
        mPlayer.seekTo(i);
    }

    public int getCurrentTime() {
        return mPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public int getPosition() {
        return mPosition;
    }

    public void stopSong() {
        mPlayer.stop();
    }

    public int shuffle() {
        int position = mPosition;
        Random random = new Random();
        mPosition = random.nextInt(mArraySongs.size());
        while (mPosition == position) {
            mPosition = random.nextInt(mArraySongs.size());
        }

        return mPosition;
    }

    public void pauseSong() {
        mPlayer.pause();

        startForeground(NOTIFICATION_ID, createNotification(isPlaying()));
        stopForeground(false);
        sendBroadcastMessage(ActivityMusic.MESSAGE_BROADCAST_UPDATE_UI);
    }

    public void resumeSong() {
        mPlayer.start();

        startForeground(NOTIFICATION_ID, createNotification(isPlaying()));
        sendBroadcastMessage(ActivityMusic.MESSAGE_BROADCAST_UPDATE_UI);
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void playNext() {
        if (mIsShuffle) {
            mPosition = shuffle();
        } else {
            ++mPosition;
            if (mPosition > mArraySongs.size() - 1) {
                mPosition = 0;
            }
        }
        playSong();
    }

    public void playPrev() {
        if (getCurrentTime() < 3000) {
            --mPosition;
            if (mPosition < 0) {
                mPosition = mArraySongs.size() - 1;
            }
        }
        playSong();
    }

    public void setShuffle(boolean shuffle) {
        mIsShuffle = shuffle;
    }

    public void setRepeat(String repeat) {
        mRepeat = repeat;
    }

    private void sendBroadcastMessage(String action) {
        mIntent.setAction(action);
        sendBroadcast(mIntent);
    }

    public void putRepeatToPrf(String repeat) {
        mEditor.putString(PRF_REPEAT, repeat);
        mEditor.commit();
    }

    public void putShuffleToPrf(boolean isShuffle) {
        mEditor.putBoolean(PRF_SHUFFLE, isShuffle);
        mEditor.commit();
    }

    public ArrayList<Song> getArraySongs() {
        return mArraySongs;
    }

    public void createChannel() {
        NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Music Notification",
                    NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setSound(null, null);
            notifyManager.createNotificationChannel(notificationChannel);
        }
    }

    public Notification createNotification(boolean isPlaying) {
        RemoteViews normalView = new RemoteViews(mContext.getPackageName(), R.layout.custom_normal_notification);
        RemoteViews expandedView = new RemoteViews(mContext.getPackageName(), R.layout.custom_expanded_notification);

        mSong = mArraySongs.get(mPosition);

        normalView.setImageViewBitmap(R.id.noti_normal_art, mSong.getAlbumArt(mContext, mSong.getmResource()));
        expandedView.setImageViewBitmap(R.id.noti_expanded_art, mSong.getAlbumArt(mContext, mSong.getmResource()));
        expandedView.setTextViewText(R.id.noti_expanded_song_title, mSong.getmTitle());
        expandedView.setTextViewText(R.id.noti_expanded_artist, mSong.getmArtist());

        if (isPlaying) {
            normalView.setImageViewResource(R.id.noti_normal_play, R.drawable.ic_action_pause);
            expandedView.setImageViewResource(R.id.noti_expanded_play, R.drawable.ic_action_pause);
        } else {
            normalView.setImageViewResource(R.id.noti_normal_play, R.drawable.ic_action_play);
            expandedView.setImageViewResource(R.id.noti_expanded_play, R.drawable.ic_action_play);
        }

        Intent playIntent = new Intent(this, MediaPlaybackService.class).setAction(ACTION_PLAY);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, NOTIFICATION_ID, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(this, MediaPlaybackService.class).setAction(ACTION_PLAY_NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getService(this, NOTIFICATION_ID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent prevIntent = new Intent(this, MediaPlaybackService.class).setAction(ACTION_PLAY_PREV);
        PendingIntent pendingPrevIntent = PendingIntent.getService(this, NOTIFICATION_ID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        normalView.setOnClickPendingIntent(R.id.noti_normal_play, pendingPlayIntent);
        normalView.setOnClickPendingIntent(R.id.noti_normal_next, pendingNextIntent);
        normalView.setOnClickPendingIntent(R.id.noti_normal_prev, pendingPrevIntent);

        expandedView.setOnClickPendingIntent(R.id.noti_expanded_play, pendingPlayIntent);
        expandedView.setOnClickPendingIntent(R.id.noti_expanded_next, pendingNextIntent);
        expandedView.setOnClickPendingIntent(R.id.noti_expanded_prev, pendingPrevIntent);

        Intent intent = new Intent(mContext, ActivityMusic.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setShowWhen(false)
                .setCustomContentView(normalView)
                .setCustomBigContentView(expandedView)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        return notification;
    }
}