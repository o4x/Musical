package github.o4x.m2.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import github.o4x.m2.R
import github.o4x.m2.helper.MusicPlayerRemote
import github.o4x.m2.model.Song
import github.o4x.m2.util.MusicUtil
import java.io.File
import java.util.*

class DeleteSongsDialog : DialogFragment() {

    private var songsToDelete: List<Song>? = null
    private var deleteRequestLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteRequestLauncher = registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    songsToDelete?.let { songs ->
                        songs.forEach { MusicPlayerRemote.removeFromQueue(it) }
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.deleted_x_songs, songs.size),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs: List<Song>? = requireArguments().getParcelableArrayList("songs")
        songsToDelete = songs
        val title: Int
        val content: CharSequence
        if (songs!!.size > 1) {
            title = R.string.delete_songs_title
            val fileList = songs.joinToString("<br/>") { "• ${File(it.data).name}" }
            content = Html.fromHtml(getString(R.string.delete_x_songs, songs.size) + "<br/><br/>" + fileList)
        } else {
            title = R.string.delete_song_title
            val fileName = File(songs[0].data).name
            content = Html.fromHtml(getString(R.string.delete_song_x, songs[0].title) + "<br/><small>" + fileName + "</small>")
        }
        return MaterialDialog(requireContext())
            .title(title)
            .message(text = content)
            .positiveButton(R.string.delete_action) {
                if (activity == null) return@positiveButton
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val uris = songs.map { song ->
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            song.id
                        )
                    }
                    val pendingIntent = MediaStore.createDeleteRequest(
                        requireContext().contentResolver,
                        uris
                    )
                    deleteRequestLauncher?.launch(
                        IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    )
                } else {
                    MusicUtil.deleteTracks(requireActivity(), songs)
                }
            }
            .negativeButton(R.string.cancel)
    }

    companion object {
        @JvmStatic
        fun create(song: Song): DeleteSongsDialog {
            val list: MutableList<Song> = ArrayList()
            list.add(song)
            return create(list)
        }

        @JvmStatic
        fun create(songs: List<Song>?): DeleteSongsDialog {
            val dialog = DeleteSongsDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", ArrayList(songs!!))
            dialog.arguments = args
            return dialog
        }
    }
}
