package com.bkav.music.Interface;


public interface IMediaControl {
    void onClick(int id, boolean isPlaying);
    void onClickShuffle(boolean isShuffle);
    void onClickRepeat(String repeat);
}
