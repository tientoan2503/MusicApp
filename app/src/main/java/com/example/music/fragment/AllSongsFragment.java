package com.example.music.fragment;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.music.Song;
import com.example.music.adapter.SongAdapter;

import java.util.ArrayList;

public class AllSongsFragment extends BaseSongListFragment {

    private SongAdapter mAdapter;

    @Override
    public SongAdapter getAdapter() {
        mAdapter = new SongAdapter();
        return mAdapter;
    }

    public void getAllSongs() {
        mAdapter.getAllSongs(getContext());
    }

    public ArrayList<Song> getArraySongs() {
        return mAdapter.mArraySongs;
    }

}
