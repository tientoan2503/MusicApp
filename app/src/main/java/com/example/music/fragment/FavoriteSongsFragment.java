package com.example.music.fragment;

import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.music.Interface.IFavoriteControl;
import com.example.music.R;
import com.example.music.Song;

public class FavoriteSongsFragment extends BaseSongListFragment {

    public FavoriteSongsFragment() {
    }

    public FavoriteSongsFragment(IFavoriteControl favoriteControl) {
        super(favoriteControl);
    }

    @Override
    public void updateAdapter() {
        mSongAdapter.getFavoriteList(getContext());
        mSongAdapter.notifyDataSetChanged();
    }


    @Override
    public void updatePopupMenu(View view) {
        mPopup = new PopupMenu(getContext(), view.findViewById(R.id.action_more));
        mPopup.getMenuInflater().inflate(R.menu.menu_popup_media_playback, mPopup.getMenu());
        mPopup.setOnMenuItemClickListener(this);
        mPopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Song song = (Song) mSongAdapter.getArr().get(mPosition);
        int id = song.getmId();
        if (mPosition < mSongAdapter.getArr().size()) {
            mFavoriteSongsDB.setFavorite(id, 1);
            mFavoriteSongsDB.updateCount(id, 0);
            Toast.makeText(getContext(), R.string.remove_favorite, Toast.LENGTH_SHORT).show();
            mIsFavorite = false;
        }
        song.setmIsFavorite(mIsFavorite);
        mFavoriteControl.updateUI(id, mIsFavorite);
        return true;
    }
}
