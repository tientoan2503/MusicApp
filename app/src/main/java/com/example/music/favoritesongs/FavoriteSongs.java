package com.example.music.favoritesongs;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.example.music.Song;

public class FavoriteSongs extends SQLiteOpenHelper {

    private static final String DB_NAME = "DB_NAME";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "FAVORITE_SONGS";
    private static final String ID = "ID";
    private static final String ID_PROVIDER = "ID_PROVIDER";
    private static final String IS_FAVORITE = "IS_FAVORITE";
    private static final String COUNT_OF_PLAY = "COUNT_OF_PLAY";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ID_PROVIDER + " INTEGER, " + IS_FAVORITE + " INTEGER DEFAULT 0, "
            + COUNT_OF_PLAY + " INTEGER DEFAULT 0)";

    public FavoriteSongs(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insert() {
        SQLiteDatabase  database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        database.insert(TABLE_NAME,null, contentValues);
    }
}
