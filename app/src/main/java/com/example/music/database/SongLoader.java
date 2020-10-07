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

    public SongLoader(@NonNull Context context) {
        super(context);
        mContext  = context;
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
        Log.d("ToanNTe", "loadInBackground: " + mArraySongs.size());
        return mArraySongs;
    }

    public void getAllSongs(Context context) {
//        // TODO TrungTH dùng asyncTask đã được dậy chứ
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
            cursor.close();
        }
    }
}
