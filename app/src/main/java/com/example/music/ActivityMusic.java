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
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.music.Interface.IClickItem;
import com.example.music.Interface.IMediaControl;
import com.example.music.Interface.IPassData;
import com.example.music.adapter.SongAdapter;
import com.example.music.fragment.AllSongsFragment;
import com.example.music.fragment.MediaPlaybackFragment;
import com.example.music.service.MediaPlaybackService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ActivityMusic extends AppCompatActivity implements IClickItem, IPassData, IMediaControl, View.OnClickListener {

    private static final int REQUEST_PERMISSION_CODE = 1;
    public static final String ACTION_PLAY_COMPLETE = "ACTION_PLAY_COMPLETE";
    public static final String BUNDLE_SONG_KEY = "BUNDLE_SONG_KEY";
    public static final String BUNDLE_IS_PLAYING = "BUNDLE_IS_PLAYING";
    //TODO TrungTH những cái bên dưới này nên đặt là hằng số cho tiện so sánh và đỡ tốn tài nguyên
    public final String FALSE = "false";
    public final String TRUE = "true";
    public final String REPEAT = "repeat";


    private ActionBar mActionBar;
    private RelativeLayout mInfoLayout;
    private TextView mTvTitle, mTvArtist;
    private ImageView mImgArt, mActionPlay;
    private AllSongsFragment mAllSongsFragment;
    private MediaPlaybackFragment mMediaPlaybackFragment;
    private Song mSong;
    private ArrayList<Song> mArraySongs;
    private SongAdapter mAdapter;
    private int mPosition;
    private MediaPlaybackService mService;
    private MediaPlayer mPlayer;
    private Handler mHandler;
    private boolean mIsShuffle;
    private String mRepeat;
    private SharedPreferences mSharedPrf;
    private boolean mIsPortrait;
    private int mCurrentTime;
    private int mDuration;
    private BroadcastReceiver mBroadcast;
    private boolean mBound;
    private Bundle mBundle;

    public class BroadcastMusic extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_PLAY_COMPLETE)) {
                mPosition = intent.getIntExtra(MediaPlaybackService.BROAD_POSITION, 0);
                mSong = mArraySongs.get(mPosition);

                //if app in portrait mode
                if (mIsPortrait) {
                    setSongInfo(mSong);

                    //at MediaPlayback Fragment
                    if (findViewById(R.id.mediaPlayback_layout) != null) {
                        mMediaPlaybackFragment.setSongInfo(mSong);
                    }
                }

                //if app in landscape mode
                else {
                    mMediaPlaybackFragment.setSongInfo(mSong);
                    if (mService.isPlaying()) {
                        mMediaPlaybackFragment.setImgPlay(R.drawable.ic_action_pause);
                    } else {
                        mMediaPlaybackFragment.setImgPlay(R.drawable.ic_action_play);
                    }
//                    sendBundleToMediaPlaybackFragment();
                }

                mAdapter.setPositionClicked(mPosition);
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
        // TODO TrungTH khởi tạo services như này để làm gì đây ?
//        mService = new MediaPlaybackService();

        //TODO TrungTH khởi tạo ở đây chưa chuẩn
//        mMediaPlaybackFragment = new MediaPlaybackFragment(ActivityMusic.this, ActivityMusic.this);

        if (mIsPortrait) {

            //event click play or pause
            mActionPlay.setOnClickListener(ActivityMusic.this);

            //event click song info
            mInfoLayout.setOnClickListener(ActivityMusic.this);
        } else {

        }

        //if app in landscape mode
//        else {
//            getSupportFragmentManager().beginTransaction().replace(R.id.frame_mediaPlayback, mMediaPlaybackFragment).commit();
//        }

        //get data from SharedPreference -> set variable of shuffle, repeat, stop
//        mSharedPrf = getSharedPreferences(mService.PRF_NAME, MODE_PRIVATE);
//        mRepeat = mSharedPrf.getString(mService.PRF_REPEAT, FALSE);
//        mIsShuffle = mSharedPrf.getBoolean(mService.PRF_SHUFFLE, false);
//        mPosition = mSharedPrf.getInt(mService.PRF_POSITION, -1);
        // TODO TrungTH có cần những biến này ở đây không ?

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

        //get data from SharedPreference -> set variable of shuffle, repeat, stop
        getDataSharedPrf();
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
        super.onBackPressed();
        // TODO TrungTH : đoạn dưới này xử lý cho trường hợp từ playback sang all all song ? thế từ all song cũng chạy à?
        // DONE đã kiểm tra nếu ờ màn hình dọc thì sẽ xử lí

        if (mIsPortrait) {
            //check Media Player is play or not to set play icon
            checkPlaying();

            //show Action Bar, InfoLayout
            mActionBar.show();
            mInfoLayout.setVisibility(View.VISIBLE);
            setSongInfo(mSong);
            mAdapter.setPositionClicked(mPosition);

            getDataFromStorage();
        }
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
        mFilter.addAction(ACTION_PLAY_COMPLETE);
        this.registerReceiver(mBroadcast, mFilter);
    }

    private void getDataSharedPrf() {
        mSharedPrf = getSharedPreferences(mService.PRF_NAME, MODE_PRIVATE);
        mRepeat = mSharedPrf.getString(mService.PRF_REPEAT, FALSE);
        mIsShuffle = mSharedPrf.getBoolean(mService.PRF_SHUFFLE, false);
        mPosition = mSharedPrf.getInt(mService.PRF_POSITION, -1);
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
            mAdapter = mAllSongsFragment.getAdapter();
            mArraySongs = mAdapter.getArraySongs();

            //send position to Adapter
            if (mPosition != -1) {
                mAdapter.setPositionClicked(mPosition);
            }

            //send ArraySongs to MediaPlaybackService
            mService.setArraySongs(mArraySongs);
            mService.setPosition(mPosition);

            //set shuffle, repeat variable
            mService.setShuffle(mIsShuffle);
            mService.setRepeat(mRepeat);

            //if app in landscape mode
            if (!mIsPortrait) {

                if (mPosition == -1) mPosition = 0;

                mSong = mArraySongs.get(mPosition);

                // TODO TrungTH đưa hàm vào trong lớp mMediaPlaybackFragment giảm thiểu sự phụ thuộc của lớp mMediaPlaybackFragment vào activity
                //  => chưa hiểu rõ ý nghĩa của fragment thì phải -
                mMediaPlaybackFragment = new MediaPlaybackFragment(ActivityMusic.this, ActivityMusic.this);
                sendBundleToMediaPlaybackFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.frame_mediaPlayback, mMediaPlaybackFragment).commit();

                //update real time of song
                updateTimeSong();
                setShuffleAndRepeat(mIsShuffle, mRepeat);

            }

            //if app in portrait mode
            else {

                // if app opened 2nd time onwards
                if (mPosition != -1 && findViewById(R.id.mediaPlayback_layout) == null) {
                    mSong = mArraySongs.get(mPosition);

                    mArraySongs = mAdapter.getArraySongs();
                    //get Song from ArraySongs
                    mSong = mArraySongs.get(mPosition);

                    //initialize InfoSongLayout
                    mInfoLayout.setVisibility(View.VISIBLE);

                    //set info song to layout
                    setSongInfo(mSong);

                    //check Media Player is playing or not to set play icon
                    checkPlaying();
                }

                //TODO TrungTH lắng sự kiện click để ở chỗ find view
                //event click play or pause
//                mActionPlay.setOnClickListener(ActivityMusic.this);
//
//                //event click song info
//                mInfoLayout.setOnClickListener(ActivityMusic.this);
            }

