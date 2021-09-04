package com.kamikadze328.developerslife.additional

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.TimeUnit


class Downloader {
    private val defaultUrl = "https://developerslife.ru/"
    private val parameters = "?json=true"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1000, TimeUnit.MILLISECONDS)
        .callTimeout(2000, TimeUnit.MILLISECONDS)
        .build()

    private fun createUrl(category: String, page: Int = 0, isRandom: Boolean): URL {
        return URL(defaultUrl + category + (if (isRandom) "" else "/$page") + parameters)
    }


    fun getData(callback: Callback, category: Category, page: Int = 0) {
        val isRandom = category == Category.RANDOM
        val categoryStr = category.urlParam
        val url = createUrl(categoryStr, page, isRandom)
        makeRequest(url, callback)
    }

    private fun makeRequest(myUrl: URL, callback: Callback) {
        val request: Request = Request.Builder()
            .url(myUrl)
            .build()
        okHttpClient.newCall(request)
            .enqueue(callback)
    }
}