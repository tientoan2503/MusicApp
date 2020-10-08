package com.example.music.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.ActivityMusic;
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

    public void setArraySongs() {
        new SongLoader(getContext()) {
            @Override
            public void onFinishQuery() {
                super.onFinishQuery();
                mArraySongs = getAllSongs();
                updateAdapter();

                int id = mSharePrf.getInt(MediaPlaybackService.PRF_ID, -1);
                if (id != -1) {
                    int i = -1;
                    do {
                        i++;
                        mSong = mArraySongs.get(i);
                    } while (mSong.getmId() != id);
                    mPosition = i;
                }

                TextView mTvTitle = getActivity().findViewById(R.id.sub_title);
                TextView mTvArtist = getActivity().findViewById(R.id.sub_artist);
                ImageView mImgArt = getActivity().findViewById(R.id.sub_art);

                if (mSong != null) {
                    mTvTitle.setText(mSong.getmTitle());
                    mTvArtist.setText(mSong.getmArtist());
                    mImgArt.setImageBitmap(mSong.getAlbumArt(getContext(), mSong.getmResource()));
                }
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
        setArraySongs();
        song.setmIsFavorite(mIsFavorite);
        mFavoriteControl.updateUI(id, mIsFavorite);
        updateAdapter();
        return true;
    }
}
