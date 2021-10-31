package com.kamikadze328.developerslife.data

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT = 1000L
private const val CALL_TIMEOUT = 2000L

class Downloader {
    private val defaultUrl = "https://developerslife.ru/"
    private val parameters = "?json=true"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .callTimeout(CALL_TIMEOUT, TimeUnit.MILLISECONDS)
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

    fun getMemUrl(memId: Int) = defaultUrl + memId.toString()
}