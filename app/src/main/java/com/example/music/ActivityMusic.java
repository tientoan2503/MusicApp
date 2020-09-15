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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.music.Interface.IClickItem;
import com.example.music.Interface.IMediaControl;
import com.example.music.fragment.AllSongsFragment;
import com.example.music.fragment.MediaPlaybackFragment;
import com.example.music.service.MediaPlaybackService;

import java.util.ArrayList;

public class ActivityMusic extends AppCompatActivity implements IClickItem, IMediaControl, View.OnClickListener {

    private static final int REQUEST_PERMISSION_CODE = 1;
    public static final String MESSAGE_BROADCAST_UPDATE_UI = "MESSAGE_BROADCAST_UPDATE_UI";
    public static final String BUNDLE_SONG_KEY = "BUNDLE_SONG_KEY";
    public static final String FALSE = "false";
    public static final String TRUE = "true";
    public static final String REPEAT = "repeat";


    private ActionBar mActionBar;
    private RelativeLayout mInfoLayout;
    private TextView mTvTitle, mTvArtist;
    private ImageView mImgArt, mActionPlay;
    private AllSongsFragment mAllSongsFragment;
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
    private Bundle mBundle;

    public class BroadcastMusic extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MESSAGE_BROADCAST_UPDATE_UI)) {

                mPosition = intent.getIntExtra(MediaPlaybackService.BROAD_POSITION, 0);
                mSong = mArraySongs.get(mPosition);

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
                mAllSongsFragment.setAnimation(mSong.getmId(), mService.isPlaying());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        mActionPlay = findViewById(R.id.img_action_play);
        mInfoLayout = findViewById(R.id.song_info);
        mActionBar = getSupportActionBar();
        mTvTitle = findViewById(R.id.sub_title);
        mTvArtist = findViewById(R.id.sub_artist);
        mImgArt = findViewById(R.id.sub_art);
        mIsPortrait = findViewById(R.id.frame_mediaPlayback) == null;

        //add AllSongsFragment to Activity
        mAllSongsFragment = new AllSongsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.all_song, mAllSongsFragment).commit();

        mSharedPrf = getSharedPreferences(MediaPlaybackService.PRF_NAME, MODE_PRIVATE);
        mEditor = mSharedPrf.edit();


        if (mIsPortrait) {
            //event click play or pause
            mActionPlay.setOnClickListener(ActivityMusic.this);

            //event click song info
            mInfoLayout.setOnClickListener(ActivityMusic.this);
        } else {
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        //request permission to get data from storage
        requestPermission();

        //start MediaPlayback Service
        //TODO TrungTH có quyền rồi thì mới nên bind services ? -> DONE
//        if (mIntent == null) {
//            mIntent = new Intent(this, MediaPlaybackService.class);
//            bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
//            startService(mIntent);
//        }

        //TODO TrungTH đặt tên cho rõ ràng hơn, ngoài ra registerReceiver với unregisterReceiver code sai chỗ
        // Thường khi nào app hiển thị mới cần lắng nghe => em xem lại vòng đời như nào và đặt cho chuẩn
        //register BroadcastMusic
//        mBroadcast = new BroadcastMusic();
//        mFilter = new IntentFilter();
//        mFilter.addAction(ACTION_PLAY_COMPLETE);
//        this.registerReceiver(mBroadcast, mFilter);
    }

    protected void onResume() {
        super.onResume();

        //register BroadcastMusic
        registerReceiver();

        //get adapter from AllSongsFragment
//        //TODO TrungTH lấy đối tượng mAdapter này ra đây làm gì ? ko cần thiết => xem lại
//        mAdapter = mAllSongsFragment.getAdapter();
//        mArraySongs = mAdapter.getArraySongs();

        //TODO TrungTH trường hợp dọc có cần thiết gọi cái này không ? -> DONE Đã check trường hợp dọc thì không set nữa
//        if (!mIsPortrait) {
//            setShuffleAndRepeat(mIsShuffle, mRepeat);
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(mMediaPlaybackFragment.isAdded()){
                //check Media Player is play or not to set play icon
                checkPlaying();

                //show Action Bar, InfoLayout
                mActionBar.show();
                mInfoLayout.setVisibility(View.VISIBLE);
                setSongInfo(mSong);
        }

        super.onBackPressed();
        getDataFromStorage();
        mAllSongsFragment.setAnimation(mSong.getmId(), mService.isPlaying());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregister Receiver
        this.unregisterReceiver(mBroadcast);

        //unbound Service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        mEditor.putInt(MediaPlaybackService.PRF_POSITION, mPosition);
        mEditor.commit();

    }

    private void getDataFromStorage() {
        mAllSongsFragment.initRecyclerView();
        mAllSongsFragment.getAllSongs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && requestCode == REQUEST_PERMISSION_CODE && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if permission granted -> get data from external storage
                getDataFromStorage();

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
            getDataFromStorage();

            //start MediaPlaybackService
            startMediaPlaybackService();
        }
    }

    private void startMediaPlaybackService() {
        Intent mIntent = new Intent(this, MediaPlaybackService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(mIntent);
    }

    private void registerReceiver() {
        mBroadcast = new BroadcastMusic();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(MESSAGE_BROADCAST_UPDATE_UI);
        this.registerReceiver(mBroadcast, mFilter);
    }

    //create icon Search on Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mBound = true;
            MediaPlaybackService.LocalBinder binder = (MediaPlaybackService.LocalBinder) iBinder;

            //get MediaPlaybackService from iBinder
            mService = binder.getService();
            mArraySongs = mAllSongsFragment.getArraySongs();
            mPosition = mSharedPrf.getInt(MediaPlaybackService.PRF_POSITION, -1);

            //send ArraySongs to MediaPlaybackService
            mService.setArraySongs(mArraySongs);
            mService.setPosition(mPosition);

            //set shuffle, repeat variable
            mService.setShuffle(mIsShuffle);
            mService.setRepeat(mRepeat);

            //if app in landscape mode
            if (!mIsPortrait) {

                if (mPosition == -1) {
                    mPosition = 0;
                }
                mSong = mArraySongs.get(mPosition);
                //send position to Adapter
                // TODO TrungTH đưa hàm vào trong lớp mMediaPlaybackFragment giảm thiểu sự phụ thuộc của lớp mMediaPlaybackFragment vào activity
                //  => chưa hiểu rõ ý nghĩa của fragment thì phải

                //update real time of song
                updateUIMediaPlayback();
                setMediaPlaybackService();
                setShuffleAndRepeat(mIsShuffle, mRepeat);
                mAllSongsFragment.setAnimation(mSong.getmId(), mService.isPlaying());

            }

            //if app in portrait mode
            else {

                // if app opened 2nd time onwards
                if (mPosition != -1 && findViewById(R.id.mediaPlayback_layout) == null) {

                    mSong = mArraySongs.get(mPosition);

                    //initialize InfoSongLayout
                    mInfoLayout.setVisibility(View.VISIBLE);

                    //set info song to layout
                    setSongInfo(mSong);

                    //check Media Player is playing or not to set play icon
                    checkPlaying();
                    mAllSongsFragment.setAnimation(mSong.getmId(), mService.isPlaying());
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    public void onClickItem(int position) {
        //TODO TrungTH tinh chỉnh lại đoạn này cho gọn lại , ko lặp code


        mPosition = position;
        mService.setPosition(mPosition);
        mService.setArraySongs(mArraySongs);
        mSong = mArraySongs.get(mPosition);

        //play song
        mService.playSong();

        //send song id when click
        mAllSongsFragment.setAnimation(mSong.getmId(), mService.isPlaying());

        /*check app in landscape or portrait mode
         ** in portrait mode*/
        if (mIsPortrait) {

            //initialize InfoSongLayout
            mInfoLayout.setVisibility(View.VISIBLE);

            //set song info layout
            setSongInfo(mSong);

            //check Media Player is playing or not to set play icon
            checkPlaying();
        }
        //in landscape mode
        else {
            updateUIMediaPlayback();
            setMediaPlaybackService();
        }
    }

    private void setSongInfo(Song song) {
        mTvTitle.setText(song.getmTitle());
        mTvArtist.setText(song.getmArtist());
//        mSong.setImage(this, mImgArt);
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
    public void onClickPlay(int id, boolean isPlaying) {

        //set animation of Equalizer view
        mAllSongsFragment.setAnimation(id, isPlaying);
    }

    @Override
    public void onClickNext(int id, boolean isPlaying) {

        //set animation of Equalizer view
        mAllSongsFragment.setAnimation(id, isPlaying);

    }

    @Override
    public void onClickPrev(int id, boolean isPlaying) {

        //set animation of Equalizer view
        mAllSongsFragment.setAnimation(id, isPlaying);

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
//                    if (mPlayer == null) {
//                        mService.playSong();
//                        mPlayer = mService.getPlayer();
//                    } else {
//                        mService.resumeSong();
//                    }
                    mService.resumeSong();
                }
                setImgPlay(mService.isPlaying());
                //set animation of Equalizer view
                mAllSongsFragment.setAnimation(mSong.getmId(), mService.isPlaying());
                break;

            case R.id.song_info:
                //hide Action bar
                if (mActionBar != null) {
                    mActionBar.hide();
                }

                mMediaPlaybackFragment = new MediaPlaybackFragment(this);
                sendBundleToMediaPlaybackFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.all_song, mMediaPlaybackFragment).addToBackStack(null).commit();

                //hide InfoLayout
                mInfoLayout.setVisibility(View.GONE);

                setShuffleAndRepeat(mIsShuffle, mRepeat);

                //update real time of song
                setMediaPlaybackService();
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
        mBundle = new Bundle();
        mBundle.putParcelable(BUNDLE_SONG_KEY, mSong);
        mMediaPlaybackFragment.setArguments(mBundle);
    }

    private void updateUIMediaPlayback() {
        mMediaPlaybackFragment = new MediaPlaybackFragment(this);
        sendBundleToMediaPlaybackFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_mediaPlayback, mMediaPlaybackFragment).commit();
    }

}