package com.example.music.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.music.Song;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SongLoader extends AsyncTaskLoader<ArrayList<Song>> {
    private Context mContext;
    private ArrayList<Song> mArraySongs;
    private ArrayList<Song> mFavorites;

    public ArrayList<Song> getAllSongs() {
        return mArraySongs;
    }

    public ArrayList<Song> getFavorites() {
        return mFavorites;
    }

    public SongLoader(@NonNull Context context) {
        super(context);
        mContext = context;
        forceLoad();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public ArrayList<Song> loadInBackground() {
        getAllSongs(mContext);
        getFavoriteList(mContext);
        return null;
    }

    @Override
    public void deliverResult(@Nullable ArrayList<Song> data) {
        super.deliverResult(data);
        onFinishQuery();
    }

    public void onFinishQuery() {
    }

    public void getAllSongs(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                //format duration to mm:ss
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                String duration = simpleDateFormat.format(time);

                //add Song to songList
                Song song = new Song(title, artist, id, albumId, duration, resource);
                mArraySongs.add(song);
            }
            Log.d("ToanNTe", "getAllSongs: "+mArraySongs.size());
            cursor.close();
        }
    }

    public void getFavoriteList(Context context) {
        Cursor cursor = context.getContentResolver().query(SongProvider.CONTENT_URI, null,
                FavoriteSongsDB.IS_FAVORITE + "=2", null, null);
        mFavorites = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idOfFavoriteSong = cursor.getInt(cursor.getColumnIndex(FavoriteSongsDB.ID_PROVIDER));
                Cursor cursor1 = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, MediaStore.Audio.Media.IS_MUSIC + "=1"
                                + " AND " + MediaStore.Audio.Media._ID + "=" + idOfFavoriteSong,
                        null, MediaStore.Audio.Media.TITLE + " ASC");
                if (cursor1 != null) {
                    while (cursor1.moveToNext()) {
                        String title = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String resource = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.DATA));
                        int time = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        int albumId = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        int id = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media._ID));

                        //format duration to mm:ss
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                        String duration = simpleDateFormat.format(time);

                        //add Song to songList
                        Song song = new Song(title, artist, id, albumId, duration, resource);
                        mFavorites.add(song);
                    }
                    cursor1.close();
                }
            }
            cursor.close();
        }
    }
}
