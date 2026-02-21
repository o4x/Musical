package github.o4x.musical.ui.adapter.artist;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import github.o4x.musical.R;
import github.o4x.musical.helper.SortOrder;
import github.o4x.musical.model.Artist;
import github.o4x.musical.model.Song;
import github.o4x.musical.ui.adapter.base.AbsAdapter;
import github.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import github.o4x.musical.util.MusicUtil;
import github.o4x.musical.util.NavigationUtil;
import github.o4x.musical.prefs.PreferenceUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends AbsAdapter<ArtistAdapter.ViewHolder, Artist> {

    public ArtistAdapter(@NonNull AppCompatActivity activity, List<Artist> dataSet, @LayoutRes int itemLayoutRes) {
        super(activity, dataSet, itemLayoutRes);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).getId();
    }

    @Override
    protected ViewHolder createViewHolder(@NotNull View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Artist artist = dataSet.get(position);

        if (holder.title != null) {
            holder.title.setText(artist.getName());
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
        }

    }

    @Override
    protected void loadImage(Artist artist, final ViewHolder holder) {
        if (holder.image == null) return;
        getImageLoader(holder)
                .load(artist)
                .into(holder.image);
    }

    @Override
    protected String getName(Artist artist) {
        return artist.getName();
    }

    @Override
    @NonNull
    protected List<Song> getSongList(@NonNull List<Artist> artists) {
        final List<Song> songs = new ArrayList<>();
        for (Artist artist : artists) {
            songs.addAll(artist.getSongs()); // maybe async in future?
        }
        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        @Nullable String sectionName = null;
        switch (PreferenceUtil.getArtistSortOrder()) {
            case SortOrder.ArtistSortOrder.ARTIST_A_Z:
            case SortOrder.ArtistSortOrder.ARTIST_Z_A:
                sectionName = dataSet.get(position).getName();
                break;
        }

        return MusicUtil.getSectionName(sectionName);
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_artist_image));
            if (menu != null) {
                menu.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()).getId());
        }
    }
}
