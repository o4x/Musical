package com.o4x.musical.ui.activities.details

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote.enqueue
import com.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import com.o4x.musical.helper.MusicPlayerRemote.playNext
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Song
import com.o4x.musical.network.Models.LastFmArtist
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import com.o4x.musical.ui.activities.tageditor.ArtistTagEditorActivity
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog
import com.o4x.musical.ui.dialogs.SleepTimerDialog
import com.o4x.musical.ui.viewmodel.ArtistDetailsViewModel
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil.isAllowedToDownloadMetadata
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*
import com.o4x.musical.network.Result
import com.o4x.musical.ui.adapter.song.DetailsSongAdapter

/**
 * Be careful when changing things in this Activity!
 */
class ArtistDetailActivity : AbsDetailActivity<Artist>() {

    private val detailsViewModel: ArtistDetailsViewModel by viewModel {
        parametersOf(intent.extras!!.getLong(EXTRA_ID))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addMusicServiceEventListener(detailsViewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeMusicServiceEventListener(detailsViewModel)
    }

    override fun initObserver() {
        setArtist(
            detailsViewModel.loadArtistSync()
        )
        detailsViewModel.getArtist().observe(this, {
            setArtist(it)
        })
    }

    private fun setArtist(artist: Artist) {
        val isFirst = this.data == null
        this.data = artist

        if (isFirst) {
            loadImageSync()
        } else {
            loadImage()
        }

        if (isAllowedToDownloadMetadata(this)) {
            loadBiography()
        }
        toolbar.title = artist.name
        //        songCountTextView.setText(MusicUtil.getSongCountString(this, artist.getSongCount()));
//        albumCountTextView.setText(MusicUtil.getAlbumCountString(this, artist.getAlbumCount()));
//        durationTextView.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, artist.getSongs())));
        songAdapter?.swapDataSet(artist.songs)
        songAdapter?.data = artist
    }

    override fun getSongs(): List<Song> {
        return data!!.songs
    }

    override fun loadImage() {
        imageLoader.byThis(data!!).displayInTo(image)
    }

    override fun loadImageSync() {
        imageLoader.byThis(data!!).loadImageSync(image)
    }

    private fun loadBiography(lang: String? = Locale.getDefault().language) {
        (songRecyclerView.findViewHolderForAdapterPosition(0) as DetailsSongAdapter.HeaderViewHolder?)
            ?.hTitle?.text = "sssssssss"

        wiki = null
        detailsViewModel.getArtistInfo(data!!.name, lang, null)
            .observe(this, { result ->
                when (result) {
                    is Result.Loading -> println("Loading")
                    is Result.Error -> println("Error")
                    is Result.Success -> artistInfo(result.data)
                }
            })

//        ApiClient.getClient(this).create(LastFMService.class)
//                .getArtistInfo(getArtist().getName(), lang, null)
//                .enqueue(new Callback<LastFmArtist>() {
//                    @Override
//                    public void onResponse(@NonNull Call<LastFmArtist> call, @NonNull Response<LastFmArtist> response) {
//                        final LastFmArtist lastFmArtist = response.body();
//                        if (lastFmArtist != null && lastFmArtist.getArtist() != null) {
//                            final String bioContent = lastFmArtist.getArtist().getBio().getContent();
//                            if (bioContent != null && !bioContent.trim().isEmpty()) {
//                                biography = Html.fromHtml(bioContent);
//                            }
//                        }
//
//                        // If the "lang" parameter is set and no biography is given, retry with default language
//                        if (biography == null && lang != null) {
//                            loadBiography(null);
//                            return;
//                        }
//
//                        if (!PreferenceUtil.isAllowedToDownloadMetadata(ArtistDetailActivity.this)) {
//                            if (biography != null) {
//                                biographyDialog.setContent(biography);
//                            } else {
//                                biographyDialog.dismiss();
//                                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.biography_unavailable), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<LastFmArtist> call, @NonNull Throwable t) {
//                        t.printStackTrace();
//                        biography = null;
//                    }
//                });
    }

    private fun artistInfo(lastFmArtist: LastFmArtist?) {
//        if (lastFmArtist != null && lastFmArtist.artist != null) {
//            val bioContent = lastFmArtist.artist.bio.content
//            if (bioContent != null && bioContent.trim { it <= ' ' }.isNotEmpty()) {
//                biographyText.visibility = View.VISIBLE
//                biographyTitle.visibility = View.VISIBLE
//                biography = HtmlCompat.fromHtml(bioContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
//                biographyText.text = biography
//                if (lastFmArtist.artist.stats.listeners.isNotEmpty()) {
//                    listeners.show()
//                    listenersLabel.show()
//                    scrobbles.show()
//                    scrobblesLabel.show()
//                    listeners.text =
//                        RetroUtil.formatValue(lastFmArtist.artist.stats.listeners.toFloat())
//                    scrobbles.text =
//                        RetroUtil.formatValue(lastFmArtist.artist.stats.playcount.toFloat())
//                }
//            }
//        }
//
//        // If the "lang" parameter is set and no biography is given, retry with default language
//        if (biography == null && lang != null) {
//            loadBiography(artist.name, null)
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_artist_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val songs = getSongs()
        when (id) {
            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(supportFragmentManager, "SET_SLEEP_TIMER")
                return true
            }
            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(this)
                return true
            }
            R.id.action_shuffle_artist -> {
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
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            R.id.action_biography -> {
                if (wikiDialog == null) {
                    wikiDialog = MaterialDialog.Builder(this)
                        .title(data!!.name)
                        .positiveText(android.R.string.ok)
                        .build()
                }
                if (isAllowedToDownloadMetadata(this@ArtistDetailActivity)) { // wiki should've been already downloaded
                    if (wiki != null) {
                        wikiDialog?.setContent(wiki)
                        wikiDialog?.show()
                    } else {
                        Toast.makeText(
                            this@ArtistDetailActivity,
                            resources.getString(R.string.biography_unavailable),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else { // force download
                    wikiDialog?.show()
                    loadBiography()
                }
                return true
            }
            R.id.action_reset_artist_image -> {
                Toast.makeText(
                    this@ArtistDetailActivity,
                    resources.getString(R.string.updating),
                    Toast.LENGTH_SHORT
                ).show()
                CustomImageUtil(data).resetCustomImage()
                return true
            }
            R.id.action_tag_editor -> {
                val editor = Intent(this, ArtistTagEditorActivity::class.java)
                editor.putExtra(AbsTagEditorActivity.EXTRA_ID, data!!.id)
                startActivityForResult(editor, TAG_EDITOR_REQUEST)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}