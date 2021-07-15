package com.laohu.kit.view.banner

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.use
import androidx.viewpager.widget.ViewPager
import com.laohu.kit.R
import com.laohu.kit.extensions.asTo
import com.laohu.kit.extensions.hideSelf
import com.laohu.kit.extensions.showSelf
import com.laohu.kit.view.RatioView

class BannerView : RatioView {
    private lateinit var indicatorView: IndicatorView
    private lateinit var autoScrollViewPager: AutoScrollViewPager
    private lateinit var defaultView: View

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.banner_view, this)
        defaultView = findViewById(R.id.defaultView)
        indicatorView = findViewById(R.id.indicatorView)
        autoScrollViewPager = findViewById(R.id.viewPager)
        autoScrollViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                indicatorView.setCurrentIndex(position)
            }
        })

        attrs?.let { attrSet ->
            context.obtainStyledAttributes(attrSet, R.styleable.BannerView).use {
                indicatorView.indicatorSize =
                    it.getDimensionPixelSize(R.styleable.BannerView_indicatorSize, indicatorView.indicatorSize)
                indicatorView.itemSpace =
                    it.getDimensionPixelSize(R.styleable.BannerView_indicatorSpace, indicatorView.itemSpace)
                indicatorView.normalDrawable =
                    it.getDrawable(R.styleable.BannerView_indicatorNormalDrawable) ?: indicatorView.normalDrawable
                indicatorView.selectedDrawable =
                    it.getDrawable(R.styleable.BannerView_indicatorSelectDrawable) ?: indicatorView.selectedDrawable
                val margin = it.getDimensionPixelOffset(R.styleable.BannerView_indicatorMarginBottom, 0)
                if (margin != 0) {
                    indicatorView.layoutParams.asTo<MarginLayoutParams>()?.bottomMargin = margin
                }
            }
        }
    }

    fun setPlayInterval(intervalTime: Long) {
        autoScrollViewPager.intervalTime = intervalTime
    }

    fun startPlay() = autoScrollViewPager.startPlay()

    fun stopPlay() = autoScrollViewPager.stopPlay()

    fun bind(items: List<BannerItem>) {
        val currentPos = if (items.isEmpty()) 0 else autoScrollViewPager.currentItem % items.size
        indicatorView.bind(items.size, currentPos)
        autoScrollViewPager.setItems(items, currentPos)
        if (items.size > 1) {
            autoScrollViewPager.startPlay()
        }

        if (items.isEmpty()) {
            defaultView.showSelf()
        } else {
            defaultView.hideSelf()
        }
    }

    fun setError() {
        defaultView.showSelf()
        autoScrollViewPager.hideSelf()
        indicatorView.hideSelf()
    }

    fun setItemClicker(clicker: ((BannerItem, Int) -> Unit)?) {
        autoScrollViewPager.itemClicker = clicker
    }

    fun setViewBinder(binder: ((View?, BannerItem, ViewGroup) -> View)) {
        autoScrollViewPager.viewBinder = binder
    }

    fun setSwipeListener(listener: ((BannerItem, Int) -> Unit)?) {
        autoScrollViewPager.swipeClicker = listener
    }
}