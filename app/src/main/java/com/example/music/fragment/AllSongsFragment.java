package com.example.music.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.R;
import com.example.music.adapter.SongAdapter;

public class AllSongsFragment extends BaseSongListFragment {

    @Override
    public void updateAdapter() {
        mSongAdapter.getAllSongs(getContext());
    }
}
