package com.laohu.kit.util

import android.graphics.Rect
import android.view.View

private const val UP = 1
private const val DOWN = -1
private const val UP_TRIGGER_RATIO = 0.2f

class KeyboardStateListener(
    block: KeyboardStateListener.() -> Unit = {}
) : View.OnLayoutChangeListener {

    private var lastDirection = 0
    private var lastHeight = 0

    val isUp get() = lastDirection == UP
    var onUp: ((Int) -> Unit)? = null
    var onDown: ((Int) -> Unit)? = null

    init {
        block(this)
    }

    override fun onLayoutChange(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        val rootView = view.rootView
        val rect = Rect().apply { rootView.getWindowVisibleDisplayFrame(this) }

        val screenHeight = rootView.height
        val keyboardHeight = screenHeight - rect.bottom

        val direction = if (keyboardHeight > screenHeight * UP_TRIGGER_RATIO) UP else DOWN
        if (direction != lastDirection || keyboardHeight != lastHeight) {
            (if (direction == UP) onUp else onDown)?.invoke(keyboardHeight)
            lastDirection = direction
            lastHeight = keyboardHeight
        }
    }
}
