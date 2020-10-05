package com.example.music.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music.Interface.IFavoriteControl;
import com.example.music.Interface.IMediaControl;
import com.example.music.Song;
import com.example.music.database.FavoriteSongsDB;
import com.example.music.service.MediaPlaybackService;
import com.example.music.ActivityMusic;
import com.example.music.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MediaPlaybackFragment extends Fragment implements View.OnClickListener {

    private Song mSong;

    public final String FALSE = "false";
    public final String TRUE = "true";
    public final String REPEAT = "repeat";

    private ImageView mImgArtTop,
            mImgQueue, mImgFavorite, mImgRepeat, mImgPrev, mImgPlay,
            mImgNext, mImgShuffle, mImgSongArt;
    private TextView mTvSongTitle, mTvArtist, mTvCurrentTime, mTvTotalTime;
    private SeekBar mSbDuration;
    private IMediaControl mMediaControl;
    private IFavoriteControl mFavoriteControl;

    private boolean mIsShuffle;
    private String mRepeat;
    private boolean mIsFavorite = false;
    private MediaPlaybackService mService;
    private Handler mHandler;
    private Runnable mRunnable;
    private ArrayList<Song> mArraySongs;
    private int mPosition;
    private BaseSongListFragment mBaseFragment;


    public MediaPlaybackFragment(IMediaControl mediaControl, IFavoriteControl favoriteControl) {
        mFavoriteControl = favoriteControl;
        mMediaControl = mediaControl;
    }

    public MediaPlaybackFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_playback, container, false);
        mImgArtTop = view.findViewById(R.id.sub_art_top);
        mImgSongArt = view.findViewById(R.id.music_art);
        mImgQueue = view.findViewById(R.id.img_queu);
        mImgFavorite = view.findViewById(R.id.img_favorite);
        mImgRepeat = view.findViewById(R.id.img_repeat);
        mImgPrev = view.findViewById(R.id.img_prev);
        mImgPlay = view.findViewById(R.id.img_play);
        mImgNext = view.findViewById(R.id.img_next);
        mImgShuffle = view.findViewById(R.id.img_shuffle);
        mTvSongTitle = view.findViewById(R.id.sub_title_top);
        mTvArtist = view.findViewById(R.id.sub_artist_top);
        mTvCurrentTime = view.findViewById(R.id.tv_current_time);
        mTvTotalTime = view.findViewById(R.id.tv_total_time);
        mSbDuration = view.findViewById(R.id.sb_duration);

        //event click play, pause, next, prev, shuffle, repeat, queue music
        mImgPlay.setOnClickListener(this);
        mImgNext.setOnClickListener(this);
        mImgPrev.setOnClickListener(this);
        mImgRepeat.setOnClickListener(this);
        mImgShuffle.setOnClickListener(this);
        mImgQueue.setOnClickListener(this);
        mImgFavorite.setOnClickListener(this);

        //event seek bar change
        seekBarChange();

        if (mService != null) {
            updateTimeSong();
            mHandler.postDelayed(mRunnable, 0);
        }

        SharedPreferences mSharedPrf = getActivity().getSharedPreferences(MediaPlaybackService.PRF_NAME, MODE_PRIVATE);
        mIsShuffle = mSharedPrf.getBoolean(MediaPlaybackService.PRF_SHUFFLE, false);
        mRepeat = mSharedPrf.getString(MediaPlaybackService.PRF_REPEAT, FALSE);

        if (mService != null) {
            checkPlaying(mService.isPlaying());
        }

        Bundle mBundle = getArguments();
        if (mBundle != null) {
            mSong = mBundle.getParcelable(ActivityMusic.BUNDLE_SONG_KEY);
            setSongInfo(mSong);
        }

        mIsFavorite = mSong.getIsIsFavorite();

        checkShuffle();
        checkRepeat();
        checkFavorite(mIsFavorite);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void checkShuffle() {
        if (mIsShuffle) {
            setImgShuffle(R.drawable.ic_play_shuffle_selected);
        } else {
            setImgShuffle(R.drawable.ic_play_shuffle_default);
        }
    }

    private void checkRepeat() {
        switch (mRepeat) {
            case FALSE:
                setImgRepeat(R.drawable.ic_play_repeat_default);
                break;
            case TRUE:
                setImgRepeat(R.drawable.ic_play_repeat_selected);
                break;
            case REPEAT:
                setImgRepeat(R.drawable.ic_play_repeat_1);
                break;
        }
    }

    public void checkPlaying(boolean isPlaying) {
        if (isPlaying) {
            mImgPlay.setImageResource(R.drawable.ic_action_pause);
        } else {
            mImgPlay.setImageResource(R.drawable.ic_action_play);
        }
    }

    public void setFavorite(int id, boolean isFavorite) {
        mPosition = mService.getPosition();
        mArraySongs = mService.getArraySongs();
        mSong = mArraySongs.get(mPosition);
        int mId = mSong.getmId();
        mIsFavorite = isFavorite;
        if (id == mId) {
            checkFavorite(mIsFavorite);
        }
    }

    public void checkFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
        if (isFavorite) {
            setImgFavorite(R.drawable.ic_favorite_selected);
        } else {
            setImgFavorite(R.drawable.ic_favorite_default);
        }
    }

    public void setImgPlay(boolean isPlaying) {
        if (isPlaying) {
            mImgPlay.setImageResource(R.drawable.ic_action_pause);
        } else {
            mImgPlay.setImageResource(R.drawable.ic_action_play);
        }
    }

    public void setImgShuffle(int res) {
        mImgShuffle.setImageResource(res);
    }

    public void setShuffle(boolean isShuffle) {
        mIsShuffle = isShuffle;
    }

    public void setImgRepeat(int res) {
        mImgRepeat.setImageResource(res);
    }

    public void setRepeat(String repeat) {
        mRepeat = repeat;
    }

    public void setImgFavorite(int res) {
        mImgFavorite.setImageResource(res);
    }


    public void setSongInfo(Song song) {
        mSong = song;
        mIsFavorite = mSong.getIsIsFavorite();
        mTvSongTitle.setText(mSong.getmTitle());
        mTvArtist.setText(mSong.getmArtist());
        mTvTotalTime.setText(mSong.getmDuration());
        mImgArtTop.setImageBitmap(mSong.getAlbumArt(getContext(), mSong.getmResource()));
        mImgSongArt.setImageBitmap(mSong.getAlbumArt(getContext(), mSong.getmResource()));
        checkFavorite(mIsFavorite);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setCurrentTime(String currentTime) {
        mTvCurrentTime.setText(currentTime);
    }

    public void setCurrentSeekBar(int currentTime, int duration) {
        mSbDuration.setProgress(currentTime);
        mSbDuration.setMax(duration);
    }

    public void seekBarChange() {
        mSbDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mService.playerSeekTo(mSbDuration.getProgress());
            }
        });
    }

    @Override
    public void onClick(View view) {
        mArraySongs = mService.getArraySongs();
        mPosition = mService.getPosition();
        if (mPosition == -1) {
            mPosition = 0;
        }
        mService.setPosition(mPosition);

        switch (view.getId()) {

            //click play button
            case R.id.img_play:
                if (mService.isPlaying()) {
                    mService.pauseSong();
                } else {
                    if (mService.getCurrentTime() == 0) {
                        mService.playSong();
                    } else {
                        mService.resumeSong();
                    }
                }
                mSong = mArraySongs.get(mPosition);
                break;

            //click next button
            case R.id.img_next:
                mService.playNext();
                break;

            //click prev button
            case R.id.img_prev:
                mService.playPrev();
                break;

            //click shuffle
            case R.id.img_shuffle:
                if (mIsShuffle) {
                    mIsShuffle = false;
                } else {
                    mIsShuffle = true;
                }
                checkShuffle();

                mService.putShuffleToPrf(mIsShuffle);
                mMediaControl.onClickShuffle(mIsShuffle);
                break;

            //click repeat
            case R.id.img_repeat:
                switch (mRepeat) {
                    case FALSE:
                        mRepeat = TRUE;
                        break;
                    case TRUE:
                        mRepeat = REPEAT;
                        break;
                    case REPEAT:
                        mRepeat = FALSE;
                        break;
                }
                checkRepeat();
                mMediaControl.onClickRepeat(mRepeat);
                mService.putRepeatToPrf(mRepeat);
                break;

            //click favorite
            case R.id.img_favorite:
                FavoriteSongsDB favoriteSongsDB = new FavoriteSongsDB(getContext());
                if (mIsFavorite) {
                    mIsFavorite = false;
                    favoriteSongsDB.setFavorite(mSong.getmId(), 1);
                    Toast.makeText(mService, R.string.remove_favorite, Toast.LENGTH_SHORT).show();
                } else {
                    mIsFavorite = true;
                    favoriteSongsDB.addToFavoriteDB(mSong.getmId());
                    favoriteSongsDB.setFavorite(mSong.getmId(), 2);
                    Toast.makeText(mService, R.string.add_to_favorite, Toast.LENGTH_SHORT).show();
                }
                mFavoriteControl.onClickFavorite();
                mSong.setmIsFavorite(mIsFavorite);
                checkFavorite(mIsFavorite);
                break;

            case R.id.img_queu:
                mBaseFragment = new AllSongsFragment();
                getFragmentManager().beginTransaction().replace(R.id.list_song, mBaseFragment).addToBackStack(null).commit();

        }
        mMediaControl.onClick(mSong.getmId(), mService.isPlaying());
    }

    private void updateTimeSong() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                int mCurrentTime = mService.getCurrentTime();
                int mDuration = mService.getDuration();
                setCurrentTime(format.format(mCurrentTime));
                setCurrentSeekBar(mCurrentTime, mDuration);
                mHandler.postDelayed(this, 1000);
            }
        };
    }

    public void setService(MediaPlaybackService service) {
        mService = service;
    }
}
