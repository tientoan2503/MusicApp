package com.example.music.favoritesongs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SongsHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "DB_NAME";
    private static final int DB_VERSION = 1;
    private static final String SONG_TITLE = "SONG_TITLE";
    private static final String SONG_ARTIST = "SONG_ARTIST";
    private static final String SONG_DURATION = "SONG_DURATION";
    private static final String SONG_RESOURCE = "SONG_RESOURCE";
    private static final String ALBUM_ID = "ALBUM_ID";
    private static final String SONG_ID = "SONG_ID";

    private static final String CREATE_DATABASE = "CREATE TABLE " + DB_NAME
            + " (" + SONG_TITLE + " TEXT NOT NULL, "
            + SONG_ARTIST + " TEXT NOT NULL, "
            + SONG_DURATION + " TEXT NOT NULL, "
            + SONG_RESOURCE + "TEXT NOT NULL, "
            + ALBUM_ID + " INTEGER NOT NULL, "
            + SONG_ID + " INTEGER NOT NULL)";

    public SongsHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
