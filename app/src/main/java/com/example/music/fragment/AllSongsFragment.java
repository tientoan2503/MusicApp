package com.example.music.fragment;

public class AllSongsFragment extends BaseSongListFragment {

    @Override
    public void updateAdapter() {
        mSongAdapter.getAllSongs(getContext());
    }
}
