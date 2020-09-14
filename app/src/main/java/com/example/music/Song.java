package com.example.music;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

//TrungTH sai convention
public class Song implements Parcelable {
    private String title, artist, duration, resource;
    private int order, albumID, id;

    public Song(int order, String title, String artist, int id, int albumID, String duration, String resource) {
        this.order = order;
        this.title = title;
        this.artist = artist;
        this.id = id;
        this.albumID = albumID;
        this.duration = duration;
        this.resource = resource;
    }

    public Song() {
    }

    protected Song(Parcel in) {
        title = in.readString();
        artist = in.readString();
        duration = in.readString();
        albumID = in.readInt();
        resource = in.readString();
        order = in.readInt();
        id = in.readInt();
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlbumID() {
        return albumID;
    }

    public void setAlbumID(int albumID) {
        this.albumID = albumID;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
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
        return ContentUris.withAppendedId(sArtworkUri, albumID);
    }

    public void setImage(Context context, ImageView imageView) {
        Glide.with(context)
                .load(getUri())
                .placeholder(R.drawable.art_default)
                .into(imageView);
    }
}
