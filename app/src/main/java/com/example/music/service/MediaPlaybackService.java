package com.example.music.service;

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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.music.ActivityMusic;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.notification.Notification;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class MediaPlaybackService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    public static final String BROAD_POSITION = "BROAD_POSITION";
    public static final String PRF_NAME = "PRF_NAME";
    public static final String PRF_POSITION = "PRF_POSITION";
    public static final String PRF_REPEAT = "PRF_REPEAT";
    public static final String PRF_SHUFFLE = "PRF_SHUFFLE";
    public static final String PRF_CURRENT_TIME = "PRF_CURRENT_TIME";
    public static final String PRF_DURATION = "PRF_DURATION";
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
    private int mCurrentTime, mDuration;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private NotificationManager mNotifyManager;
    private Context mContext;

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        mIsShuffle = mSharedPrf.getBoolean(PRF_SHUFFLE, false);
        mRepeat = mSharedPrf.getString(PRF_REPEAT, ActivityMusic.FALSE);

        if (mIsShuffle) {
            if (mRepeat.equals(REPEAT)) {
                playSong();
            } else {
                playShuffle();
            }
        } else {
            switch (mRepeat) {
                case FALSE:
                    if (mPosition != mArraySongs.size() -1) {
                        playNext();
                    }
                    break;
                case TRUE:
                    playNext();
                    break;
                case REPEAT:
                    playRepeat();
                    break;
            }
        }

        setIntent(ActivityMusic.ACTION_PLAY_COMPLETE);
        sendBroadcast(mIntent);

        mEditor.putInt(PRF_POSITION, mPosition);
        mEditor.putBoolean(PRF_SHUFFLE, mIsShuffle);
        mEditor.putString(PRF_REPEAT, mRepeat);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        if (mPosition != -1) {
            mPlayer.reset();
            mUri = Uri.parse(mArraySongs.get(mPosition).getResource());

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

        return super.onStartCommand(intent, flags, startId);
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

    // TODO TrungTH lặp code với playShuffle cần tách hàm sao để dùng chung
    public void playSong() {
        createUri(mPosition);
        createNotification();

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

    public int shuffle() {
        Random random = new Random();
        // TODO TRungth ban đầu gán luôn = pos đỡ bị lặp code random.nextInt(mArraySongs.size());
        int ranNumber = random.nextInt(mArraySongs.size());
        while (mPosition == ranNumber) {
            mPosition = random.nextInt(mArraySongs.size());
        }
        return mPosition;
    }

    private void createUri(int position) {
        mPlayer.reset();
        mUri = Uri.parse(mArraySongs.get(position).getResource());
        try {
            mPlayer.setDataSource(getApplicationContext(), mUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playShuffle() {
        mPosition = shuffle();
        createUri(mPosition);
    }

    public void pauseSong() {
        mPlayer.pause();
    }

    public void resumeSong() {
        mPlayer.start();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void playNext() {
        ++mPosition;
        if (mPosition > mArraySongs.size() - 1) {
            mPosition = 0;
        }
        this.playSong();
    }

    public void playPrev() {
        --mPosition;
        if (mPosition < 0) {
            mPosition = mArraySongs.size() - 1;
        }
        this.playSong();
    }

    public void playRepeat() {
        this.playSong();
    }

    public void setShuffle(boolean shuffle) {
        mIsShuffle = shuffle;
    }

    public void setRepeat(String repeat) {
        mRepeat = repeat;
    }

    private void setIntent(String action) {
        mIntent.setAction(action);
        mIntent.putExtra(BROAD_POSITION, mPosition);
    }

//    public void putDataToSharedPrf(int position, String repeat, boolean isShuffle) {
//        //put data to SharedPreference
//        mEditor.putInt(PRF_POSITION, position);
//        mEditor.putString(PRF_REPEAT, repeat);
//        mEditor.putBoolean(PRF_SHUFFLE, isShuffle);
////        mEditor.putInt(PRF_CURRENT_TIME, mCurrentTime);
////        mEditor.putInt(PRF_DURATION, mDuration);
//        mEditor.commit();
//    }

    public void putRepeatToPrf(String repeat) {
        mEditor.putString(PRF_REPEAT, repeat);
        mEditor.commit();
    }

    public void putShuffleToPrf(boolean isShuffle) {
        mEditor.putBoolean(PRF_SHUFFLE, isShuffle);
        mEditor.commit();
    }

    public void putPositionToPrf(int position) {
        mEditor.putInt(PRF_POSITION, position);
        mEditor.commit();
    }

    public ArrayList<Song> getArraySongs() {
        return mArraySongs;
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
                .setContentText("This is your notification text.");

        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
        startForeground(1, notifyBuilder.build());
    }
}