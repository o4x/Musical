package com.o4x.musical.ui.fragments.mainactivity.search

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.o4x.musical.R
import com.o4x.musical.extensions.showToast
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.ui.adapter.SearchAdapter
import com.o4x.musical.ui.fragments.mainactivity.AbsPopupFragment
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*
import kotlin.collections.ArrayList

class SearchFragment : AbsPopupFragment(R.layout.fragment_search), TextWatcher {

    companion object {
        const val QUERY = "query"
        const val REQ_CODE_SPEECH_INPUT = 9001
    }

    private lateinit var searchView: TextInputEditText
    private lateinit var voiceSearch: AppCompatImageView
    private lateinit var clearText: AppCompatImageView

    private lateinit var searchAdapter: SearchAdapter
    private var query: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val search = mainActivity.search
        search.visibility = View.VISIBLE
        searchView = search.findViewById(R.id.search_view)
        voiceSearch = search.findViewById(R.id.voice_search)
        clearText = search.findViewById(R.id.clear_text)


        setupRecyclerView()
        searchView.addTextChangedListener(this)
        voiceSearch.setOnClickListener { startMicSearch() }
        clearText.setOnClickListener { searchView.clearText() }

        searchView.requestFocus()
        // show keyboard
        val inputManager = getSystemService(
            requireContext(),
            InputMethodManager::class.java
        )
        inputManager?.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY)
        }
        libraryViewModel.getSearchResult().observe(viewLifecycleOwner, {
            showData(it)
        })
    }

    private fun showData(data: List<Any>) {
        if (data.isNotEmpty()) {
            searchAdapter.swapDataSet(data)
        } else {
            searchAdapter.swapDataSet(ArrayList())
        }
    }


    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(requireActivity() as AppCompatActivity, emptyList())
        searchAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                empty.isVisible = searchAdapter.itemCount < 1
            }
        })
        recyclerView.apply {
            layoutManager = OverScrollLinearLayoutManager(requireContext())
            adapter = searchAdapter
            addAppbarListener()
        }
    }

    override fun afterTextChanged(newText: Editable?) {
        search(newText.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private fun search(query: String) {
        this.query = query
//        TransitionManager.beginDelayedTransition(mainActivity.appbar)
        voiceSearch.isGone = query.isNotEmpty()
        clearText.isVisible = query.isNotEmpty()
        libraryViewModel.search(query)
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
                REQ_CODE_SPEECH_INPUT
            )
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            showToast(getString(R.string.speech_not_supported))
        }
    }
}

fun TextInputEditText.clearText() {
    text = null
}