//            mDuration = mSharedPrf.getInt(mService.PRF_DURATION, 0);
//            mCurrentTime = mSharedPrf.getInt(mService.PRF_CURRENT_TIME, 0);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    public void onClickItem(int position) {
        //TODO TrungTH tinh chỉnh lại đoạn này cho gọn lại , ko lặp code
        //get Array Songs from Adapter
        mAdapter = mAllSongsFragment.getAdapter();
        mArraySongs = mAdapter.getArraySongs();

        mPosition = position;
        mService.setPosition(mPosition);
        mService.setArraySongs(mArraySongs);

        //set position clicked in Adapter
        mAdapter.setPositionClicked(mPosition);

        //play song
        mService.playSong();

        //get Song from ArraySongs
        mSong = mArraySongs.get(mPosition);

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
            Log.d("ToanNTe", "onClickItem: " + mService.isPlaying());
//            sendBundleToMediaPlaybackFragment();
            mMediaPlaybackFragment.setSongInfo(mSong);
            sendBundleToMediaPlaybackFragment();
        }
    }

    private void setSongInfo(Song song) {
        mSong = song;
        mTvTitle.setText(mSong.getTitle());
        mTvArtist.setText(mSong.getArtist());
        mSong.setImage(this, mImgArt);
    }

    private void checkPlaying() {
        if (mService.isPlaying()) {
            mActionPlay.setImageResource(R.drawable.ic_media_pause);
        } else {
            mActionPlay.setImageResource(R.drawable.ic_media_play);
        }
    }

    //receive position of SeekBar from MediaPlaybackFragment
    @Override
    public void positionSeekBarReceived(int currentPosition) {
        mPlayer.seekTo(currentPosition);
    }

    //methods play or pause, next, prev
    @Override
    public void onClickPlay(ImageView imageView) {
        if (mService.isPlaying()) {
            mMediaPlaybackFragment.setImgPlay(R.drawable.ic_action_play);
            mService.pauseSong();
        } else {
            mService.resumeSong();

            mMediaPlaybackFragment.setImgPlay(R.drawable.ic_action_pause);
        }
    }

    private void setSongInfoFragment() {
        mPosition = mService.getPosition();
        mSong = mArraySongs.get(mPosition);
        mMediaPlaybackFragment.setSongInfo(mSong);
    }

    @Override
    public void onClickNext(ImageView imageView) {
        mService.playNext();
        if (mIsShuffle) {
            mService.playShuffle();
        }

        setSongInfoFragment();

        //set position clicked in Adapter
        mAdapter.setPositionClicked(mPosition);
    }

    @Override
    public void onClickPrev(ImageView imageView) {
        if (mService.getPlayer().getCurrentPosition() < 3000) {
            mService.playPrev();
        } else {
            mService.playSong();
        }

        setSongInfoFragment();

        //set position clicked in Adapter
        mAdapter.setPositionClicked(mPosition);
    }

    //method update real time of song
    // TODO TrungTH Thường thì chỉ cần 1 s chạy 1 lần, ngoài ra giao diện playback
    //  quan tâm đến việc update thì đặt code trong giao diện play back,
    //  thoát app cũng ko thấy cancel cái này

    private void updateTimeSong() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                mPlayer = mService.getPlayer();
                mCurrentTime = mPlayer.getCurrentPosition();
                mDuration = mPlayer.getDuration();
                mMediaPlaybackFragment.setCurrentTime(format.format(mCurrentTime));
                mMediaPlaybackFragment.setCurrentSeekBar(mCurrentTime, mDuration);
                mHandler.postDelayed(this, 1000);
            }
        }, 0);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_action_play:
                if (mService.isPlaying()) {
                    mService.pauseSong();
                    mActionPlay.setImageResource(R.drawable.ic_media_play);
                } else {
                    Log.d("ToanNTe", "onClick: " + mPlayer);
                    if (mPlayer == null) {
                        mService.playSong();
                        mPlayer = mService.getPlayer();
                    } else {
                        mService.resumeSong();
                    }
                    mActionPlay.setImageResource(R.drawable.ic_media_pause);
                }
                break;

            case R.id.song_info:
                //hide Action bar
                if (mActionBar != null) {
                    mActionBar.hide();
                }
                mMediaPlaybackFragment = new MediaPlaybackFragment(this, this);
                sendBundleToMediaPlaybackFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.all_song, mMediaPlaybackFragment).addToBackStack(null).commit();

                //hide InfoLayout
                mInfoLayout.setVisibility(View.GONE);

                setShuffleAndRepeat(mIsShuffle, mRepeat);

                //update real time of song
                updateTimeSong();
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
        mBundle.putBoolean(BUNDLE_IS_PLAYING, mService.isPlaying());
        mMediaPlaybackFragment.setArguments(mBundle);
    }

}