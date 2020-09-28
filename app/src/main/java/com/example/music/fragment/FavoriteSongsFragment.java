package com.example.music.fragment;


public class FavoriteSongsFragment extends BaseSongListFragment {

    @Override
    public void updateAdapter() {
        mSongAdapter.getFavoriteList(getContext());
    }
}
