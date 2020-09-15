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
import android.util.Log;
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

    public static final String BROAD_POSITION = "BROAD_POSITION";
    public static final String PRF_NAME = "PRF_NAME";
    public static final String PRF_POSITION = "PRF_POSITION";
    public static final String PRF_REPEAT = "PRF_REPEAT";
    public static final String PRF_SHUFFLE = "PRF_SHUFFLE";
    private static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY_PREV = "ACTION_PLAY_PREV";
    public static final String ACTION_PLAY_NEXT = "ACTION_PLAY_NEXT";

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
    private RemoteViews mNormalView, mExpandedView;

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private NotificationManager mNotifyManager;
    private Notification mNotification;
    private Context mContext;
    private Intent mPlayIntent, mNextIntent, mPrevIntent;

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        mIsShuffle = mSharedPrf.getBoolean(PRF_SHUFFLE, false);
        mRepeat = mSharedPrf.getString(PRF_REPEAT, ActivityMusic.FALSE);

        if (mRepeat.equals(FALSE)) {
            if (!mIsShuffle) {
                if (mPosition != mArraySongs.size() - 1) {
                    playNext();
                }
            } else {
                playNext();
            }
        } else if (mRepeat.equals(TRUE)){
            playNext();
        } else if (mRepeat.equals(REPEAT)) {
            playSong();
        }

        setIntent(ActivityMusic.MESSAGE_BROADCAST_UPDATE_UI);
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
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mArraySongs != null) {
            createChannel();
            createNotification();
        }
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_PLAY)) {
                if (isPlaying()) {
                    pauseSong();
                } else {
                    resumeSong();
                    mNormalView.setImageViewResource(R.id.noti_normal_play, R.drawable.ic_action_play);
                }
            } else if (action.equals(ACTION_PLAY_NEXT)) {
                playNext();
            } else if (action.equals(ACTION_PLAY_PREV)) {
                playPrev();
            }
            setIntent(ActivityMusic.MESSAGE_BROADCAST_UPDATE_UI);
            sendBroadcast(mIntent);
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
        mUri = Uri.parse(mArraySongs.get(mPosition).getmResource());
        try {
            mPlayer.setDataSource(getApplicationContext(), mUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        this.stopForeground(false);
    }

    public void resumeSong() {
        mPlayer.start();
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

    private void setIntent(String action) {
        mIntent.setAction(action);
        mIntent.putExtra(BROAD_POSITION, mPosition);
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
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Music Notification",
                    NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setSound(null, null);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    public void createNotification() {

        mNormalView = new RemoteViews(mContext.getPackageName(), R.layout.custom_normal_notification);
        mExpandedView = new RemoteViews(mContext.getPackageName(), R.layout.custom_expanded_notification);

        mSong = mArraySongs.get(mPosition);

        mNormalView.setImageViewBitmap(R.id.noti_normal_art, mSong.getAlbumArt(mContext, mSong.getmResource()));
        mExpandedView.setImageViewBitmap(R.id.noti_expanded_art, mSong.getAlbumArt(mContext, mSong.getmResource()));
        mExpandedView.setTextViewText(R.id.noti_expanded_song_title, mSong.getmTitle());
        mExpandedView.setTextViewText(R.id.noti_expanded_artist, mSong.getmArtist());

        if (isPlaying()) {
            mNormalView.setImageViewResource(R.id.noti_normal_play, R.drawable.ic_action_pause);
            mExpandedView.setImageViewResource(R.id.noti_expanded_play, R.drawable.ic_action_pause);
        } else {
            mNormalView.setImageViewResource(R.id.noti_normal_play, R.drawable.ic_action_play);
            mExpandedView.setImageViewResource(R.id.noti_expanded_play, R.drawable.ic_action_play);
        }

        mPlayIntent = new Intent(this, MediaPlaybackService.class).setAction(ACTION_PLAY);
        PendingIntent pPlayIntent = PendingIntent.getService(this, NOTIFICATION_ID, mPlayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNextIntent = new Intent(this, MediaPlaybackService.class).setAction(ACTION_PLAY_NEXT);
        PendingIntent pNextIntent = PendingIntent.getService(this, NOTIFICATION_ID, mNextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mPrevIntent = new Intent(this, MediaPlaybackService.class).setAction(ACTION_PLAY_PREV);
        PendingIntent pPrevIntent = PendingIntent.getService(this, NOTIFICATION_ID, mPrevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNormalView.setOnClickPendingIntent(R.id.noti_normal_play, pPlayIntent);
        mNormalView.setOnClickPendingIntent(R.id.noti_normal_next, pNextIntent);
        mNormalView.setOnClickPendingIntent(R.id.noti_normal_prev, pPrevIntent);

        mExpandedView.setOnClickPendingIntent(R.id.noti_expanded_play, pPlayIntent);
        mExpandedView.setOnClickPendingIntent(R.id.noti_expanded_next, pNextIntent);
        mExpandedView.setOnClickPendingIntent(R.id.noti_expanded_prev, pPrevIntent);

        Intent intent = new Intent(mContext, ActivityMusic.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setShowWhen(false)
                .setCustomContentView(mNormalView)
                .setCustomBigContentView(mExpandedView)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(NOTIFICATION_ID, mNotification);
    }

    private void updateNotification() {

    }
}