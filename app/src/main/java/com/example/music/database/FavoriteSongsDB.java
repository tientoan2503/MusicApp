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

    public void updateCount(int id, int count) {
        mContentValues = new ContentValues();
        mContentValues.put(FavoriteSongsDB.COUNT_OF_PLAY, count);
        mContext.getContentResolver().update(SongProvider.CONTENT_URI, mContentValues,
                FavoriteSongsDB.ID_PROVIDER + "=" + id, null);
    }

    public void setFavorite(int id, int favorite) {
        Cursor cursor = mContext.getContentResolver().query(SongProvider.CONTENT_URI, new String[]{FavoriteSongsDB.COUNT_OF_PLAY,
                FavoriteSongsDB.ID_PROVIDER}, FavoriteSongsDB.ID_PROVIDER + " = " + id, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mContentValues = new ContentValues();
                mContentValues.put(FavoriteSongsDB.IS_FAVORITE, favorite);
                mContext.getContentResolver().update(SongProvider.CONTENT_URI, mContentValues,
                        FavoriteSongsDB.ID_PROVIDER + "=" + id, null);
            } else {
                insertDB(id, 0);
                setFavorite(id, 2);
            }
        }
    }

    public void addToFavoriteDB(int id) {
        Cursor cursor = mContext.getContentResolver().query(SongProvider.CONTENT_URI, new String[]{FavoriteSongsDB.COUNT_OF_PLAY,
                FavoriteSongsDB.ID_PROVIDER}, FavoriteSongsDB.ID_PROVIDER + " = " + id, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                int count = cursor.getInt(cursor.getColumnIndex(FavoriteSongsDB.COUNT_OF_PLAY));
                updateCount(id, ++count);
                if (count >= 3) {
                    setFavorite(id, 2);
                }
            } else {
                insertDB(id, 1);
            }
        }
    }
}
