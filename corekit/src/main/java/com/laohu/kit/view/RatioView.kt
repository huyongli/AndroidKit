package com.laohu.kit.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.use
import com.laohu.kit.R

open class RatioView : FrameLayout {
    private var heightRatio: Float = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.let { attr ->
            context.obtainStyledAttributes(attr, R.styleable.RatioView).use {
                heightRatio = it.getFloat(R.styleable.RatioView_heightRatio, 0f)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (heightRatio <= 0F) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val originalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val calculatedHeight = originalWidth * heightRatio
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(calculatedHeight.toInt(), MeasureSpec.EXACTLY))
    }
}