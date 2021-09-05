package com.kamikadze328.developerslife.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.kamikadze328.developerslife.R
import com.kamikadze328.developerslife.ui.data.ImageDownloadProblemClickedListener


class ImageDownloadProblemFragment : Fragment(R.layout.fragment_image_download_problem) {
    private val listeners = mutableListOf<ImageDownloadProblemClickedListener>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageButton>(R.id.imageDownloadProblemButton)
            .setOnClickListener { notifyListeners() }
    }

    private fun notifyListeners() {
        listeners.forEach { it.imageDownloadProblemClicked() }
    }

    fun addListeners(listener: ImageDownloadProblemClickedListener) {
        listeners.add(listener)
    }
}