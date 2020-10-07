package com.example.music.database;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.loader.content.AsyncTaskLoader;

import com.example.music.Song;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongLoader {

    private ArrayList<Song> mArraySongs, mFavoriteList;
    private Song mSong;
    private int mId;
    private Context mContext;

    public SongLoader(Context context) {
        mContext = context;
    }

    public void getSong() {
        AsyncTaskLoader asyncTaskLoader = new AsyncTaskLoader(mContext) {

            @Nullable
            @Override
            public Object loadInBackground() {
                Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, MediaStore.Audio.Media.IS_MUSIC + "=1",
                        null, MediaStore.Audio.Media.TITLE + " ASC");
                mArraySongs = new ArrayList<>();
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String resource = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        int time = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        mId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                        //format duration to mm:ss
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                        String duration = simpleDateFormat.format(time);

                        //add Song to songList
                        mSong = new Song(title, artist, mId, albumId, duration, resource);
                        mArraySongs.add(mSong);
                    }

                    Cursor cursor1 = getContext().getContentResolver().query(SongProvider.CONTENT_URI, null,
                            FavoriteSongsDB.IS_FAVORITE + "=2", null, null);

                    if (cursor1 != null) {
                        while (cursor1.moveToNext()) {
                            int idOfFavoriteSong = cursor1.getInt(cursor.getColumnIndex(FavoriteSongsDB.ID_PROVIDER));
                            if (idOfFavoriteSong == mId) {
                                mFavoriteList.add(mSong);
                            }
                        }
                        cursor1.close();
                    }
                    cursor.close();
                }
                return null;
            }

            @Override
            public void deliverResult(@Nullable Object data) {
                super.deliverResult(data);
                onFinishRetrieve();
            }
        };
        asyncTaskLoader.forceLoad();
    }

    public void onFinishRetrieve(){

    }

//
//    public void getSong(Context context) {
//        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null, MediaStore.Audio.Media.IS_MUSIC + "=1",
//                null, MediaStore.Audio.Media.TITLE + " ASC");
//        mArraySongs = new ArrayList<>();
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                String resource = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//                int time = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
//                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                mId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
//
//                //format duration to mm:ss
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
//                String duration = simpleDateFormat.format(time);
//
//                //add Song to songList
//                mSong = new Song(title, artist, mId, albumId, duration, resource);
//                mArraySongs.add(mSong);
//            }
//
//            Cursor cursor1 = context.getContentResolver().query(SongProvider.CONTENT_URI, null,
//                    FavoriteSongsDB.IS_FAVORITE + "=2", null, null);
//
//            if (cursor1 != null) {
//                while (cursor1.moveToNext()) {
//                    int idOfFavoriteSong = cursor.getInt(cursor.getColumnIndex(FavoriteSongsDB.ID_PROVIDER));
//                    if (mId == idOfFavoriteSong) {
//                        mFavoriteList.add(mSong);
//                    }
//                }
//                cursor1.close();
//            }
//            cursor.close();
//        }
//    }

    public ArrayList<Song> getFavoriteList() {
        return mFavoriteList;
    }

    public ArrayList<Song> getSongList() {
        return mArraySongs;
    }
}
