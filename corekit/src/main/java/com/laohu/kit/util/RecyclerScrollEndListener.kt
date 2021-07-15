package com.laohu.kit.util

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * 继承自RecyclerView.OnScrollListener，可以监听到是否滑动到页面最低部
 */
class RecyclerScrollEndListener(private val onScrollEnd: () -> Unit) :
    RecyclerView.OnScrollListener() {
    private val TAG = RecyclerScrollEndListener::class.java.simpleName

    private val lastItemToLoading = 1

    /**
     * 最后一个的位置
     */
    private var lastPositions: IntArray? = null

    /**
     * 最后一个可见的item的位置
     */
    private var lastVisibleItemPosition: Int = 0

    private fun findLastItems(recyclerView: RecyclerView) {
        when (val layoutManager = recyclerView.layoutManager) {
            is LinearLayoutManager -> lastVisibleItemPosition =
                layoutManager.findLastVisibleItemPosition()
            is GridLayoutManager -> lastVisibleItemPosition =
                layoutManager.findLastVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                val staggeredGridLayoutManager = layoutManager as StaggeredGridLayoutManager?
                if (lastPositions == null) {
                    lastPositions = IntArray(staggeredGridLayoutManager!!.spanCount)
                }
                staggeredGridLayoutManager!!.findLastVisibleItemPositions(lastPositions)
                lastVisibleItemPosition = findMax(lastPositions!!)
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState != RecyclerView.SCROLL_STATE_IDLE) return

        val layoutManager = recyclerView.layoutManager ?: return
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        findLastItems(recyclerView)
        if (visibleItemCount > 0 &&
            newState == RecyclerView.SCROLL_STATE_IDLE &&
            lastVisibleItemPosition >= totalItemCount - lastItemToLoading
        ) {
            Log.i(TAG, "call onScrollEnd; time:${System.currentTimeMillis()}")
            onScrollEnd.invoke()
        }
    }

    /**
     * 取数组中最大值
     *
     * @param lastPositions
     * @return
     */
    private fun findMax(lastPositions: IntArray): Int {
        var max = lastPositions[0]
        for (value in lastPositions) {
            if (value > max) {
                max = value
            }
        }
        return max
    }
}
