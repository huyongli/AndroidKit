package com.laohu.kit.bindings

import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

@BindingAdapter(value = ["itemBinding", "items"], requireAll = false)
fun <T> LinearLayout.bindAdapter(itemBinding: ItemBinding<T>, items: List<T>? = null) {
    this.removeAllViews()
    val layoutInflater = LayoutInflater.from(this.context)
    items?.forEach {
        val itemView = layoutInflater.inflate(itemBinding.getLayoutRes(), this, false)
        val variableId = itemBinding.getVariableId()
        if (variableId == ItemBinding.VAR_NONE) {
            return
        }

        val binding: ViewDataBinding = DataBindingUtil.bind(itemView)!!

        val result = binding.setVariable(variableId, it)
        if (!result) {
            val context = binding.root.context
            val resources = context.resources
            val layoutName = resources.getResourceName(itemBinding.getLayoutRes())
            val bindingVariableName = DataBindingUtil.convertBrIdToString(variableId)
            throw IllegalStateException("Could not bind variable '$bindingVariableName' in layout '$layoutName'")
        }

        val extraBindings = itemBinding.getExtraBind().orEmpty()
        for ((id, data) in extraBindings) {
            binding.takeIf { variableId != ItemBinding.VAR_NONE }?.setVariable(id, data)
        }

        binding.lifecycleOwner = tryGetLifecycleOwner(this)

        this.addView(itemView)
    }
}