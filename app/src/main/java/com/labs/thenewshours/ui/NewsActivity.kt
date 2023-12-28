package com.labs.thenewshours.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.labs.thenewshours.R
import com.labs.thenewshours.db.ArticleDatabase
import com.labs.thenewshours.repository.NewsRepository

class NewsActivity : AppCompatActivity() {

    lateinit var newsViewModelSetup: NewsViewModel
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        initView()
    }

    private fun initView() {

        val toolbar = findViewById<MaterialToolbar>(R.id.news_toolbar)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_menu)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

        setSupportActionBar(toolbar)
        if (navHostFragment != null) {
            navController = navHostFragment.findNavController()
        }
        navController.addOnDestinationChangedListener { controller, destination, arguement ->
            title = when(destination.id){
                R.id.breakingNewsFragment -> "Breaking News"
                R.id.savedNewsFragment -> "Saved News"
                R.id.searchNewsFragment -> "Search News"
                R.id.articleFragment -> "Detail Article"
                else -> "Breaking News"
            }
            if (destination.id == R.id.articleFragment){
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
        bottomNavigationView.setupWithNavController(navController)

        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        newsViewModelSetup = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]

    }

    override fun navigateUpTo(upIntent: Intent?): Boolean {
        return  navController.navigateUp()|| super.navigateUpTo(upIntent)
    }
}