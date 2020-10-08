package com.example.music.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Interface.IClickItem;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.database.FavoriteSongsDB;
import com.example.music.database.SongProvider;
import com.example.music.fragment.BaseSongListFragment;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> implements Filterable {

    private com.example.music.Interface.IClickItem IClickItem;
    public int mPosition;
    public int mSongId;
    public boolean mIsPlaying;
    private BaseSongListFragment mBaseFragment;
    public ArrayList<Song> mArraySongs;
    private ArrayList<Song> mListFiltered;

    public SongAdapter() {
    }

    public SongAdapter(BaseSongListFragment fragment) {
        mBaseFragment = fragment;
    }

    ;

    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.song_row, parent, false);
        return new ViewHolder(view, this);
    }


    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        Song song = mArraySongs.get(position);
        holder.mSongOrder.setText(position + 1 + "");
        holder.mTvSongName.setText(song.getmTitle());
        holder.mTvDuration.setText(song.getmDuration());

        if (song.getmId() == mSongId) {
            holder.mTvSongName.setTypeface(Typeface.DEFAULT_BOLD);
            holder.mSongOrder.setVisibility(View.INVISIBLE);
            holder.mEqualizer.setVisibility(View.VISIBLE);
            if (mIsPlaying) {
                holder.mEqualizer.animateBars();
            } else {
                holder.mEqualizer.stopBars();
            }

        } else {
            holder.mTvSongName.setTypeface(Typeface.DEFAULT);
            holder.mEqualizer.stopBars();
            holder.mEqualizer.setVisibility(View.GONE);
            holder.mSongOrder.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public int getItemCount() {
        return mArraySongs.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String songTitle = charSequence.toString();
                ArrayList<Song> filteredList = new ArrayList<>();

                if (songTitle.isEmpty()) {
                    filteredList = mListFiltered;
                } else {
                    for (Song song : mListFiltered) {
                        if (song.getmTitle().toLowerCase().contains(songTitle.toLowerCase())) {
                            filteredList.add(song);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mArraySongs = (ArrayList<Song>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mActionMore;
        TextView mTvSongName, mTvDuration, mSongOrder;
        EqualizerView mEqualizer;

        WeakReference<SongAdapter> mAdapter;

        public ViewHolder(@NonNull final View itemView, SongAdapter adapter) {
            super(itemView);
            mAdapter = new WeakReference<>(adapter);

            mSongOrder = itemView.findViewById(R.id.song_order);
            mTvSongName = itemView.findViewById(R.id.song_title);
            mTvDuration = itemView.findViewById(R.id.duration);
            mActionMore = itemView.findViewById(R.id.action_more);
            mEqualizer = itemView.findViewById(R.id.equalizer);

            //initialize event click item in recyclerview
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapter.get().mPosition = getAdapterPosition();
                    IClickItem = (IClickItem) view.getContext();
                    IClickItem.onClickItem(mPosition);
                }
            });

            mActionMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBaseFragment.updatePopupMenu(v);
                    mBaseFragment.setPosition(getAdapterPosition());
                }
            });
        }
    }

    //method read song from storage
    public void getAllSongs(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.IS_MUSIC + "=1",
                null, MediaStore.Audio.Media.TITLE + " ASC");
        mArraySongs = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String resource = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                int time = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));

                //format duration to mm:ss
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                String duration = simpleDateFormat.format(time);

                //add Song to songList
                Song song = new Song(title, artist, id, albumId, duration, resource);
                mArraySongs.add(song);
            }
            mListFiltered = new ArrayList<>();
            mListFiltered.addAll(mArraySongs);
            cursor.close();
            notifyDataSetChanged();
        }
    }

    public ArrayList getArr() {
        return mArraySongs;
    }

    public void getFavoriteList(Context context) {
        mListFiltered = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(SongProvider.CONTENT_URI, null,
                FavoriteSongsDB.IS_FAVORITE + "=2", null, null);
        mArraySongs = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idOfFavoriteSong = cursor.getInt(cursor.getColumnIndex(FavoriteSongsDB.ID_PROVIDER));
                Cursor cursor1 = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, MediaStore.Audio.Media.IS_MUSIC + "=1"
                                + " AND " + MediaStore.Audio.Media._ID + "=" + idOfFavoriteSong,
                        null, MediaStore.Audio.Media.TITLE + " ASC");
                if (cursor1 != null) {
                    while (cursor1.moveToNext()) {
                        String title = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String resource = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.DATA));
                        int time = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        int albumId = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        int id = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Audio.Media._ID));

                        //format duration to mm:ss
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                        String duration = simpleDateFormat.format(time);

                        //add Song to songList
                        Song song = new Song(title, artist, id, albumId, duration, resource);
                        mArraySongs.add(song);
                        mListFiltered.add(song);
                    }
                    cursor1.close();
                    notifyDataSetChanged();
                }
            }
            cursor.close();
        }
    }

    public void setData(ArrayList<Song> arrayList) {

    }

//    public class GetAllSongs extends AsyncTask<Context, Void, ArrayList<Song>> {
//
//        @Override
//        protected ArrayList<Song> doInBackground(Context... contexts) {
//            mArraySongs = new ArrayList<>();
//            Cursor cursor = contexts[0].getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                    null, MediaStore.Audio.Media.IS_MUSIC + "=1", null, MediaStore.Audio.Media.TITLE + " ASC");
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                    String resource = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//                    int time = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
//                    int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                    int songId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
//
//                    //format duration to mm:ss
//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
//                    String duration = simpleDateFormat.format(time);
//
//                    //add Song to songList
//                    Song song = new Song(title, artist, songId, albumId, duration, resource);
//                    mArraySongs.add(song);
//                }
//                cursor.close();
//            }
//            return mArraySongs;
//        }
//    }
}


