package com.example.music.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SongProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.music.databases";
    public static final String PATH = "favoritesongs";
    public static final String URL = "content://" + AUTHORITY + "/" + PATH;
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final int SONGS = 1;
    public static final int SONG_ID = 2;

    static final String SINGLE_SONG_MIME_TYPE =
            "vnd.android.cursor.item/vnd.com.example.music.databases.favoritesongs";
    static final String MULTIPLE_SONGS_MIME_TYPE =
            "vnd.android.cursor.dir/vnd.com.example.music.databases.favoritesongs";

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH, SONGS);
        sUriMatcher.addURI(AUTHORITY, PATH + "/#", SONG_ID);
    }

    private SQLiteDatabase mDatabase;

    @Override
    public boolean onCreate() {
        FavoriteSongsDB.SongHelper songHelper = new FavoriteSongsDB.SongHelper(getContext());
        mDatabase = songHelper.getWritableDatabase();
        return mDatabase == null ? false : true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(FavoriteSongsDB.TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case SONGS:
                break;
            case SONG_ID:
                sqLiteQueryBuilder.appendWhere(FavoriteSongsDB.ID_PROVIDER + "=" + uri.getPathSegments().get(1));
                break;
            default:
                try {
                    throw new IllegalAccessException("Unknown URI: " + uri);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
        Cursor cursor = sqLiteQueryBuilder.query(mDatabase, projection, selection, selectionArgs,null, null, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)){
            case SONGS:
                return MULTIPLE_SONGS_MIME_TYPE;
            case SONG_ID:
                return SINGLE_SONG_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long rowId = mDatabase.insert(FavoriteSongsDB.TABLE_NAME, null, contentValues);

        if (rowId > 0) {
            Uri newSongUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(newSongUri, null);
            return newSongUri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (sUriMatcher.match(uri)){
            case SONGS:
                count = mDatabase.delete(FavoriteSongsDB.TABLE_NAME, selection, selectionArgs);
                break;
            case SONG_ID:
                String id = uri.getPathSegments().get(1);
                count = mDatabase.delete(FavoriteSongsDB.TABLE_NAME, FavoriteSongsDB.ID_PROVIDER +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (sUriMatcher.match(uri)){
            case SONGS:
                count = mDatabase.update(FavoriteSongsDB.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case SONG_ID:
                String id = uri.getPathSegments().get(1);
                count = mDatabase.update(FavoriteSongsDB.TABLE_NAME, contentValues, FavoriteSongsDB.ID_PROVIDER + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
