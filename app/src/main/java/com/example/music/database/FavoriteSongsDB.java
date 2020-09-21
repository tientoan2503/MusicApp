package com.example.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoriteSongsDB {

    public static final String DB_NAME = "FavoriteSongs";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "FavoriteSongs";
    public static final String ID = "ID";
    public static final String ID_PROVIDER = "ID_PROVIDER";
    public static final String IS_FAVORITE = "IS_FAVORITE";
    public static final String COUNT_OF_PLAY = "COUNT_OF_PLAY";
    private ContentValues mContentValues;

    private Context mContext;

    public FavoriteSongsDB(Context context) {
        mContext = context;
    }

    public static class SongHelper extends SQLiteOpenHelper {

        public SongHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            String sql = "CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ID_PROVIDER + " INTEGER, " + IS_FAVORITE + " INTEGER DEFAULT 0, "
                    + COUNT_OF_PLAY + " INTEGER DEFAULT 0)";
            sqLiteDatabase.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

    }

    public void insertDB(int id, int count) {
        mContentValues = new ContentValues();
        mContentValues.put(ID_PROVIDER, id);
        mContentValues.put(COUNT_OF_PLAY, count);
        mContext.getContentResolver().insert(SongProvider.CONTENT_URI, mContentValues);
    }

    public void updateDatabase(int id, int count) {
        mContentValues = new ContentValues();
        mContentValues.put(FavoriteSongsDB.COUNT_OF_PLAY, count);
        mContext.getContentResolver().update(SongProvider.CONTENT_URI, mContentValues,
                FavoriteSongsDB.ID_PROVIDER + " = " + id, null);
    }
}
