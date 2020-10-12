package com.example.music.fragment;


import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.music.Interface.IFavoriteControl;
import com.example.music.R;
import com.example.music.Song;


public class AllSongsFragment extends BaseSongListFragment {

    public AllSongsFragment() {
    }

    public AllSongsFragment(IFavoriteControl favoriteControl) {
        super(favoriteControl);
    }

    @Override
    public void updateAdapter() {
        mSongAdapter.getAllSongs(getContext());
        mSongAdapter.notifyDataSetChanged();
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
        Song song = (Song) mSongAdapter.getArr().get(mPosition);
        int id = song.getmId();
        switch (item.getItemId()) {
            case R.id.popup_remove:
                mFavoriteSongsDB.setFavorite(id, 1);
                mFavoriteSongsDB.updateCount(id, 0);
                mIsFavorite = false;
                Toast.makeText(getContext(), R.string.remove_favorite, Toast.LENGTH_SHORT).show();
                break;
            case R.id.popup_add:
                mFavoriteSongsDB.setFavorite(id, 2);
                mIsFavorite = true;
                Toast.makeText(getContext(), R.string.add_to_favorite, Toast.LENGTH_SHORT).show();
                break;
        }
        song.setmIsFavorite(mIsFavorite);
        mFavoriteControl.updateUI(id, mIsFavorite);
        updateAdapter();
        return true;
    }
}
