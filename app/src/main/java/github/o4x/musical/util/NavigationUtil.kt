package github.o4x.musical.util

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityCompat
import github.o4x.musical.ui.activities.details.ArtistDetailActivity
import github.o4x.musical.ui.activities.details.AlbumDetailActivity
import github.o4x.musical.model.Song
import github.o4x.musical.ui.activities.tageditor.SongTagEditorActivity
import github.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import github.o4x.musical.model.Album
import github.o4x.musical.model.Artist
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
}