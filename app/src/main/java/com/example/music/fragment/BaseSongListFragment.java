package com.example.music.fragment;

import android.os.Bundle;
import android.util.Log;
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

public abstract class BaseSongListFragment extends Fragment {

    private RecyclerView mRecyclerview;
    protected SongAdapter mSongAdapter;

    public abstract void updateAdapter();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        mSongAdapter = new SongAdapter();
        updateAdapter();
        mRecyclerview = view.findViewById(R.id.recyclerview);
        initRecyclerView();
        return view;
    }

    public void initRecyclerView() {
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerview.setAdapter(mSongAdapter);
        mRecyclerview.setLayoutManager(linearLayout);
    }

    public ArrayList<Song> getArraySongs() {
        return mSongAdapter.mArraySongs;
    }

    public void setAnimation(int position, int id, boolean isPlaying) {
        mSongAdapter.mSongId = id;
        mSongAdapter.mIsPlaying = isPlaying;
        mRecyclerview.smoothScrollToPosition(position);
        mSongAdapter.notifyDataSetChanged();
    }
}
