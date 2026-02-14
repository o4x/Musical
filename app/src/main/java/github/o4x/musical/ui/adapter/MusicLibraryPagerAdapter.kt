package github.o4x.musical.ui.adapter

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import github.o4x.musical.model.CategoryInfo
import github.o4x.musical.ui.fragments.mainactivity.library.pager.*
import github.o4x.musical.prefs.PreferenceUtil.libraryCategory
import java.lang.ref.WeakReference
import java.util.*

class MusicLibraryPagerAdapter(
    private val mContext: Context,
    private val fragmentManager: FragmentManager
) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_SET_USER_VISIBLE_HINT) {

    private val mFragmentArray = SparseArray<WeakReference<Fragment?>>()
    private val mHolderList: MutableList<Holder> = ArrayList()

    fun setCategoryInfos(categoryInfos: List<CategoryInfo>) {
        mHolderList.clear()
        for ((category, visible) in categoryInfos) {
            if (visible) {
                val fragment = MusicFragments.valueOf(
                    category.toString()
                )
                val holder = Holder()
                holder.mClassName = fragment.fragmentClass.name
                holder.title = mContext.resources
                    .getString(category.stringRes)
                    .uppercase(Locale.getDefault())
                mHolderList.add(holder)
            }
        }
        alignCache()
        notifyDataSetChanged()
    }

    fun getFragment(position: Int): Fragment? {
        val mWeakFragment = mFragmentArray[position]
        return if (mWeakFragment?.get() != null) {
            mWeakFragment.get()
        } else getItem(position)
    }

    override fun getItemPosition(fragment: Any): Int {
        var i = 0
        val size = mHolderList.size
        while (i < size) {
            val holder = mHolderList[i]
            if (holder.mClassName == fragment.javaClass.name) {
                return i
            }
            i++
        }
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val mFragment = super.instantiateItem(container, position) as Fragment
        val mWeakFragment = mFragmentArray[position]
        mWeakFragment?.clear()
        mFragmentArray.put(position, WeakReference(mFragment))
        return mFragment
    }

    override fun getItem(position: Int): Fragment {
        val mCurrentHolder = mHolderList[position]
        return fragmentManager.fragmentFactory.instantiate(
            ClassLoader.getSystemClassLoader(),
            mCurrentHolder.mClassName!!
        )
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        val mWeakFragment = mFragmentArray[position]
        mWeakFragment?.clear()
    }

    override fun getCount(): Int {
        return mHolderList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mHolderList[position].title!!
    }

    /**
     * Aligns the fragment cache with the current category layout.
     */
    private fun alignCache() {
        if (mFragmentArray.size() == 0) return
        val mappings = HashMap<String?, WeakReference<Fragment?>>(mFragmentArray.size())
        run {
            var i = 0
            val size = mFragmentArray.size()
            while (i < size) {
                val ref = mFragmentArray.valueAt(i)
                val fragment = ref.get()
                if (fragment != null) {
                    mappings[fragment.javaClass.name] = ref
                }
                i++
            }
        }
        var i = 0
        val size = mHolderList.size
        while (i < size) {
            val ref = mappings[mHolderList[i].mClassName]
            if (ref != null) {
                mFragmentArray.put(i, ref)
            } else {
                mFragmentArray.remove(i)
            }
            i++
        }
    }

    enum class MusicFragments(val fragmentClass: Class<out Fragment>) {
        SONGS(SongsFragment::class.java),
        ALBUMS(AlbumsFragment::class.java),
        ARTISTS(ArtistsFragment::class.java),
        GENRES(GenresFragment::class.java),
        PLAYLISTS(PlaylistsFragment::class.java);

        private object All {
            val FRAGMENTS = values()
        }

        companion object {
            fun of(cl: Class<*>): MusicFragments {
                val fragments = All.FRAGMENTS
                for (fragment in fragments) {
                    if (cl == fragment.fragmentClass) return fragment
                }
                throw IllegalArgumentException("Unknown music fragment $cl")
            }
        }
    }

    private class Holder {
        var mClassName: String? = null
        var title: String? = null
    }

    init {
        setCategoryInfos(libraryCategory)
    }
}