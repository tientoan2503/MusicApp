package com.example.music;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private String mTitle, mArtist, mDuration, mResource;
    private int mAlbumID, mId;
    private boolean mIsFavorite = false;

    public Song(String title, String artist, int id, int albumID, String duration, String resource) {
        this.mTitle = title;
        this.mArtist = artist;
        this.mId = id;
        this.mAlbumID = albumID;
        this.mDuration = duration;
        this.mResource = resource;
    }

    protected Song(Parcel in) {
        mTitle = in.readString();
        mArtist = in.readString();
        mDuration = in.readString();
        mAlbumID = in.readInt();
        mResource = in.readString();
        mId = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public int getmAlbumID() {
        return mAlbumID;
    }

    public void setmAlbumID(int mAlbumID) {
        this.mAlbumID = mAlbumID;
    }

    public String getmResource() {
        return mResource;
    }

    public void setmResource(String mResource) {
        this.mResource = mResource;
    }

    public boolean ismIsFavorite() {
        return mIsFavorite;
    }

    public void setmIsFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public Bitmap getAlbumArt(Context context, String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] data = mediaMetadataRetriever.getEmbeddedPicture();
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.art_default);
    }
}
