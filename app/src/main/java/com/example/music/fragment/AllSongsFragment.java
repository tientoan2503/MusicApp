package com.example.music.fragment;

public class AllSongsFragment extends BaseSongListFragment {

    @Override
    public void updateAdapter() {
        mArrSong = mSongAdapter.getAllSongs(getContext());
    }
}
