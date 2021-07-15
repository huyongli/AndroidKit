package com.laohu.kit.bindings

import android.content.res.Resources
import android.graphics.Color
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.databinding.BindingAdapter
import com.laohu.kit.util.LHLogKit

@BindingAdapter("textOrResource")
fun TextView.textOrResource(text: Any?) {
    if (text == null) {
        this.text = ""
        return
    }
    when (text) {
        is String -> {
            try {
                val intValue = text.toInt()
                val string = this.context.resources.getString(intValue)
                this.text = string
                return
            } catch (e: NumberFormatException) {
            } catch (e: Resources.NotFoundException) {
            }
            this.text = text
        }
        is Spanned -> {
            this.setText(text)
            text.getSpans<Any>().forEach {
                if (it is ClickableSpan) {
                    movementMethod = LinkMovementMethod.getInstance()
                    highlightColor = Color.TRANSPARENT
                    return@forEach
                }
            }
        }
        is Int -> {
            if (text > 0) {
                this.setText(text)
            }
        }
        else -> LHLogKit.e("Not implemented")
    }
}

@BindingAdapter("textOrGone")
fun TextView.textOrGone(text: Any?) {
    when (text) {
        is String -> {
            if (text.isEmpty()) {
                this.visibility = View.GONE
            } else {
                this.text = text
                this.visibility = View.VISIBLE
            }
        }
        is Int -> {
            if (text == 0) {
                this.visibility = View.GONE
            } else {
                this.setText(text)
                this.visibility = View.VISIBLE
            }
        }
        is Spanned -> {
            if (text.isEmpty()) {
                this.visibility = View.GONE
            } else {
                this.text = text
                this.visibility = View.VISIBLE
            }
        }
        else -> this.visibility = View.GONE
    }
}