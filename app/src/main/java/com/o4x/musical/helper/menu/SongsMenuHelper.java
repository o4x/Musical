package com.o4x.musical.helper.menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.o4x.musical.R;
import com.o4x.musical.dialogs.AddToPlaylistDialog;
import com.o4x.musical.dialogs.DeleteSongsDialog;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsMenuHelper {
    public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull List<Song> songs, int menuItemId) {
        switch (menuItemId) {
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_delete_from_device:
                DeleteSongsDialog.create(songs).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                return true;
        }
        return false;
    }
}
