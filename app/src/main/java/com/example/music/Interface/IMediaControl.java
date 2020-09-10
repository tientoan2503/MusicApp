package com.example.music.Interface;

import android.widget.ImageView;

public interface IMediaControl {
    void onClickPlay(ImageView view);
    void onClickNext(ImageView view);
    void onClickPrev(ImageView view);
    void onClickShuffle(boolean isShuffle);
    void onClickRepeat(String repeat);
}
