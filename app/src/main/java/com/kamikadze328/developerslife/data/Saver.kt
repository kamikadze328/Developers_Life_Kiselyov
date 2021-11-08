package com.kamikadze328.developerslife.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.kamikadze328.developerslife.BuildConfig
import java.io.File
import java.nio.ByteBuffer


fun saveGif(gifDrawable: GifDrawable, context: Context): Uri? {
    val baseDir = context.externalCacheDir
    val fileName = "sharingGif.gif"

    val sharingGifFile = File(baseDir, fileName)
    gifDrawableToFile(gifDrawable, sharingGifFile)

    return try {
        FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            sharingGifFile
        )
    } catch (e: IllegalArgumentException) {
        Log.d("File Selector", "The selected file can't be shared: $sharingGifFile")
        null
    }
}

private fun gifDrawableToFile(gifDrawable: GifDrawable, gifFile: File) {
    val byteBuffer = gifDrawable.buffer
    gifFile.outputStream().use {
        val bytes = ByteArray(byteBuffer.capacity())
        (byteBuffer.duplicate().clear() as ByteBuffer).get(bytes)
        it.write(bytes, 0, bytes.size)
    }

}