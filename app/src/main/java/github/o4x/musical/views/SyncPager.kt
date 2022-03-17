package github.o4x.musical.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class SyncPager : ViewPager {

    private var mSyncPagers = mutableListOf<SyncPager>()
    private var forSuper = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun addSyncViewPager(syncPager: SyncPager) {
        if (syncPager != this && !mSyncPagers.contains(syncPager)) {
            mSyncPagers.add(syncPager)
        }
    }

    private fun forSuper(forSuper: Boolean) {
        this.forSuper = forSuper
    }

    override fun onInterceptTouchEvent(arg0: MotionEvent?): Boolean {
        if (!forSuper) {
            for (mSyncPager in mSyncPagers)
                mSyncPager.apply {
                    forSuper(true)
                    onInterceptTouchEvent(arg0)
                    forSuper(false)
                }
        }
        return super.onInterceptTouchEvent(arg0)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(arg0: MotionEvent?): Boolean {
        if (!forSuper) {
            for (mSyncPager in mSyncPagers)
                mSyncPager.apply {
                    forSuper(true)
                    onTouchEvent(arg0)
                    forSuper(false)
                }
        }
        return super.onTouchEvent(arg0)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        if (!forSuper) {
            for (mSyncPager in mSyncPagers)
                mSyncPager.apply {
                    forSuper(true)
                    setCurrentItem(item, smoothScroll)
                    forSuper(false)
                }
        }
        super.setCurrentItem(item, smoothScroll)
    }

    override fun setCurrentItem(item: Int) {
        if (!forSuper) {
            for (mSyncPager in mSyncPagers)
                mSyncPager.apply {
                    forSuper(true)
                    currentItem = item
                    forSuper(false)
                }
        }
        super.setCurrentItem(item)
    }
}