package com.laohu.kit.bindings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laohu.kit.extensions.asTo
import com.laohu.kit.util.RecyclerScrollEndListener

enum class LoadMoreState {
    IDLE, LOADING, NO_MORE
}

interface ItemBinding<T> {
    fun getBindClazz(): Class<T>
    fun getVariableId(): Int
    fun getLayoutRes(): Int
    fun getExtraBind(): Map<Int, Any>?
    fun bindExtra(variableId: Int, value: Any): ItemBinding<T>

    companion object {
        const val VAR_NONE = 0

        inline fun <reified T> create(variableId: Int, @LayoutRes layoutRes: Int): ItemBinding<T> {
            return ItemBindingImpl(T::class.java, variableId, layoutRes)
        }

        fun createLoadMoreBinding(variableId: Int, @LayoutRes layoutRes: Int) =
            create<LoadMoreState>(variableId, layoutRes)
    }
}

typealias LoadMoreBinding = ItemBinding<LoadMoreState>

class ItemBindingImpl<T> constructor(
    private val clazz: Class<T>,
    private val variableId: Int = 0,
    @LayoutRes private val layoutRes: Int = 0
) : ItemBinding<T> {
    override fun getBindClazz(): Class<T> = clazz

    override fun getVariableId(): Int = variableId

    override fun getLayoutRes(): Int = layoutRes

    override fun getExtraBind(): Map<Int, Any>? = extraBindings

    private var extraBindings: MutableMap<Int, Any>? = null

    override fun bindExtra(variableId: Int, value: Any): ItemBindingImpl<T> {
        if (extraBindings == null) {
            extraBindings = mutableMapOf()
        }
        extraBindings!![variableId] = value
        return this
    }
}

private class BindingRecyclerViewAdapter<T> : RecyclerView.Adapter<BindingRecyclerViewAdapter.ViewHolder>() {
    private var observableListChangeCallback =
        object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
            override fun onChanged(sender: ObservableList<T>?) {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }

            override fun onItemRangeMoved(sender: ObservableList<T>?, fromPosition: Int, toPosition: Int, count: Int) {
                for (i in 0 until count) {
                    notifyItemMoved(fromPosition + i, toPosition + i)
                }
            }

            override fun onItemRangeInserted(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeChanged(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }
        }

    private fun generateDiffCallback(oldList: List<T>, newList: List<T>) = object : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private var attachedRecyclerView: RecyclerView? = null
    private var lifecycleOwner: LifecycleOwner? = null
    lateinit var itemBindings: List<ItemBinding<T>>
    private var data: List<T> = emptyList()
    var loadMoreBinding: LoadMoreBinding? = null
    private var loadMoreState: LoadMoreState? = null
    var loadMoreCallback: (() -> Unit)? = null
    private var loadMoreListener: RecyclerScrollEndListener =
        RecyclerScrollEndListener {
            if (loadMoreState != LoadMoreState.LOADING) loadMoreCallback?.invoke()
        }

    private fun hasLoadMoreBinding() = loadMoreBinding != null && loadMoreState != null

    private fun isLoadMoreItem(position: Int) = hasLoadMoreBinding() && position == itemCount - 1

    fun setData(itemData: List<T>) {
        if (itemData == data) return

        data.asTo<ObservableList<T>>()?.removeOnListChangedCallback(observableListChangeCallback)

        val oldList = data
        data = itemData
        diffUpdateData(oldList, data, true)

        data.asTo<ObservableList<T>>()?.addOnListChangedCallback(observableListChangeCallback)
    }

