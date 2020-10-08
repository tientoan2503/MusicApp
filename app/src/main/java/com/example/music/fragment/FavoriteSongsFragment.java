package com.example.music.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.music.Interface.IFavoriteControl;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.database.FavoriteSongsDB;
import com.example.music.database.SongLoader;
import com.example.music.database.SongProvider;

import java.text.SimpleDateFormat;

public class FavoriteSongsFragment extends BaseSongListFragment {

    public FavoriteSongsFragment() {
    }

    public FavoriteSongsFragment(IFavoriteControl favoriteControl) {
        super(favoriteControl);
    }

    @Override
    public void updateAdapter() {
        mSongAdapter.setArray(mArraySongs);
        mSongAdapter.notifyDataSetChanged();
    }

    public void setArraySongs(){
        new SongLoader(getContext()){
            @Override
            public void onFinishQuery() {
                super.onFinishQuery();
                mArraySongs = getFavorites();
                updateAdapter();
            }
        };
    }

    @Override
    public void updatePopupMenu(View view) {
        mPopup = new PopupMenu(getContext(), view.findViewById(R.id.action_more));
        mPopup.getMenuInflater().inflate(R.menu.menu_popup_media_playback, mPopup.getMenu());
        mPopup.setOnMenuItemClickListener(this);
        mPopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        FavoriteSongsDB favoriteSongsDB = new FavoriteSongsDB(getContext());
        Song song = (Song) mSongAdapter.getArr().get(mPosition);
        int id = song.getmId();
        if (mPosition < mSongAdapter.getArr().size()) {
            favoriteSongsDB.setFavorite(id, 1);
            favoriteSongsDB.updateCount(id, 0);
            Toast.makeText(getContext(), R.string.remove_favorite, Toast.LENGTH_SHORT).show();
            updateAdapter();
            mIsFavorite = false;
        }
        song.setmIsFavorite(mIsFavorite);
        mFavoriteControl.updateUI(id, mIsFavorite);
        updateAdapter();
        return true;
    }

//
//    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        CursorLoader cursorLoader;
//        cursorLoader = new CursorLoader(getContext(), SongProvider.CONTENT_URI, null,
//                FavoriteSongsDB.IS_FAVORITE + "=2", null, null);
//        return cursorLoader;
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader loader) {
//
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
//        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null, MediaStore.Audio.Media.IS_MUSIC + "=1",
//                null, MediaStore.Audio.Media.TITLE + " ASC");
//        if (data != null) {
//            while (data.moveToNext()) {
//                int idOfFavoriteSong = data.getInt(data.getColumnIndex(FavoriteSongsDB.ID_PROVIDER));
//                Cursor cursor1 = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                        null, MediaStore.Audio.Media.IS_MUSIC + "=1"
//                                + " AND " + MediaStore.Audio.Media._ID + "=" + idOfFavoriteSong,
//                        null, MediaStore.Audio.Media.TITLE + " ASC");
//                if (cursor1 != null) {
//                    while (cursor1.moveToNext()) {
//                        String title = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                        String artist = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                        String resource = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.DATA));
//                        int time = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media.DURATION));
//                        int albumId = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                        int id = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media._ID));
//
//                        //format duration to mm:ss
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
//                        String duration = simpleDateFormat.format(time);
//
//                        //add Song to songList
//                        Song song = new Song(title, artist, id, albumId, duration, resource);
//                        mArraySongs.add(song);
//                        Log.d("ToanNTe", "onLoadFinished: " + mArraySongs.size());
//                    }
//                    cursor1.close();
//                }
//            }
//            cursor.close();
//        }
//
//    }
}
