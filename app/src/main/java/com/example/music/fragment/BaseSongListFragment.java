package com.example.music.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.music.service.MediaPlaybackService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public abstract class BaseSongListFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, LoaderManager.LoaderCallbacks<ArrayList<Song>> {

    public RecyclerView mRecyclerview;
    protected SongAdapter mSongAdapter;
    protected PopupMenu mPopup;
    protected View mView;
    private ArrayList<Song> mArraySongs;
    protected int mPosition;
    private MediaPlaybackService mService;
    protected IFavoriteControl mFavoriteControl;
    protected boolean mIsFavorite;

    public BaseSongListFragment() {
    }

    public BaseSongListFragment(IFavoriteControl favoriteControl) {
        mFavoriteControl = favoriteControl;
    }

    @NonNull
    @Override
    public Loader<ArrayList<Song>> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == 0) {
            Log.d("ToanNte", "onCreateLoader: ");
            mSongAdapter.getAllSongs(getContext());
        } else {
            mSongAdapter.getFavoriteList(getContext());
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<Song>> loader, ArrayList<Song> data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<Song>> loader) {

    }

    //    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        CursorLoader cursorLoader;
//        if (id == 0) {
//            cursorLoader = new CursorLoader(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                    null, MediaStore.Audio.Media.IS_MUSIC + "=1",
//                    null, MediaStore.Audio.Media.TITLE + " ASC");
//        } else {
//            cursorLoader = new CursorLoader(getContext(), SongProvider.CONTENT_URI, null,
//                    FavoriteSongsDB.IS_FAVORITE + "=2", null, null);
//        }
//        Log.d("ToanNTe", "onCreateLoader: ");
//        return cursorLoader;
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
//        int id;
//        Song song;
//        if (data != null && data.getCount() > 0) {
//            data.moveToFirst();
//            if (loader.getId() == 0) {
//                String title = data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                String artist = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                String resource = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA));
//                int time = data.getInt(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
//                int albumId = data.getInt(data.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                id = data.getInt(data.getColumnIndex(MediaStore.Audio.Media._ID));
//
//                //format duration to mm:ss
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
//                String duration = simpleDateFormat.format(time);
//
//                //add Song to songList
//                song = new Song(title, artist, id, albumId, duration, resource);
//                mArraySongs.add(song);
//            } else {
//
//            }
//        }
//        mSongAdapter.setArray(mArraySongs);
//
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

//    }

    public abstract void updateAdapter();

    public abstract void updatePopupMenu(View view);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSongAdapter = new SongAdapter(this, getContext());
        updateAdapter();

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
