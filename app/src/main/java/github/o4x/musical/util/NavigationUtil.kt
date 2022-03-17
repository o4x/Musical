package github.o4x.musical.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.core.app.ActivityCompat
import github.o4x.musical.R
import github.o4x.musical.helper.MusicPlayerRemote.audioSessionId
import github.o4x.musical.ui.activities.details.ArtistDetailActivity
import github.o4x.musical.ui.activities.details.AlbumDetailActivity
import github.o4x.musical.model.Song
import github.o4x.musical.ui.activities.tageditor.SongTagEditorActivity
import github.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import github.o4x.musical.model.Album
import github.o4x.musical.model.Artist
import github.o4x.musical.ui.activities.LicenseActivity
import github.o4x.musical.ui.activities.details.AbsDetailActivity
import github.o4x.musical.ui.activities.tageditor.AlbumTagEditorActivity
import github.o4x.musical.ui.activities.tageditor.ArtistTagEditorActivity

object NavigationUtil {

    @JvmStatic
    fun goToArtist(activity: Activity, artistId: Long) {
        val intent = Intent(activity, ArtistDetailActivity::class.java)
        intent.putExtra(AbsDetailActivity.EXTRA_ID, artistId)
        activity.startActivity(intent)
    }

    @JvmStatic
    fun goToAlbum(activity: Activity, albumId: Long) {
        val intent = Intent(activity, AlbumDetailActivity::class.java)
        intent.putExtra(AbsDetailActivity.EXTRA_ID, albumId)
        activity.startActivity(intent)
    }

    @JvmStatic
    fun goToSongTagEditor(activity: Activity, song: Song) {
        val tagEditorIntent = Intent(activity, SongTagEditorActivity::class.java)
        tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
        activity.startActivity(tagEditorIntent)
    }

    @JvmStatic
    fun goToAlbumTagEditor(activity: Activity, album: Album) {
        val intent = Intent(activity, AlbumTagEditorActivity::class.java)
        intent.putExtra(AbsTagEditorActivity.EXTRA_ID, album.id)
        activity.startActivityForResult(intent, AbsDetailActivity.TAG_EDITOR_REQUEST)
    }

    @JvmStatic
    fun goToArtistTagEditor(activity: Activity, artist: Artist) {
        val editor = Intent(activity, ArtistTagEditorActivity::class.java)
        editor.putExtra(AbsTagEditorActivity.EXTRA_ID, artist.id)
        activity.startActivityForResult(editor, AbsDetailActivity.TAG_EDITOR_REQUEST)
    }

    @JvmStatic
    fun openEqualizer(activity: Activity) {
        val sessionId = audioSessionId
        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            Toast.makeText(
                activity,
                activity.resources.getString(R.string.no_audio_ID),
                Toast.LENGTH_LONG
            ).show()
        } else {
            try {
                val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                activity.startActivityForResult(effects, 0)
            } catch (notFound: ActivityNotFoundException) {
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.no_equalizer),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @JvmStatic
    fun goToOpenSource(activity: Activity) {
        ActivityCompat.startActivity(activity, Intent(activity, LicenseActivity::class.java), null)
    }
}