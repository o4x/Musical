package github.o4x.musical.ui.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.o4x.appthemehelper.extensions.accentColor
import github.o4x.musical.R
import github.o4x.musical.databinding.ActivityMusicPickerBinding
import github.o4x.musical.extensions.applyToolbar
import github.o4x.musical.extensions.showToast
import github.o4x.musical.repository.SongRepository
import github.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import github.o4x.musical.ui.activities.intro.PermissionActivity
import github.o4x.musical.ui.adapter.song.SelectSongAdapter
import github.o4x.musical.ui.fragments.mainactivity.search.SearchFragment
import github.o4x.musical.ui.fragments.mainactivity.search.clearText
import github.o4x.musical.util.MusicUtil
import github.o4x.musical.util.ViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.*

class MusicPickerActivity : AbsMusicServiceActivity(), TextWatcher {

    companion object {
        const val SONG_ID = "song_id"
    }

    val songRepository by inject<SongRepository>()

    private lateinit var selectSongAdapter: SelectSongAdapter
    private var query: String? = null

    private val binding by lazy { ActivityMusicPickerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions()) {
            val myIntent = Intent(
                this,
                PermissionActivity::class.java
            )

            this.startActivity(myIntent)
        }

        setContentView(binding.root)
        applyToolbar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        setStatusBarColorAuto()

        selectSongAdapter =
            SelectSongAdapter(this, emptyList(), R.layout.item_list, null) {
                val returnIntent = Intent()
                returnIntent.data = MusicUtil.getFileUriFromSong(it.id)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        binding.recyclerView.apply {

            ViewUtil.setUpFastScrollRecyclerViewColor(
                this@MusicPickerActivity,
                this,
                accentColor()
            )

            adapter = selectSongAdapter
            layoutManager = LinearLayoutManager(this@MusicPickerActivity)
            selectSongAdapter.registerAdapterDataObserver(object :
                RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    binding.empty.isVisible = selectSongAdapter.itemCount < 1
                }
            })
        }

        binding.search.apply {
            searchView.addTextChangedListener(this@MusicPickerActivity)
            voiceSearch.setOnClickListener { startMicSearch() }
            clearText.setOnClickListener { searchView.clearText() }
        }

        updateSongs()
    }

    override fun afterTextChanged(newText: Editable?) {
        search(newText.toString())
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private fun search(query: String) {
        this.query = query
        binding.search.voiceSearch.isGone = query.isNotEmpty()
        binding.search.clearText.isVisible = query.isNotEmpty()

        updateSongs()
    }

    private fun updateSongs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val songs = if (query == null)
                songRepository.songs() else songRepository.songs(query!!)

            withContext(Dispatchers.Main) {
                selectSongAdapter.swapDataSet(songs)
            }
        }
    }

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        updateSongs()
    }

    private fun startMicSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt))
        try {
            startActivityForResult(
                intent,
                SearchFragment.REQ_CODE_SPEECH_INPUT
            )
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            showToast(getString(R.string.speech_not_supported))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}