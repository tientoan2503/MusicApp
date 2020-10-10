package com.example.music;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.music.Interface.IClickItem;
import com.example.music.Interface.IFavoriteControl;
import com.example.music.Interface.IMediaControl;
import com.example.music.adapter.SongAdapter;
import com.example.music.database.FavoriteSongsDB;
import com.example.music.database.SongProvider;
import com.example.music.fragment.AllSongsFragment;
import com.example.music.fragment.BaseSongListFragment;
import com.example.music.fragment.FavoriteSongsFragment;
import com.example.music.fragment.MediaPlaybackFragment;
import com.example.music.service.MediaPlaybackService;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class ActivityMusic extends AppCompatActivity implements IClickItem, IMediaControl, View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener, IFavoriteControl, AudioManager.OnAudioFocusChangeListener {

    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final String PRF_INDEX_KEY = "shared index key";
    public static final String MESSAGE_BROADCAST_UPDATE_UI = "MESSAGE_BROADCAST_UPDATE_UI";
    public static final String BUNDLE_SONG_KEY = "BUNDLE_SONG_KEY";
    public static final String PRF_IS_PORTRAIT = "is portrait";

    private RelativeLayout mInfoLayout;
    private TextView mTvTitle, mTvArtist;
    private ImageView mImgArt, mActionPlay;
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private Song mSong;
    private ArrayList<Song> mArraySongs;
    private int mPosition;
    private MediaPlaybackService mService;
    private boolean mIsShuffle;
    private String mRepeat;
    private SharedPreferences mSharedPrf;
    private SharedPreferences.Editor mEditor;
    private boolean mIsPortrait;
    private BroadcastReceiver mBroadcast;
    private boolean mBound;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private BaseSongListFragment mBaseFragment;
    private int mIndexNavigation = 0;
    private int mId;

    public class BroadcastMusic extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(MESSAGE_BROADCAST_UPDATE_UI)) {
                mArraySongs = mService.getArraySongs();
                mPosition = mService.getPosition();
                mSong = mArraySongs.get(mPosition);
                setSongToFavoriteDB(mSong.getmId());

                //if app in portrait mode
                if (mIsPortrait) {
                    setSongInfo(mSong);

                    //at MediaPlayback Fragment
                    if (findViewById(R.id.mediaPlayback_layout) != null) {
                        mMediaPlaybackFragment.setSongInfo(mSong);
                        mMediaPlaybackFragment.setImgPlay(mService.isPlaying());
                    } else {
                        setImgPlay(mService.isPlaying());
                    }
                }

                //if app in landscape mode
                else {
                    updateUIMediaPlayback();
                    setMediaPlaybackService();
                }

                //set animation of Equalizer view
                setAnimation();

            }
        }
    }

    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        mActionPlay = findViewById(R.id.img_action_play);
        mInfoLayout = findViewById(R.id.song_info);
        mTvTitle = findViewById(R.id.sub_title);
        mTvArtist = findViewById(R.id.sub_artist);
        mImgArt = findViewById(R.id.sub_art);
        mIsPortrait = findViewById(R.id.frame_mediaPlayback) == null;
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        //add AllSongsFragment to Activity
        mSharedPrf = getSharedPreferences(MediaPlaybackService.PRF_NAME, MODE_PRIVATE);
        mEditor = mSharedPrf.edit();

        //if app in portrait mode
        if (mIsPortrait) {
            //event click play or pause
            mActionPlay.setOnClickListener(ActivityMusic.this);

            //event click song info
            mInfoLayout.setOnClickListener(ActivityMusic.this);
        }

        if (savedInstanceState != null) {
            mIndexNavigation = savedInstanceState.getInt(PRF_INDEX_KEY);

            if (mIndexNavigation == 0) {
                mBaseFragment = new AllSongsFragment(this);
                getSupportActionBar().setTitle(R.string.music_actionbar_title);
            } else {
                mBaseFragment = new FavoriteSongsFragment(this);
                getSupportActionBar().setTitle(R.string.favorite_actionbar_title);
            }
        } else {
            mBaseFragment = new AllSongsFragment(this);
        }
        navigationView.getMenu().getItem(mIndexNavigation).setChecked(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.all_song);
        mDrawerLayout.addDrawerListener(mToggle);
        navigationView.setNavigationItemSelectedListener(this);
        mEditor.putBoolean(PRF_IS_PORTRAIT, mIsPortrait);
        mEditor.apply();

        //
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PRF_INDEX_KEY, mIndexNavigation);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_all_songs:
                if (mIndexNavigation != 0) {
                    getSupportActionBar().setTitle(R.string.music_actionbar_title);
                    mBaseFragment = new AllSongsFragment(this);
                    mIndexNavigation = 0;
                }
                break;

            case R.id.nav_favorite:
                if (mIndexNavigation != 1) {
                    getSupportActionBar().setTitle(R.string.favorite_actionbar_title);
                    mBaseFragment = new FavoriteSongsFragment(this);
                    mIndexNavigation = 1;
                }
                break;
        }

        if (mId != -1) {
            mBaseFragment.setService(mService);
        }

        if (mMediaPlaybackFragment != null) {
            if (mIsPortrait && mMediaPlaybackFragment.isAdded()) {
                getSupportFragmentManager().beginTransaction().remove(mBaseFragment).commit();

                getSupportFragmentManager().popBackStack();
                getSupportActionBar().show();
                mInfoLayout.setVisibility(View.VISIBLE);
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.all_song, mBaseFragment).commit();
        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //request permission to get data from storage
        requestPermission();

        //register BroadcastMusic
        registerReceiver();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mIsPortrait) {
                if (mMediaPlaybackFragment != null) {
                    //check Media Player is play or not to set play icon
                    checkPlaying();

                    mInfoLayout.setVisibility(View.VISIBLE);
                    setSongInfo(mSong);

                    //show Action Bar, InfoLayout
                    getSupportActionBar().show();

                    //set animation of Equalizer view
                    setAnimation();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAudioManager.abandonAudioFocus(this);

        //unbound Service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        if (mSong != null) {
            mEditor.putInt(MediaPlaybackService.PRF_ID, mSong.getmId());
            mEditor.commit();
        }

        //unregister Receiver
        this.unregisterReceiver(mBroadcast);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && requestCode == REQUEST_PERMISSION_CODE && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSupportFragmentManager().beginTransaction().replace(R.id.all_song, mBaseFragment).commit();

                //start MediaPlaybackService
                startMediaPlaybackService();
            } else {
                finish();
            }
        }
    }

    private void requestPermission() {

        //if permission is denied -> request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        } else {
            if (mMediaPlaybackFragment == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.all_song, mBaseFragment).commit();
            }

            //start MediaPlaybackService
            startMediaPlaybackService();
        }
    }

    private void startMediaPlaybackService() {
        Intent intent = new Intent(this, MediaPlaybackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    private void registerReceiver() {
        mBroadcast = new BroadcastMusic();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MESSAGE_BROADCAST_UPDATE_UI);
        this.registerReceiver(mBroadcast, filter);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mBound = true;
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) iBinder;

            //get MediaPlaybackService from iBinder
            mService = binder.getService();
            mArraySongs = mService.getArraySongs();
            SongAdapter adapter = mBaseFragment.getAdapter();

            if (mArraySongs == null) {
                mArraySongs = adapter.getArr();
            }

            //send ArraySongs to MediaPlaybackService
            mService.setArraySongs(mArraySongs);

            mId = mSharedPrf.getInt(MediaPlaybackService.PRF_ID, -1);
            if (mId != -1) {
                int i = -1;
                do {
                    i++;
                    mSong = mArraySongs.get(i);
                } while (mSong.getmId() != mId);
                mPosition = i;
            }

            //set shuffle, repeat variable
            mService.setShuffle(mIsShuffle);
            mService.setRepeat(mRepeat);
            mService.setPosition(mPosition);

            //if app in landscape mode
            if (!mIsPortrait) {

                if (mId != -1) {
                    mSong = mArraySongs.get(mPosition);
                    setSongToFavoriteDB(mSong.getmId());

                    //update real time of song
                    updateUIMediaPlayback();
                    setMediaPlaybackService();
                    setShuffleAndRepeat(mIsShuffle, mRepeat);
                }
            }

            //if app in portrait mode
            else {
                // if app opened 2nd time onwards
                if (mId != -1 && findViewById(R.id.mediaPlayback_layout) == null) {

                    //initialize InfoSongLayout
                    mInfoLayout.setVisibility(View.VISIBLE);

                    //set info song to layout
                    setSongInfo(mSong);

                    //check Media Player is playing or not to set play icon
                    checkPlaying();
                }
            }

            //set animation of Equalizer view
            setAnimation();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    public void onAudioFocusChange(int i) {
        if (i <= 0) {
            //LOSS -> PAUSE
            mService.pauseSong();
        } else {
            //GAIN -> PLAY
            mService.resumeSong();
        }
    }

    @Override
    public void onClickItem(int position) {
        mArraySongs = mBaseFragment.getArraySongs();
        mPosition = position;
        mService.setPosition(mPosition);
        mService.setArraySongs(mArraySongs);
        mSong = mArraySongs.get(mPosition);
        mId = mSong.getmId();

        //play song
        mService.playSong();

        //set animation of Equalizer view
        setAnimation();

        //check app in landscape or portrait mode
        // in portrait mode
        if (mIsPortrait) {
            //set song info layout
            setSongInfo(mSong);

            //check Media Player is playing or not to set play icon
            checkPlaying();

            //initialize InfoSongLayout
            mInfoLayout.setVisibility(View.VISIBLE);
        }
        //in landscape mode
        else {
            updateUIMediaPlayback();
            setMediaPlaybackService();
        }

        // add songs to Favorite Songs database
        FavoriteSongsDB favoriteSongsDB = new FavoriteSongsDB(getApplicationContext());
        favoriteSongsDB.addToFavoriteDB(mId);
    }

    private void setSongInfo(Song song) {
        mTvTitle.setText(song.getmTitle());
        mTvArtist.setText(song.getmArtist());
        mImgArt.setImageBitmap(mSong.getAlbumArt(getApplicationContext(), mSong.getmResource()));
    }

    private void checkPlaying() {
        if (mService.isPlaying()) {
            mActionPlay.setImageResource(R.drawable.ic_media_pause);
        } else {
            mActionPlay.setImageResource(R.drawable.ic_media_play);
        }
    }

    //methods play or pause, next, prev
    @Override
    public void onClick(int id, boolean isPlaying) {

        //set animation of Equalizer view
        setAnimation();
    }

    @Override
    public void onClickShuffle(boolean isShuffle) {
        mIsShuffle = isShuffle;
        mService.setShuffle(mIsShuffle);
    }

    @Override
    public void onClickRepeat(String repeat) {
        mRepeat = repeat;
        mService.setRepeat(mRepeat);
    }

    @Override
    public void onClickList() {
        getSupportFragmentManager().beginTransaction().remove(mMediaPlaybackFragment).commit();
        getSupportFragmentManager().popBackStack();
        mInfoLayout.setVisibility(View.VISIBLE);
        getSupportActionBar().show();
    }

    private void setImgPlay(boolean isPlaying) {
        if (isPlaying) {
            mActionPlay.setImageResource(R.drawable.ic_media_pause);
        } else {
            mActionPlay.setImageResource(R.drawable.ic_media_play);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_action_play:
                if (mService.isPlaying()) {
                    mService.pauseSong();
                } else {
                    if (mService.getCurrentTime() > 0) {
                        mService.resumeSong();
                    } else
                        mService.playSong();
                }
                setImgPlay(mService.isPlaying());

                //set animation of Equalizer view
                setAnimation();
                break;

            case R.id.song_info:
                getSupportActionBar().hide();

                mMediaPlaybackFragment = new MediaPlaybackFragment(this, this);
                sendBundleToMediaPlaybackFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.all_song, mMediaPlaybackFragment).addToBackStack(null).commit();

                //hide InfoLayout
                mInfoLayout.setVisibility(View.GONE);

                setShuffleAndRepeat(mIsShuffle, mRepeat);

                //update real time of song
                setMediaPlaybackService();

                setSongToFavoriteDB(mSong.getmId());
                break;
        }
    }

    private void setShuffleAndRepeat(boolean isShuffle, String repeat) {
        mMediaPlaybackFragment.setShuffle(isShuffle);
        mMediaPlaybackFragment.setRepeat(repeat);
    }

    private void setMediaPlaybackService() {
        mMediaPlaybackFragment.setService(mService);
    }

    private void sendBundleToMediaPlaybackFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_SONG_KEY, mSong);
        mMediaPlaybackFragment.setArguments(bundle);
    }

    private void updateUIMediaPlayback() {
        mMediaPlaybackFragment = new MediaPlaybackFragment(this, this);
        sendBundleToMediaPlaybackFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_mediaPlayback, mMediaPlaybackFragment).commit();
    }

    private void setAnimation() {
        if (mId != -1) {
            if (mPosition == 0) {
                mBaseFragment.setAnimation(mPosition, mSong.getmId(), mService.isPlaying());
            } else {
                mBaseFragment.setAnimation(mPosition + 1, mSong.getmId(), mService.isPlaying());
            }
        }
    }

    public void setSongToFavoriteDB(int id) {
        Cursor cursor = getContentResolver().query(SongProvider.CONTENT_URI, new String[]{FavoriteSongsDB.ID_PROVIDER,
                FavoriteSongsDB.IS_FAVORITE}, FavoriteSongsDB.ID_PROVIDER + "=" + id + " AND " +
                FavoriteSongsDB.IS_FAVORITE + "=" + 2, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                mSong.setmIsFavorite(true);
            }
        }
    }

    @Override
    public void onClickFavorite() {
        if (!mIsPortrait) {
            if (mIndexNavigation == 1) {
                mBaseFragment.updateAdapter();
            }
        }
    }

    @Override
    public void updateUI(int id, boolean favorite) {
        if (mMediaPlaybackFragment != null) {
            mMediaPlaybackFragment.setFavorite(id, favorite);
        }
    }

}