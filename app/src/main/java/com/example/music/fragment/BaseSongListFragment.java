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
import com.example.music.adapter.SongAdapter;

public abstract class BaseSongListFragment extends Fragment {

    private RecyclerView mRecyclerview;
    private SongAdapter mAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        Log.d("ToanNTe", "onCreateView: ");
        mAdapter = new SongAdapter();
        mRecyclerview = view.findViewById(R.id.recyclerview);

        setAdapter(getAdapter());

        return view;
    }

    public void initRecyclerView() {
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerview.setAdapter(mAdapter);
        mRecyclerview.setLayoutManager(linearLayout);
    }

    public void setAdapter(SongAdapter adapter) {
        mAdapter = adapter;
    }

    public abstract SongAdapter getAdapter();

    public void setAnimation(int position, int id, boolean isPlaying) {
        mAdapter.mSongId = id;
        mAdapter.mIsPlaying = isPlaying;
        mRecyclerview.smoothScrollToPosition(position);
        mAdapter.notifyDataSetChanged();
    }

}
