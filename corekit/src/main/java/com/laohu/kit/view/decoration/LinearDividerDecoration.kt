package com.laohu.kit.view.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laohu.kit.extensions.asTo
import com.laohu.kit.extensions.getColorCompat

class LinearDividerDecoration(
    private val space: Int,
    private val colorRes: Int = 0,
    private val ignoreStartItemSize: Int = 0,
    private val ignoreLastItemSize: Int = 0
) : RecyclerView.ItemDecoration() {
    private lateinit var divider: ColorDrawable
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val childCount = parent.adapter?.itemCount ?: 0
        val position = parent.getChildAdapterPosition(view)

        when (parent.layoutManager?.asTo<LinearLayoutManager>()?.orientation) {
            RecyclerView.VERTICAL -> getOffsetForVertical(outRect, position, childCount)
            RecyclerView.HORIZONTAL -> getOffsetForHorizontal(outRect, position, childCount)
            else -> return
        }
    }

    private fun getOffsetForVertical(outRect: Rect, position: Int, itemCount: Int) {
        outRect.takeIf { isShowStart(position, itemCount) }?.top = space
        outRect.takeIf { isShowEnd(position, itemCount) }?.bottom = space
    }

    private fun getOffsetForHorizontal(outRect: Rect, position: Int, itemCount: Int) {
        outRect.takeIf { isShowStart(position, itemCount) }?.left = space
        outRect.takeIf { isShowEnd(position, itemCount) }?.right = space
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (colorRes == 0) {
            return
        }

        val dividerColor = parent.context.getColorCompat(colorRes)
        divider = ColorDrawable(dividerColor)

        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val orientation = parent.layoutManager?.asTo<LinearLayoutManager>()?.orientation
        val childCount = parent.childCount
        val itemCount = parent.adapter?.itemCount ?: 0

        if (orientation != RecyclerView.VERTICAL) {
            return
        }

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val adapterPosition = parent.getChildAdapterPosition(child)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val isShowTop = isShowStart(adapterPosition, itemCount)
            val isLastItem = isShowEnd(adapterPosition, itemCount)

            if (isShowTop) {
                val left = child.left - params.leftMargin
                val right = child.right + params.rightMargin
                val top = child.top - params.topMargin - space
                val bottom = top + space
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }

            if (isLastItem) {
                val left = child.left - params.leftMargin
                val right = child.right + params.rightMargin
                val top = child.bottom + params.bottomMargin
                val bottom = top + space
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val orientation = parent.layoutManager?.asTo<LinearLayoutManager>()?.orientation
        val itemCount = parent.adapter?.itemCount ?: 0
        val childCount = parent.childCount

        if (orientation != RecyclerView.HORIZONTAL) {
            return
        }

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val adapterPosition = parent.getChildAdapterPosition(child)

            val isShowTop = isShowStart(adapterPosition, itemCount)
            val isLastItem = isShowEnd(adapterPosition, itemCount)

            val params = child.layoutParams as RecyclerView.LayoutParams

            if (isShowTop) {
                val top = child.top - params.topMargin
                val bottom = child.bottom + params.bottomMargin + space
                val left = child.left - params.leftMargin - space
                val right = left + space
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }

            if (isLastItem) {
                val top = child.top - params.topMargin
                val bottom = child.bottom + params.bottomMargin + space
                val left = child.right + params.rightMargin
                val right = left + space
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
        }
    }

    private fun isShowEnd(adapterPosition: Int, itemCount: Int): Boolean =
        ignoreLastItemSize == 0 && adapterPosition == itemCount - 1

    private fun isShowStart(adapterPosition: Int, itemCount: Int) =
        adapterPosition >= ignoreStartItemSize && adapterPosition <= itemCount - ignoreLastItemSize
}