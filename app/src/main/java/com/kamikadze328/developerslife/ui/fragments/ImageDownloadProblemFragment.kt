package com.kamikadze328.developerslife.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.kamikadze328.developerslife.R


class ImageDownloadProblemFragment : Fragment() {
    private val listeners = mutableListOf<ImageDownloadProblemClickedListener>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_download_problem, container, false)
        val button = view.findViewById<ImageButton>(R.id.imageDownloadProblemButton)
        button.setOnClickListener { notifyListeners() }
        return view
    }

    private fun notifyListeners() {
        listeners.forEach { it.imageDownloadProblemClicked() }
    }

    fun addListeners(listener: ImageDownloadProblemClickedListener) {
        listeners.add(listener)
    }
}