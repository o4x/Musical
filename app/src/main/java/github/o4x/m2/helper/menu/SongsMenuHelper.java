package github.o4x.m2.helper.menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import github.o4x.m2.R;
import github.o4x.m2.helper.MusicPlayerRemote;
import github.o4x.m2.model.Song;
import github.o4x.m2.ui.dialogs.AddToPlaylistDialog;
import github.o4x.m2.ui.dialogs.DeleteSongsDialog;

import java.util.List;

public class SongsMenuHelper {
    public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull List<Song> songs, int menuItemId) {
        if (menuItemId == R.id.action_play_next) {
            MusicPlayerRemote.playNext(songs);
            return true;
        } else if (menuItemId == R.id.action_add_to_current_playing) {
            MusicPlayerRemote.enqueue(songs);
            return true;
        } else if (menuItemId == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
            return true;
        } else if (menuItemId == R.id.action_delete_from_device) {
            DeleteSongsDialog.create(songs).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
            return true;
        }
        return false;
    }
}
