package com.example.music.fragment;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.music.R;

public class AllSongsFragment extends BaseSongListFragment {

    @Override
    public void updateAdapter() {
        mSongAdapter.getAllSongs(getContext());
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
        return true;
    }
}
