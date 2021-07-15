package com.laohu.kit.view.banner

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.laohu.kit.extensions.dp
import com.laohu.kit.extensions.withAlpha
import com.laohu.kit.util.createShapeDrawable

class IndicatorView : LinearLayout {
    var selectedDrawable: Drawable
    var normalDrawable: Drawable
    var indicatorSize: Int
    var itemSpace: Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_HORIZONTAL
        selectedDrawable = createShapeDrawable(color = Color.WHITE, radius = 6.dp.toDouble())
        normalDrawable = createShapeDrawable(color = Color.WHITE.withAlpha(0.6f), radius = 6.dp.toDouble())
        indicatorSize = 6.dp.toInt()
        itemSpace = 4.dp.toInt()
    }

    fun bind(size: Int, selectIndex: Int = 0) {
        if (size != childCount) {
            removeAllViews()
            for (i in 0 until size) {
                val lp = LayoutParams(indicatorSize, indicatorSize)
                if (i != 0) {
                    lp.marginStart = itemSpace
                }
                addView(View(context), lp)
            }
        }
        setCurrentIndex(selectIndex)
    }

    fun setCurrentIndex(index: Int) {
        if (childCount == 0) return
        val selectIndex = index % childCount
        for (i in 0 until childCount) {
            val currentView = getChildAt(i)
            if (i != selectIndex) {
                currentView.background = normalDrawable
            } else {
                currentView.background = selectedDrawable
            }
        }
    }
}
