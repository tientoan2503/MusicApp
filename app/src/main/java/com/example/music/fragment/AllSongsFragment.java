package com.example.music.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.music.Interface.IFavoriteControl;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.database.FavoriteSongsDB;
import com.example.music.database.SongLoader;
import com.example.music.service.MediaPlaybackService;


public class AllSongsFragment extends BaseSongListFragment {

    public AllSongsFragment() {
    }

    public AllSongsFragment(IFavoriteControl favoriteControl) {
        super(favoriteControl);
    }

    @Override
    public void updateAdapter() {
        mSongAdapter.setArray(mArraySongs);
        mSongAdapter.notifyDataSetChanged();
    }

//    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        CursorLoader cursorLoader;
//        cursorLoader = new CursorLoader(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null, MediaStore.Audio.Media.IS_MUSIC + "=1",
//                null, MediaStore.Audio.Media.TITLE + " ASC");
//        return cursorLoader;
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader loader) {
//
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
//        if (data != null) {
//            while (data.moveToNext()) {
//                synchronized (this){
//                    try {
//                        wait(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                String title = data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE));
//                String artist = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                String resource = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA));
//                int time = data.getInt(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
//                int albumId = data.getInt(data.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                int id = data.getInt(data.getColumnIndex(MediaStore.Audio.Media._ID));
//
//                //format duration to mm:ss
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
//                String duration = simpleDateFormat.format(time);
//
//                //add Song to songList
//                Song song = new Song(title, artist, id, albumId, duration, resource);
//                mArraySongs.add(song);
//                Log.d("ToanNTe", "onLoadFinished: " + mArraySongs.size());
//            }
//        }
//    }

    public void setArraySongs(){
        new SongLoader(getContext()){
            @Override
            public void onFinishQuery() {
                super.onFinishQuery();
                mArraySongs = getAllSongs();
                updateAdapter();
            }
        };
    }

    @Override
    public void updatePopupMenu(View view) {
        mPopup = new PopupMenu(getContext(), view.findViewById(R.id.action_more));
        mPopup.getMenuInflater().inflate(R.menu.menu_popup_all_songs, mPopup.getMenu());

        // Set a listener so we are notified if a menu item is clicked
        mPopup.setOnMenuItemClickListener(this);

        mPopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        FavoriteSongsDB favoriteSongsDB = new FavoriteSongsDB(getContext());
        Song song = (Song) mSongAdapter.getArr().get(mPosition);
        int id = song.getmId();
        switch (item.getItemId()) {
            case R.id.popup_remove:
                favoriteSongsDB.setFavorite(id, 1);
                favoriteSongsDB.updateCount(id, 0);
                mIsFavorite = false;
                Toast.makeText(getContext(), R.string.remove_favorite, Toast.LENGTH_SHORT).show();
                break;
            case R.id.popup_add:
                favoriteSongsDB.setFavorite(id, 2);
                mIsFavorite = true;
                Toast.makeText(getContext(), R.string.add_to_favorite, Toast.LENGTH_SHORT).show();
                break;
        }
        song.setmIsFavorite(mIsFavorite);
        mFavoriteControl.updateUI(id, mIsFavorite);
        setArraySongs();
        return true;
    }
}
