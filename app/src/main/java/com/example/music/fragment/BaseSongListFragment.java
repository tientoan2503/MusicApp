package com.example.music.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Interface.IFavoriteControl;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.adapter.SongAdapter;
import com.example.music.database.SongLoader;
import com.example.music.service.MediaPlaybackService;

import java.util.ArrayList;

public abstract class BaseSongListFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    protected static final int LOADER_ID = 1;
    public RecyclerView mRecyclerview;
    protected SongAdapter mSongAdapter;
    protected PopupMenu mPopup;
    protected View mView;
    protected int mPosition;
    private MediaPlaybackService mService;
    protected IFavoriteControl mFavoriteControl;
    protected boolean mIsFavorite;
    protected SongLoader mSongLoader;

    public BaseSongListFragment() {}

    public BaseSongListFragment(IFavoriteControl favoriteControl) {
        mFavoriteControl = favoriteControl;
    }

    public abstract void updateAdapter();

    public abstract void updatePopupMenu(View view);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongAdapter = new SongAdapter(this);
        mSongLoader = new SongLoader(getContext());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_action_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSongAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSongAdapter.getFilter().filter(newText);
                return false;
            }
        });

        if (mService != null) {
            int id;
            ArrayList<Song> arraySong = mService.getArraySongs();
            int position = mService.getPosition();
            Song song = arraySong.get(position);
            id = song.getmId();
            int i = 0;
            do {
                song = arraySong.get(i);
                i++;
            } while (song.getmId() != id);
            setAnimation(position, id, mService.isPlaying());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_all_songs, container, false);
        updateAdapter();
        mRecyclerview = mView.findViewById(R.id.recyclerview);
        initRecyclerView();
        return mView;
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
        if (position == 0) {
            mRecyclerview.smoothScrollToPosition(position);
        } else {
            mRecyclerview.smoothScrollToPosition(position + 1);
        }
        mSongAdapter.notifyDataSetChanged();
    }

    public SongAdapter getAdapter() {
        return mSongAdapter;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setService(MediaPlaybackService service) {
        mService = service;
    }
}
