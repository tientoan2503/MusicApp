package com.example.music;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

//TrungTH sai convention
public class Song implements Parcelable {
    private String mTitle, mArtist, mDuration, mResource;
    private int mOrder, mAlbumID, mId;
    private ImageView mArt;

    public Song(int order, String title, String artist, int id, int albumID, String duration, String resource) {
        this.mOrder = order;
        this.mTitle = title;
        this.mArtist = artist;
        this.mId = id;
        this.mAlbumID = albumID;
        this.mDuration = duration;
        this.mResource = resource;
    }

    public Song() {
    }

    protected Song(Parcel in) {
        mTitle = in.readString();
        mArtist = in.readString();
        mDuration = in.readString();
        mAlbumID = in.readInt();
        mResource = in.readString();
        mOrder = in.readInt();
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

    public int getmOrder() {
        return mOrder;
    }

    public void setmOrder(int mOrder) {
        this.mOrder = mOrder;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public Uri getUri() {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(sArtworkUri, mAlbumID);
    }

    public void setImage(Context context, ImageView imageView) {
        Glide.with(context)
                .load(getUri())
                .placeholder(R.drawable.art_default)
                .into(imageView);
    }

    public ImageView getArt(Context context) {
        Glide.with(context)
                .load(getUri())
                .placeholder(R.drawable.art_default)
                .into(mArt);
        return mArt;
    }
}
