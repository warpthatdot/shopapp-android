package com.client.shop.ui.base.ui.pagination

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import com.client.shop.R
import com.client.shop.const.Constant.DEFAULT_PER_PAGE_COUNT
import com.client.shop.ui.base.contract.BaseMvpView
import com.client.shop.ui.base.contract.BasePresenter
import com.client.shop.ui.base.ui.lce.BaseFragment
import com.client.shop.ui.base.ui.recycler.EndlessRecyclerViewScrollListener
import com.client.shop.ui.base.ui.recycler.GridSpaceDecoration
import com.client.shop.ui.base.ui.recycler.OnItemClickListener
import com.client.shop.ui.base.ui.recycler.adapter.BaseRecyclerAdapter

abstract class PaginationFragment<M, V : BaseMvpView<List<M>>, P : BasePresenter<List<M>, V>> :
        BaseFragment<List<M>, V, P>(),
        OnItemClickListener<M>,
        SwipeRefreshLayout.OnRefreshListener {

    protected val dataList = mutableListOf<M>()
    protected var paginationValue: String? = null
    protected var recycler: RecyclerView? = null
    protected var swipeRefreshLayout: SwipeRefreshLayout? = null
    protected var adapter: BaseRecyclerAdapter<M>? = null

    companion object {
        private const val SPAN_COUNT = 2
    }

    //ANDROID

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefreshLayout()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            if (item.itemId == android.R.id.home) {
                activity.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //INIT

    override fun getContentView() = R.layout.activity_pagination

    protected open fun isGrid() = false

    fun perPageCount() = DEFAULT_PER_PAGE_COUNT

    //SETUP

    abstract fun setupAdapter(): BaseRecyclerAdapter<M>

    protected open fun setupRecyclerView() {

        adapter = setupAdapter()
        recycler = view?.findViewById(R.id.recyclerView)

        recycler?.let {
            val layoutManager: RecyclerView.LayoutManager
            if (isGrid()) {
                layoutManager = GridLayoutManager(context, SPAN_COUNT)
                it.addItemDecoration(GridSpaceDecoration(resources.getDimensionPixelSize(R.dimen.recycler_padding), SPAN_COUNT))
            } else {
                layoutManager = LinearLayoutManager(context)
            }
            it.layoutManager = layoutManager
            it.adapter = adapter
            it.setHasFixedSize(true)
            it.addOnScrollListener(object : EndlessRecyclerViewScrollListener(it.layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    if (totalItemsCount >= perPageCount())
                        loadData(true)
                }
            })
        }
    }

    protected open fun setupSwipeRefreshLayout() {
        swipeRefreshLayout = view?.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout?.let {
            it.setOnRefreshListener(this)
            it.setColorSchemeResources(R.color.colorAccent)
        }
    }

    //LCE

    override fun loadData(pullToRefresh: Boolean) {
        super.loadData(pullToRefresh)
        if (pullToRefresh) {
            swipeRefreshLayout?.isRefreshing = true
        }
    }

    override fun showContent(data: List<M>) {
        super.showContent(data)
        swipeRefreshLayout?.isRefreshing = false
    }

    override fun showError(isNetworkError: Boolean) {
        super.showError(isNetworkError)
        swipeRefreshLayout?.isRefreshing = false
    }

    //CALLBACK

    override fun onRefresh() {
        paginationValue = null
        dataList.clear()
        adapter?.notifyDataSetChanged()
        loadData(true)
    }
}