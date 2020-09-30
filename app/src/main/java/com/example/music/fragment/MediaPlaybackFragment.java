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

import com.example.music.ActivityMusic;
import com.example.music.Interface.IFavoriteControl;
import com.example.music.Interface.IMediaControl;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.database.FavoriteSongsDB;
import com.example.music.service.MediaPlaybackService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class MediaPlaybackFragment extends Fragment implements View.OnClickListener {

    private Song mSong;

    private ImageView mImgArtTop, mImgMore,
            mImgQueue, mImgFavorite, mImgRepeat, mImgPrev, mImgPlay,
            mImgNext, mImgShuffle, mImgSongArt;
    private TextView mTvSongTitle, mTvArtist, mTvCurrentTime, mTvTotalTime;
    private SeekBar mSbDuration;
    private IMediaControl mMediaControl;
    private IFavoriteControl mFavoriteControl;
    private Bundle mBundle;
    private SharedPreferences mSharedPrf;

    private boolean mIsShuffle;
    private String mRepeat;
    private boolean mIsFavorite = false;
    private MediaPlaybackService mMediaPlaybackService;
    private Handler mHandler;
    private Runnable mRunnable;
    private ArrayList<Song> mArraySongs;
    private int mPosition;


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
        mImgMore = view.findViewById(R.id.img_more_top);
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

        if (mMediaPlaybackService != null) {
            updateTimeSong();
            mHandler.postDelayed(mRunnable, 0);
        }

        mSharedPrf = getActivity().getSharedPreferences(MediaPlaybackService.PRF_NAME, MODE_PRIVATE);
        mIsShuffle = mSharedPrf.getBoolean(MediaPlaybackService.PRF_SHUFFLE, false);
        mRepeat = mSharedPrf.getString(MediaPlaybackService.PRF_REPEAT, ActivityMusic.FALSE);

        if (mMediaPlaybackService != null) {
            checkPlaying(mMediaPlaybackService.isPlaying());
        }

        mBundle = getArguments();
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
            case ActivityMusic.FALSE:
                setImgRepeat(R.drawable.ic_play_repeat_default);
                break;
            case ActivityMusic.TRUE:
                setImgRepeat(R.drawable.ic_play_repeat_selected);
                break;
            case ActivityMusic.REPEAT:
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

    public void checkFavorite(boolean isFavorite) {
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
                mMediaPlaybackService.playerSeekTo(mSbDuration.getProgress());
            }
        });
    }

    @Override
    public void onClick(View view) {
        mArraySongs = mMediaPlaybackService.getArraySongs();
        mPosition = mMediaPlaybackService.getPosition();
        if (mPosition == -1) {
            mPosition = 0;
        }
        mMediaPlaybackService.setPosition(mPosition);

        switch (view.getId()) {

            case R.id.img_play:
                if (mMediaPlaybackService.isPlaying()) {
                    mMediaPlaybackService.pauseSong();
                } else {
                    if (mMediaPlaybackService.getCurrentTime() == 0) {
                        mMediaPlaybackService.playSong();
                    } else {
                        mMediaPlaybackService.resumeSong();
                    }
                }
                mSong = mArraySongs.get(mPosition);
                mMediaControl.onClickPlay(mSong.getmId(), mMediaPlaybackService.isPlaying());
                break;

            case R.id.img_next:
                mMediaPlaybackService.playNext();
                mMediaControl.onClickNext(mSong.getmId(), mMediaPlaybackService.isPlaying());
                break;

            case R.id.img_prev:
                mMediaPlaybackService.playPrev();
                mMediaControl.onClickPrev(mSong.getmId(), mMediaPlaybackService.isPlaying());
                break;

            case R.id.img_shuffle:
                if (mIsShuffle) {
                    mIsShuffle = false;
                } else {
                    mIsShuffle = true;
                }
                checkShuffle();

                mMediaPlaybackService.putShuffleToPrf(mIsShuffle);
                mMediaControl.onClickShuffle(mIsShuffle);
                break;

            case R.id.img_repeat:
                switch (mRepeat) {
                    case ActivityMusic.FALSE:
                        mRepeat = ActivityMusic.TRUE;
                        break;
                    case ActivityMusic.TRUE:
                        mRepeat = ActivityMusic.REPEAT;
                        break;
                    case ActivityMusic.REPEAT:
                        mRepeat = ActivityMusic.FALSE;
                        break;
                }
                checkRepeat();
                mMediaControl.onClickRepeat(mRepeat);
                mMediaPlaybackService.putRepeatToPrf(mRepeat);
                break;

            case R.id.img_favorite:
                FavoriteSongsDB favoriteSongsDB = new FavoriteSongsDB(getContext());
                if (mIsFavorite) {
                    mIsFavorite = false;
                    favoriteSongsDB.setFavorite(mSong.getmId(), 1);
                    Toast.makeText(mMediaPlaybackService, R.string.remove_favorite, Toast.LENGTH_SHORT).show();
                } else {
                    mIsFavorite = true;
                    favoriteSongsDB.addToFavoriteDB(mSong.getmId());
                    Toast.makeText(mMediaPlaybackService, R.string.add_to_favorite, Toast.LENGTH_SHORT).show();
                    favoriteSongsDB.setFavorite(mSong.getmId(), 2);
                }
                mFavoriteControl.onClickFavorite(mIsFavorite);
                mSong.setmIsFavorite(mIsFavorite);
                checkFavorite(mIsFavorite);
                break;
        }
    }

    private void updateTimeSong() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                int mCurrentTime = mMediaPlaybackService.getCurrentTime();
                int mDuration = mMediaPlaybackService.getDuration();
                setCurrentTime(format.format(mCurrentTime));
                setCurrentSeekBar(mCurrentTime, mDuration);
                mHandler.postDelayed(this, 1000);
            }
        };
    }

    public void setService(MediaPlaybackService service) {
        mMediaPlaybackService = service;
    }
}
