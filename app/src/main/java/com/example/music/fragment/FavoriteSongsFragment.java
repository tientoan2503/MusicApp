package com.example.music.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.adapter.SongAdapter;

import java.util.ArrayList;

public class FavoriteSongsFragment extends BaseSongListFragment {
    private SongAdapter mAdapter;

    @Override
    public SongAdapter getAdapter() {
        mAdapter = new SongAdapter();
        return mAdapter;
    }

    public void getFavoriteList() {
        mAdapter.getFavoriteList(getContext());
    }

    public ArrayList<Song> getArraySongs() {
        return mAdapter.mArraySongs;
    }

}
