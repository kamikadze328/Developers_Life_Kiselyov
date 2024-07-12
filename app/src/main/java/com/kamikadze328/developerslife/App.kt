package com.kamikadze328.developerslife

import android.app.Application
import com.kamikadze328.developerslife.data.Downloader

class App : Application() {
    val downloader by lazy { Downloader() }
}