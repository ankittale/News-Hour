package com.labs.thenewshours.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.thenewshours.R
import com.labs.thenewshours.adapter.NewsAdapter
import com.labs.thenewshours.ui.NewsActivity
import com.labs.thenewshours.ui.NewsViewModel
import com.labs.thenewshours.utils.Constants
import com.labs.thenewshours.utils.Constants.Companion.DELAY_CALL
import com.labs.thenewshours.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment: Fragment(R.layout.fragment_search) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    var job: Job? = null
    var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel = (activity as NewsActivity).newsViewModelSetup
        val menuHost: MenuHost = requireActivity()

        setupRecyclerView(view)

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        searchView = menuItem.actionView as SearchView
                        searchView?.setOnQueryTextListener(object : OnQueryTextListener{
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                print(query)
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                job?.cancel()
                                job = MainScope().launch {
                                    delay(DELAY_CALL)
                                    if (!newText.isNullOrEmpty()) {
                                        newsViewModel.searchNews(newText)
                                    }
                                }
                                return true
                            }

                        })
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        newsViewModel.searchNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar(view)
                    response.data?.let { newsResponse ->
                        newsAdapter.differUtil.submitList(newsResponse.articles)
                    }
                }

                is Resource.Error -> {
                    hideProgressBar(view)
                    response.message?.let { message ->
                        Log.e("Search TAG", "An error occured: $message")
                    }
                }

                is Resource.Loading -> {
                    showProgressBar(view)
                }
            }
        }
    }

    private fun hideProgressBar(view: View) {
        view.findViewById<ProgressBar>(R.id.paginationProgressBar).visibility = View.INVISIBLE
    }

    private fun showProgressBar(view: View) {
        view.findViewById<ProgressBar>(R.id.paginationProgressBar).visibility = View.VISIBLE
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                newsViewModel.searchNews(searchView?.query.toString())
                isScrolling = false
            } else {
                view?.findViewById<RecyclerView>(R.id.search_recycler)?.setPadding(0, 0, 0, 0)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        newsAdapter = NewsAdapter()
        view.findViewById<RecyclerView>(R.id.search_recycler).apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}