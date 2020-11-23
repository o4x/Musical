package com.o4x.musical.ui.fragments.player

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentPlayerBinding
import com.o4x.musical.helper.MusicPlayerRemote.clearQueue
import com.o4x.musical.helper.MusicPlayerRemote.currentSong
import com.o4x.musical.helper.MusicPlayerRemote.playingQueue
import com.o4x.musical.model.lyrics.Lyrics
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import com.o4x.musical.ui.activities.tageditor.SongTagEditorActivity
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog.Companion.create
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.dialogs.LyricsDialog.Companion.create
import com.o4x.musical.ui.dialogs.SleepTimerDialog
import com.o4x.musical.ui.dialogs.SongDetailDialog
import com.o4x.musical.ui.dialogs.SongShareDialog
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.ui.fragments.player.albumcover.AlbumCoverFragment
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.NavigationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerFragment : AbsMusicServiceFragment(R.layout.fragment_player),
    Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!


    private var playbackControlsFragment: PlayerPlaybackControlsFragments? = null
    private var playerAlbumCoverFragment: AlbumCoverFragment? = null
    var lyrics: Lyrics? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpPlayerToolbar()
        setUpSubFragments()

        playerViewModel.currentSong.observe(viewLifecycleOwner, {
            updateLyrics()
        })
    }

    private fun setUpSubFragments() {
        playbackControlsFragment =
            childFragmentManager.findFragmentById(R.id.playback_controls_fragment)
                    as PlayerPlaybackControlsFragments?
        playerAlbumCoverFragment =
            childFragmentManager.findFragmentById(R.id.player_album_cover_fragment)
                    as AlbumCoverFragment?
    }

    private fun setUpPlayerToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_player)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { serviceActivity.onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener(this)
    }

    private fun updateLyrics() {
        val song = currentSong
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                lyrics = null
                playerAlbumCoverFragment?.setLyrics(null)
            }

            val data = MusicUtil.getLyrics(song)
            lyrics = if (TextUtils.isEmpty(data)) {
                null
            } else Lyrics.parse(song, data)

            withContext(Dispatchers.Main) {
                playerAlbumCoverFragment?.setLyrics(lyrics)

                binding.toolbar.menu.findItem(R.id.action_lyrics)
                    .isVisible = lyrics != null
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = currentSong
        when (item.itemId) {
            R.id.action_lyrics -> {
                if (lyrics != null)
                    create(lyrics!!).show(childFragmentManager, "LYRICS")

                return true
            }
            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(childFragmentManager, "SET_SLEEP_TIMER")
                return true
            }
            R.id.action_share -> {
                SongShareDialog.create(song).show(childFragmentManager, "SHARE_SONG")
                return true
            }
            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(serviceActivity)
                return true
            }
            R.id.action_add_to_playlist -> {
                create(song).show(childFragmentManager, "ADD_PLAYLIST")
                return true
            }
            R.id.action_clear_playing_queue -> {
                clearQueue()
                return true
            }
            R.id.action_save_playing_queue -> {
                CreatePlaylistDialog.create(playingQueue)
                    .show(serviceActivity.supportFragmentManager, "ADD_TO_PLAYLIST")
                return true
            }
            R.id.action_tag_editor -> {
                val intent = Intent(serviceActivity, SongTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
                startActivity(intent)
                return true
            }
            R.id.action_details -> {
                SongDetailDialog.create(song).show(childFragmentManager, "SONG_DETAIL")
                return true
            }
            R.id.action_go_to_album -> {
                NavigationUtil.goToAlbum(serviceActivity, song.albumId)
                return true
            }
            R.id.action_go_to_artist -> {
                NavigationUtil.goToArtist(serviceActivity, song.artistId)
                return true
            }
        }
        return false
    }
}