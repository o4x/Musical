package com.o4x.musical.ui.adapter.song;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.model.Song;
import com.o4x.musical.util.MusicUtil;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongAdapter extends SongAdapter {

    public AlbumSongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Song song = dataSet.get(position);

        if (holder.imageText != null) {
            final int trackNumber = MusicUtil.getFixedTrackNumber(song.getTrackNumber());
            final String trackNumberString = trackNumber > 0 ? String.valueOf(trackNumber) : "-";
            holder.imageText.setText(trackNumberString);
        }
    }

    @Override
    protected String getSongText(Song song) {
        return MusicUtil.getReadableDurationString(song.getDuration());
    }

    public class ViewHolder extends SongAdapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (imageText != null) {
                imageText.setVisibility(View.VISIBLE);
            }
            if (image != null) {
                image.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void loadAlbumCover(Song song, SongAdapter.ViewHolder holder) {
        // We don't want to load it in this adapter
    }
}
