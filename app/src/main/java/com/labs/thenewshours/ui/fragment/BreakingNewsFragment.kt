package com.labs.thenewshours.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.thenewshours.R
import com.labs.thenewshours.adapter.NewsAdapter
import com.labs.thenewshours.ui.NewsActivity
import com.labs.thenewshours.ui.NewsViewModel
import com.labs.thenewshours.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.labs.thenewshours.utils.Resource

class BreakingNewsFragment: Fragment(R.layout.fragment_break) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel = (activity as NewsActivity).newsViewModelSetup
        setupRecyclerView(view)

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }


        newsViewModel.breakingNews.observe(viewLifecycleOwner) { response ->
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
                        Log.e(" Breaking News Fragment ", "An error occured: $message")
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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                newsViewModel.getBreakingNews("in")
                isScrolling = false
            } else {
                view?.findViewById<RecyclerView>(R.id.breaking_recycler)?.setPadding(0, 0, 0, 0)
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
        view.findViewById<RecyclerView>(R.id.breaking_recycler).apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }
}