package com.laohu.kit.bindings

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.databinding.BindingAdapter
import com.laohu.kit.extensions.hideSelf
import com.laohu.kit.extensions.orFalse
import com.laohu.kit.extensions.showSelf
import com.laohu.kit.util.createShapeDrawable


@BindingAdapter("show")
fun View.showOrGone(show: Boolean?) {
    if (show.orFalse()) {
        this.showSelf()
    } else {
        this.hideSelf()
    }
}

@BindingAdapter(
    value = ["android:layout_marginTop", "android:layout_marginBottom", "android:layout_marginStart", "android:layout_marginEnd"],
    requireAll = false
)
fun setLayoutMargin(view: View, marginTop: Float?, marginBottom: Float?, marginStart: Float?, marginEnd: Float?) {
    val lp = view.layoutParams as? MarginLayoutParams
    lp?.let {
        lp.setMargins(
            marginStart?.toInt() ?: lp.leftMargin,
            marginTop?.toInt() ?: lp.topMargin,
            marginEnd?.toInt() ?: lp.rightMargin,
            marginBottom?.toInt() ?: lp.bottomMargin
        )
        view.layoutParams = lp
    }
}

@BindingAdapter(
    value = ["shapeSolid", "shapeRadius", "shapeStrokeColor", "shapeStrokeWidth"],
    requireAll = false
)
fun setViewShape(
    view: View, @ColorInt solid: Int? = null,
    @ColorInt strokeColor: Int? = null,
    @Dimension strokeWidth: Double? = null,
    @Dimension radius: Double? = null
) {
    view.background = createShapeDrawable(color = solid, strokeColor = strokeColor, strokeWidth = strokeWidth, radius = radius)
}