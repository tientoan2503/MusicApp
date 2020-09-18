package com.example.music.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SongHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "FavoriteSongs";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "FavoriteSongs";
    public static final String ID = "ID";
    public static final String ID_PROVIDER = "ID_PROVIDER";
    public static final String IS_FAVORITE = "IS_FAVORITE";
    public static final String COUNT_OF_PLAY = "COUNT_OF_PLAY";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ID_PROVIDER + " INTEGER, " + IS_FAVORITE + " INTEGER DEFAULT 0, "
            + COUNT_OF_PLAY + " INTEGER DEFAULT 0)";

    public SongHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void querySql(String sql) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public Cursor getData(String sql) {
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql, null);
    }

}
