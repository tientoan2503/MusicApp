package com.example.music.Interface;

import android.widget.ImageView;

public interface IMediaControl {
    void onClickPlay(int id, boolean isPlaying);
    void onClickNext(int id, boolean isPlaying);
    void onClickPrev(int id, boolean isPlaying);
    void onClickShuffle(boolean isShuffle);
    void onClickRepeat(String repeat);
}
