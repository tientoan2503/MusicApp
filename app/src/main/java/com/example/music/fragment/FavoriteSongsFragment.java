package com.example.music.fragment;


import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;


import com.example.music.Interface.IFavoriteControl;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.database.FavoriteSongsDB;

public class FavoriteSongsFragment extends BaseSongListFragment {

    public FavoriteSongsFragment(){}

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
        FavoriteSongsDB favoriteSongsDB = new FavoriteSongsDB(getContext());
        Song song = (Song) mSongAdapter.getArr().get(mPosition);
        if (mPosition < mSongAdapter.getArr().size()) {
            int id = song.getmId();
            favoriteSongsDB.setFavorite(id, 1);
            favoriteSongsDB.updateCount(id, 0);
            Toast.makeText(getContext(), R.string.remove_favorite, Toast.LENGTH_SHORT).show();
            updateAdapter();
        }
        song.setmIsFavorite(mIsFavorite);
        mFavoriteControl.updateUI(mIsFavorite);
        return true;
    }
}