    private fun diffUpdateData(oldList: List<T>, newList: List<T>, detachMoves: Boolean = false) {
        val diffCallback = generateDiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback, detachMoves)
        diffResult.dispatchUpdatesTo(this)
    }

    fun notifyLoadMoreStateChanged(loadMoreState: LoadMoreState?) {
        if (this.loadMoreState == loadMoreState) return
        this.loadMoreState = loadMoreState
        if (loadMoreState != null) {
            notifyItemChanged(itemCount - 1)
        } else {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = data.size + if (hasLoadMoreBinding()) 1 else 0

    private fun getItemData(position: Int): Any? {
        return if (isLoadMoreItem(position)) {
            loadMoreState
        } else {
            data[position]
        }
    }

    private fun getItemBinding(position: Int): ItemBinding<*> {
        return if (isLoadMoreItem(position)) {
            loadMoreBinding!!
        } else {
            val data = data[position]
            findItemBind(data)
        }
    }

    private fun findItemBind(item: T): ItemBinding<T> {
        return itemBindings.first { it.getBindClazz().isInstance(item) }
    }

    override fun getItemViewType(position: Int): Int {
        return getItemBinding(position).getLayoutRes()
    }

    override fun onCreateViewHolder(parent: ViewGroup, layoutRes: Int) = ViewHolder(parent, layoutRes)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemView = holder.itemView
        val itemData = getItemData(position)
        val itemBinding = getItemBinding(position)
        bindViewHolderData(itemView, itemData, itemBinding)
        if (isLoadMoreItem(position)) {
            adjustVisibleForLoadMore(itemView, itemData as LoadMoreState)
        }
    }

    private fun bindViewHolderData(itemView: View, itemData: Any?, itemBinding: ItemBinding<*>) {
        val variableId = itemBinding.getVariableId()
        if (variableId == ItemBinding.VAR_NONE || itemData == null) {
            return
        }

        val binding: ViewDataBinding = DataBindingUtil.bind(itemView)!!

        val result = binding.setVariable(variableId, itemData)
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

        binding.takeIf { lifecycleOwner != null }?.lifecycleOwner = lifecycleOwner

        return
    }

    private fun adjustVisibleForLoadMore(itemView: View, loadMoreState: LoadMoreState) {
        if (attachedRecyclerView?.computeVerticalScrollOffset() == 0 && loadMoreState != LoadMoreState.LOADING) {
            itemView.visibility = View.INVISIBLE
        } else {
            itemView.visibility = View.VISIBLE
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRecyclerView = recyclerView
        lifecycleOwner = tryGetLifecycleOwner(recyclerView)

        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isLoadMoreItem(position)) layoutManager.spanCount else 1
                }
            }
        }

        // need refactor, if have load more, add listener
        recyclerView.removeOnScrollListener(loadMoreListener)
        recyclerView.addOnScrollListener(loadMoreListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        attachedRecyclerView = null
        lifecycleOwner = null
    }

    private class ViewHolder(parent: ViewGroup, layoutId: Int) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    )
}

@BindingConversion
fun <T> toItemBindings(itemBinding: ItemBinding<T>?): List<ItemBinding<T>> =
    if (itemBinding == null) listOf() else listOf(itemBinding)

@BindingConversion
fun toItemDecorations(itemDecoration: RecyclerView.ItemDecoration) = listOf(itemDecoration)

@BindingAdapter(
    value = ["itemBindings", "itemDecorations", "loadMoreBinding", "loadMoreListener", "items"],
    requireAll = false
)
fun <T> RecyclerView.bindAdapter(
    itemBindings: List<ItemBinding<T>>,
    itemDecorations: List<RecyclerView.ItemDecoration>?,
    loadMoreBinding: LoadMoreBinding?,
    loadMoreListener: Runnable?,
    items: List<T>? = null
) {
    if (itemBindings.isEmpty()) return
    val loadMoreAction: () -> Unit = { loadMoreListener?.run() }
    val adapter = this.getOrGenerateAdapter(
        itemBindings, itemDecorations, loadMoreBinding,
        if (loadMoreListener != null) loadMoreAction else null
    )
    items.takeIf { it != null }?.let { adapter.setData(it) }
}

private fun <T> RecyclerView.getOrGenerateAdapter(
    itemBindings: List<ItemBinding<T>>,
    itemDecorations: List<RecyclerView.ItemDecoration>?,
    loadMoreBinding: LoadMoreBinding?,
    loadMoreListener: (() -> Unit)?
): BindingRecyclerViewAdapter<T> {
    val oldAdapter = this.adapter?.asTo<BindingRecyclerViewAdapter<T>>()
    val adapter = oldAdapter ?: BindingRecyclerViewAdapter()
    adapter.itemBindings = itemBindings
    adapter.loadMoreBinding = loadMoreBinding
    adapter.loadMoreCallback = loadMoreListener
    this.takeIf { oldAdapter != adapter }?.adapter = adapter
    itemDecorations?.forEach {
        this.removeItemDecoration(it)
        this.addItemDecoration(it)
    }
    return adapter
}

@BindingAdapter(value = ["loadMoreState"])
fun <T> RecyclerView.bindLoadMoreState(
    loadMoreState: LoadMoreState?
) {
    val adapter = this.adapter?.asTo<BindingRecyclerViewAdapter<T>>() ?: return
    adapter.notifyLoadMoreStateChanged(loadMoreState)
}

fun tryGetLifecycleOwner(view: View): LifecycleOwner? {
    val binding = DataBindingUtil.findBinding<ViewDataBinding>(view)
    var lifecycleOwner: LifecycleOwner? = null
    if (binding != null) {
        lifecycleOwner = binding.lifecycleOwner
    }
    val ctx: Context = view.context
    if (lifecycleOwner == null && ctx is LifecycleOwner) {
        lifecycleOwner = ctx
    }
    return lifecycleOwner
}

