package com.laohu.kit.view.banner

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.laohu.kit.extensions.asTo
import com.laohu.kit.extensions.orZero
import java.util.LinkedList
import java.util.concurrent.TimeUnit

class AutoScrollViewPager : ViewPager {
    var intervalTime = DEFAULT_INTERVAL_TIME
    private val autoPlayHandler = Handler()
    var startPlay = false
        private set

    var itemClicker: ((BannerItem, Int) -> Unit)? = null
    var swipeClicker: ((BannerItem, Int) -> Unit)? = null
    lateinit var viewBinder: ((View?, BannerItem, ViewGroup) -> View)

    private var lastTime: Long = System.currentTimeMillis()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        offscreenPageLimit = 5
        addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentTime = System.currentTimeMillis()
                val isSwipe = (currentTime - lastTime) < DEFAULT_INTERVAL_TIME
                lastTime = currentTime
                if (isSwipe) {
                    val pageAdapter = adapter?.asTo<AutoScrollAdapter>()
                    val index = position % pageAdapter?.items?.size.orZero()
                    val item = pageAdapter?.getItem(index)
                    item?.let {
                        swipeClicker?.invoke(it, index)
                    }
                }
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                pausePlayBanner()
            }

            MotionEvent.ACTION_UP -> {
                playNextBanner()
            }

            MotionEvent.ACTION_CANCEL -> {
                playNextBanner()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        playNextBanner()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pausePlayBanner()
    }

    fun startPlay() {
        startPlay = true
        playNextBanner()
    }

    fun stopPlay() {
        startPlay = false
        pausePlayBanner()
    }

    private val displayNextBanner = Runnable {
        val pageAdapter = adapter
        if (pageAdapter == null || pageAdapter.count == 0 || childCount == 0) {
            return@Runnable
        }
        val index = currentItem + 1
        val nextIndex = index % pageAdapter.count
        currentItem = nextIndex
        playNextBanner()
    }

    private fun playNextBanner() {
        if (!startPlay || !isAttachedToWindow || adapter?.asTo<AutoScrollAdapter>()?.count.orZero() <= 1) return
        autoPlayHandler.removeCallbacksAndMessages(null)
        autoPlayHandler.postDelayed(displayNextBanner, intervalTime)
    }

    private fun pausePlayBanner() {
        autoPlayHandler.removeCallbacksAndMessages(null)
    }

    fun setItems(items: List<BannerItem>, currentPos: Int = 0) {
        if (items.isNotEmpty()) {
            adapter = AutoScrollAdapter(items)
            setCurrentItem(currentPos, false)
        } else {
            adapter = null
        }
    }

    private fun bindView(cacheView: View?, item: BannerItem, container: ViewGroup): View {
        return viewBinder.invoke(cacheView, item, container)
    }

    companion object {
        val DEFAULT_INTERVAL_TIME = TimeUnit.SECONDS.toMillis(3)
    }

    inner class AutoScrollAdapter(val items: List<BannerItem>) : PagerAdapter() {
        private val cachedViews = LinkedList<View>()
        private var notify = false
        override fun isViewFromObject(view: View, any: Any): Boolean {
            return view == any
        }

        override fun getCount(): Int {
            return if (items.size > 1) {
                Integer.MAX_VALUE
            } else {
                items.size
            }
        }

        override fun getItemPosition(item: Any): Int {
            if (this.notify) return POSITION_NONE
            return super.getItemPosition(item)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val index = position % items.size
            val item = items[index]
            val view = bindView(getCacheView(), item, container)
            view.setOnClickListener {
                itemClicker?.invoke(item, index)
            }
            container.addView(view, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
            return view
        }

        fun getItem(position: Int) = items[position % items.size]

        override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
            val view = any as? View ?: return
            container.removeView(view)
            cachedViews.push(view)
        }

        private fun getCacheView(): View? {
            var view: View?
            do {
                if (cachedViews.isEmpty()) {
                    return null
                }
                view = cachedViews.pop()
            } while (view == null)
            return view
        }
    }
}

interface BannerItem {
    val url: String?
    val linkUrl: String?
    val id: String?
}

data class SimpleBannerItem(
    override val url: String?,
    override val linkUrl: String?,
    override val id: String?
) : BannerItem