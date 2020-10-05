package com.example.music.Interface;


public interface IMediaControl {
    void onClick(int id, boolean isPlaying);
    void onClickShuffle(boolean isShuffle);
    void onClickRepeat(String repeat);
    void onClickList();
}
