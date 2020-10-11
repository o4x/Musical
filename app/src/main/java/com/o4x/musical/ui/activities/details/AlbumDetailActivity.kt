package com.o4x.musical.ui.activities.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.loader.content.Loader
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote.enqueue
import com.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import com.o4x.musical.helper.MusicPlayerRemote.playNext
import com.o4x.musical.interfaces.LoaderIds
import com.o4x.musical.misc.WrappedAsyncTaskLoader
import com.o4x.musical.model.Album
import com.o4x.musical.model.Album.Companion.empty
import com.o4x.musical.model.Song
import com.o4x.musical.repository.RealAlbumRepository
import com.o4x.musical.repository.RealSongRepository
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import com.o4x.musical.ui.activities.tageditor.AlbumTagEditorActivity
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog
import com.o4x.musical.ui.dialogs.DeleteSongsDialog
import com.o4x.musical.ui.dialogs.SleepTimerDialog
import com.o4x.musical.ui.viewmodel.AlbumDetailsViewModel
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil.isAllowedToDownloadMetadata
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

/**
 * Be careful when changing things in this Activity!
 */
class AlbumDetailActivity : AbsDetailActivity() {

    companion object {
        const val EXTRA_ALBUM_ID = "extra_album_id"
    }

    private val detailsViewModel by viewModel<AlbumDetailsViewModel> {
        parametersOf(intent.extras!!.getLong(EXTRA_ALBUM_ID))
    }

    private var album: Album? = null

    override fun initObserver() {
        detailsViewModel.getAlbum().observe(this, {
            setAlbum(it)
        })
    }

    override fun loadImage() {
        imageLoader.loadImage(album!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_album_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun loadWiki(lang: String? = Locale.getDefault().language) {
        wiki = null

//        ApiClient.getClient(this).create(LastFMService.class)
//                .getAlbumInfo(getAlbum().getTitle(), getAlbum().getArtistName(), lang)
//                .enqueue(new Callback<LastFmAlbum>() {
//                    @Override
//                    public void onResponse(@NonNull Call<LastFmAlbum> call, @NonNull Response<LastFmAlbum> response) {
//                        final LastFmAlbum lastFmAlbum = response.body();
//                        if (lastFmAlbum != null && lastFmAlbum.getAlbum() != null && lastFmAlbum.getAlbum().getWiki() != null) {
//                            final String wikiContent = lastFmAlbum.getAlbum().getWiki().getContent();
//                            if (wikiContent != null && !wikiContent.trim().isEmpty()) {
//                                wiki = Html.fromHtml(wikiContent);
//                            }
//                        }
//
//                        // If the "lang" parameter is set and no wiki is given, retry with default language
//                        if (wiki == null && lang != null) {
//                            loadWiki(null);
//                            return;
//                        }
//
//                        if (!PreferenceUtil.isAllowedToDownloadMetadata(AlbumDetailActivity.this)) {
//                            if (wiki != null) {
//                                wikiDialog.setContent(wiki);
//                            } else {
//                                wikiDialog.dismiss();
//                                Toast.makeText(AlbumDetailActivity.this, getResources().getString(R.string.wiki_unavailable), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<LastFmAlbum> call, @NonNull Throwable t) {
//                        t.printStackTrace();
//                    }
//                });
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val songs = songAdapter.dataSet
        when (id) {
            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(supportFragmentManager, "SET_SLEEP_TIMER")
                return true
            }
            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(this)
                return true
            }
            R.id.action_shuffle_album -> {
                openAndShuffleQueue(songs, true)
                return true
            }
            R.id.action_play_next -> {
                playNext(songs)
                return true
            }
            R.id.action_add_to_current_playing -> {
                enqueue(songs)
                return true
            }
            R.id.action_add_to_playlist -> {
                AddToPlaylistDialog.create(songs).show(supportFragmentManager, "ADD_PLAYLIST")
                return true
            }
            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(songs).show(supportFragmentManager, "DELETE_SONGS")
                return true
            }
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            R.id.action_tag_editor -> {
                val intent = Intent(this, AlbumTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, getAlbum().id)
                startActivityForResult(intent, TAG_EDITOR_REQUEST)
                return true
            }
            R.id.action_go_to_artist -> {
                NavigationUtil.goToArtist(this, getAlbum().artistId)
                return true
            }
            R.id.action_wiki -> {
                if (wikiDialog == null) {
                    wikiDialog = MaterialDialog.Builder(this)
                        .title(album!!.title!!)
                        .positiveText(android.R.string.ok)
                        .build()
                }
                if (isAllowedToDownloadMetadata(this)) {
                    if (wiki != null) {
                        wikiDialog?.setContent(wiki)
                        wikiDialog?.show()
                    } else {
                        Toast.makeText(
                            this,
                            resources.getString(R.string.wiki_unavailable),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    wikiDialog?.show()
                    loadWiki()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAlbum(album: Album) {
        this.album = album
        loadImage()
        if (isAllowedToDownloadMetadata(this)) {
            loadWiki()
        }

//        artistTextView.setText(album.getArtistName());
//        songCountTextView.setText(MusicUtil.getSongCountString(this, album.getSongCount()));
//        durationTextView.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, album.getSongs())));
//        albumYearTextView.setText(MusicUtil.getYearString(album.getYear()));
        supportActionBar!!.title = album.title
        songAdapter.swapDataSet(album.songs)
        songAdapter.data = album
    }

    private fun getAlbum(): Album {
        if (album == null) album = empty
        return album!!
    }

    override fun getSongs(): List<Song> {
        return getAlbum().songs
    }
}