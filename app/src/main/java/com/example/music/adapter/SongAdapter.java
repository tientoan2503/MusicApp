package com.example.music.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Interface.IClickItem;
import com.example.music.R;
import com.example.music.Song;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public ArrayList<Song> mArraySongs;
    private IClickItem IClickItem;
    public int mPosition;
    private int mPositionClicked = -1;

    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.song_row_default, parent, false);

        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
//        Log.d("ToanNTe", "onBindViewHolder: " + Log.getStackTraceString(new Exception()));
//        Log.d("ToanNTe", "onBindViewHolder: " + mPositionClicked);
        Song song = mArraySongs.get(position);
        holder.mSongOrder.setText(song.getOrder() + "");
        holder.mTvSongName.setText(song.getTitle());
        holder.mTvDuration.setText(song.getDuration());
        if (position == mPositionClicked) {
            holder.mTvSongName.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return mArraySongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mActionMore;
        TextView mTvSongName, mTvDuration, mSongOrder;

        WeakReference<SongAdapter> mAdapter;

        public ViewHolder(@NonNull final View itemView, SongAdapter adapter) {
            super(itemView);
            mAdapter = new WeakReference<>(adapter);

            mSongOrder = itemView.findViewById(R.id.song_order);
            mTvSongName = itemView.findViewById(R.id.song_title);
            mTvDuration = itemView.findViewById(R.id.duration);
            mActionMore = itemView.findViewById(R.id.action_more);
//            mEqualizer = itemView.findViewById(R.id.equalizer_view);
            //initialize event click item in recyclerview
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapter.get().mPosition = getAdapterPosition();
                    IClickItem = (IClickItem) view.getContext();
                    IClickItem.onClickItem(mPosition);
                }
            });

        }
    }

    //method read song from storage
    public void getAllSongs(Context context) {
        // TODO TrungTH dùng asyncTask đã được dậy chứ
        int order = 0;
        mArraySongs = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.IS_MUSIC + "=1", null, MediaStore.Audio.Media.TITLE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                order++;
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String resource = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                int time = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                //format duration to mm:ss
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                String duration = simpleDateFormat.format(time);

                //add Song to songList
                Song song = new Song(order, title, artist, albumId, duration, resource);
                mArraySongs.add(song);
            }
            cursor.close();
        }
    }

    public ArrayList<Song> getArraySongs() {
        return mArraySongs;
    }

    public void setPositionClicked(int position) {
        mPositionClicked = position;
        notifyDataSetChanged();
    }

}