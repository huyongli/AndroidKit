package com.laohu.kit.extensions

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes

fun TextView.setLink(
    @StringRes text: Int = 0,
    @StringRes linkText: Int,
    @ColorRes linkTextColor: Int = 0,
    underLine: Boolean = false,
    clickListener: ((view: View) -> Unit)?
) {
    this.setLink(
        text = if (text != 0) context.getString(text) else "",
        linkText = context.getString(linkText),
        linkTextColor = linkTextColor,
        underLine = underLine,
        clickListener = clickListener
    )
}

fun TextView.setLink(
    text: String = "",
    linkText: String,
    @ColorRes linkTextColor: Int = 0,
    underLine: Boolean = false,
    clickListener: ((view: View) -> Unit)?
) {
    val start = text.length
    val end = start + linkText.length
    val linkColor = if (linkTextColor != 0)
        context.getColorCompat(linkTextColor)
    else
        this.textColors.defaultColor

    val spannable = SpannableStringBuilder(text).append(linkText).apply {
        setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                clickListener?.invoke(widget)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = underLine
                ds.color = linkColor
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(ForegroundColorSpan(linkColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    setText(spannable)
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
}