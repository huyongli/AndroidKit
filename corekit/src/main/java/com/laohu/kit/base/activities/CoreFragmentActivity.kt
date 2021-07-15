package com.laohu.kit.base.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.laohu.kit.R
import com.laohu.kit.base.fragment.IBackPressView
import com.laohu.kit.extensions.asTo
import com.laohu.kit.extensions.orFalse
import java.util.*

abstract class CoreFragmentActivity : CoreActivity() {
    private val tagStack = LinkedList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container)
        if (savedInstanceState == null) {
            replaceContent(createFragment())
        }
    }

    abstract fun createFragment(): Fragment

    open fun replaceContent(fragment: Fragment) {
        val tag = getNextFragmentTag()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, fragment, tag)
            applyFragmentTag(tag)
            commit()
        }
    }

    private fun getNextFragmentTag() = (tagStack.size + 1).toString()

    private fun applyFragmentTag(tag: String) = tagStack.push(tag)

    private fun getCurrentFragment(): Fragment? {
        val tag = tagStack.size.toString()
        return supportFragmentManager.findFragmentByTag(tag)
    }

    override fun onBackPressed() {
        val contentFragment = getCurrentFragment()
        if (contentFragment?.asTo<IBackPressView>()?.onBackPressed().orFalse()) {
            return
        }
        super.onBackPressed()
    }

    override fun finish() {
        tagStack.clear()
        super.finish()
    }
}