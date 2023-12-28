package com.labs.thenewshours.repository

import com.labs.thenewshours.api.RetrofitInstance
import com.labs.thenewshours.db.ArticleDatabase
import com.labs.thenewshours.models.Article

class NewsRepository (val db: ArticleDatabase) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.apiCall.getBreakingNews(countryCode, pageNumber)

    suspend fun getSearchNews(query: String, pageNumber: Int) =
        RetrofitInstance.apiCall.searchForNews(query, pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getSavedNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)
}