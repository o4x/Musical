package com.o4x.musical.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.o4x.musical.R;
import com.o4x.musical.model.Song;
import com.o4x.musical.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class DeleteSongsDialog extends DialogFragment {

    @NonNull
    public static DeleteSongsDialog create(Song song) {
        List<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static DeleteSongsDialog create(List<Song> songs) {
        DeleteSongsDialog dialog = new DeleteSongsDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", new ArrayList<>(songs));
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //noinspection unchecked
        final List<Song> songs = getArguments().getParcelableArrayList("songs");
        int title;
        CharSequence content;
        if (songs.size() > 1) {
            title = R.string.delete_songs_title;
            content = Html.fromHtml(getString(R.string.delete_x_songs, songs.size()));
        } else {
            title = R.string.delete_song_title;
            content = Html.fromHtml(getString(R.string.delete_song_x, songs.get(0).getTitle()));
        }
        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .content(content)
                .positiveText(R.string.delete_action)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (getActivity() == null)
                        return;
                    MusicUtil.deleteTracks(getActivity(), songs);
                })
                .build();
    }
}
