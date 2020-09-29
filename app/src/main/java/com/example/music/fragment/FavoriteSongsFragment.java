package com.example.music.fragment;


import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.music.R;
import com.example.music.database.FavoriteSongsDB;

public class FavoriteSongsFragment extends BaseSongListFragment {

    @Override
    public void updateAdapter() {
        mSongAdapter.getFavoriteList(getContext());
    }

    @Override
    public void updatePopupMenu(View view) {
        mPopup = new PopupMenu(getContext(), view.findViewById(R.id.action_more));
        mPopup.getMenuInflater().inflate(R.menu.menu_popup_media_playback, mPopup.getMenu());
        // Set a listener so we are notified if a menu item is clicked
        mPopup.setOnMenuItemClickListener(this);

        mPopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        FavoriteSongsDB favoriteSongsDB = new FavoriteSongsDB(getContext());
        switch (item.getItemId()) {
            case R.id.popup_remove:
        }
        return true;
    }
}
