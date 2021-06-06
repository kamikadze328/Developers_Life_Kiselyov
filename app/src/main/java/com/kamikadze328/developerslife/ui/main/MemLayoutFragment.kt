package com.kamikadze328.developerslife.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kamikadze328.developerslife.R
import com.kamikadze328.developerslife.additional.CATEGORY
import com.kamikadze328.developerslife.additional.Downloader
import com.kamikadze328.developerslife.databinding.FragmentMainBinding
import com.kamikadze328.developerslife.ui.fragments.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


/**
 * A placeholder fragment containing a simple view.
 */
class MemLayoutFragment : Fragment(), ImageDownloadProblemClickedListener {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private lateinit var category: CATEGORY
    private var categoryNumber = 0

    private var internetProblemFragment = InternetProblemFragment()
    private val imageDownloadProblemFragment = ImageDownloadProblemFragment()
    private var memFragment = MemFragment()
    private val loadingFragment = LoadingFragment()
    private var downloader = Downloader()

    private val cache = mutableListOf<JSONObject>()
    private var currentImageNumber = 0
    private var currentPage = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryNumber = arguments?.getInt(ARG_SECTION_NUMBER) ?: 0
        title = arguments?.getString(ARG_SECTION_TITLE).toString()
        category = CATEGORY.fromString(title) ?: CATEGORY.RANDOM
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root = binding.root
        imageDownloadProblemFragment.addListeners(this)
        Log.v("kek$categoryNumber", "onCreateView")
        return root
    }


    override fun onStart() {
        super.onStart()
        getNewMemImage()
    }

    private fun setMemDescriptionText(text: String) {
        view?.findViewById<TextView>(R.id.memDescription)?.text = text
    }

    private fun getCurrentMemDescription(): String {
        return try {
            cache[currentImageNumber].getString("description")
        } catch (e: JSONException) {
            ""
        }
    }

    private fun getCurrentMemGifUrl(): String {
        return try {
            cache[currentImageNumber].getString("gifURL")
        } catch (e: JSONException) {
            ""
        }
    }

    private fun updateMemDescriptionText() {
        setMemDescriptionText(getCurrentMemDescription())
        showMemDescription()
    }

    private fun updateMemImage() {
        val imageView = view?.findViewById<ImageView>(R.id.currentMem)
        if (imageView != null) {
            showLoading()
            Glide.with(this).asGif()
                /*.onlyRetrieveFromCache(true)*/
                .listener(object : RequestListener<GifDrawable?> {
                    override fun onResourceReady(
                        resource: GifDrawable?,
                        model: Any?,
                        target: Target<GifDrawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        hideLoading()
                        hideImageDownloadProblem()
                        resource?.start()
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        hideLoading()
                        showImageDownloadProblem()
                        return false
                    }
                })
                .load(getCurrentMemGifUrl())
                .fitCenter()
                .into(imageView)
        }
    }

    fun updateFragmentState() {
        switchToMemFragment()
        updateMemDescriptionText()
        updateMemImage()
    }

    private fun decrementCurrentImageNumber() {
        if (currentImageNumber != 0) currentImageNumber--
    }

    private fun isLoadingFragmentExists(): Boolean {
        return view?.findViewById<FrameLayout>(R.id.loadingFragment) != null
    }

    private fun isInternetProblemFragmentExists(): Boolean {
        return view?.findViewById<ConstraintLayout>(R.id.internetProblemFragment) != null
    }

    private fun isMemFragmentExists(): Boolean {
        return view?.findViewById<ConstraintLayout>(R.id.memFragment) != null
    }

    private fun isImageDownloadProblemFragmentExists(): Boolean {
        return imageDownloadProblemFragment.isAdded
    }

    private fun showMemDescription() {
        view?.findViewById<FrameLayout>(R.id.memDescriptionLayout)?.visibility = View.VISIBLE
    }

    private fun hideMemDescription() {
        switchToMemFragment()
        view?.findViewById<FrameLayout>(R.id.memDescriptionLayout)?.visibility = View.INVISIBLE

    }

    private fun switchToInternetProblemFragment() {
        if (!isInternetProblemFragmentExists()) {
            val transaction = childFragmentManager.beginTransaction()
            if (isMemFragmentExists()) {
                transaction.remove(memFragment)
            }
            transaction.add(R.id.frame, internetProblemFragment).commitNow()
        }
        setMemDescriptionText("")
    }

    private fun switchToMemFragment() {
        if (!isMemFragmentExists()) {
            val transaction = childFragmentManager.beginTransaction()
            if (isInternetProblemFragmentExists()) {
                transaction.remove(internetProblemFragment)
            }
            transaction.add(R.id.frame, memFragment).commitNow()
        }
    }

    private fun showLoading() {
        switchToMemFragment()
        if (!isLoadingFragmentExists()) {
            childFragmentManager.beginTransaction()
                .add(R.id.memFragment, loadingFragment).commitNow()
        }
    }

    private fun hideLoading() {
        if (isLoadingFragmentExists()) {
            childFragmentManager.beginTransaction()
                .remove(loadingFragment).commitNow()
        }
    }

    private fun showImageDownloadProblem() {
        switchToMemFragment()
        hideImageDownloadProblem()
        if (!isImageDownloadProblemFragmentExists()) {
            childFragmentManager.beginTransaction()
                .add(R.id.memFragment, imageDownloadProblemFragment).commitNow()
        }
    }

    private fun hideImageDownloadProblem() {
        if (isImageDownloadProblemFragmentExists()) {
            childFragmentManager.beginTransaction()
                .remove(imageDownloadProblemFragment).commitNow()
        }
    }

    fun nextImage() {
        if (cache.size != 0 && cache.size != currentImageNumber) currentImageNumber++
        if (cache.size > currentImageNumber) {
            updateFragmentState()
        } else {
            if (category != CATEGORY.RANDOM && cache.size != 0) currentPage++
            getNewMemImage()
        }
    }

    fun prevImage() {
        if (currentImageNumber == 0) Toast.makeText(
            context,
            getString(R.string.first_image),
            Toast.LENGTH_SHORT
        ).show()
        else {
            decrementCurrentImageNumber()
            updateFragmentState()
        }
    }

    private fun getNewMemImage() {
        hideMemDescription()
        showLoading()
        downloader.getData(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    processDownloadErrorMeta()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    switchToMemFragment()
                    if (response.isSuccessful && response.body != null) {
                        processResponse(JSONObject(response.body!!.string()))
                        if (cache.size > 0) updateFragmentState()
                        else processNoImagesErrorMeta()
                    } else processDownloadErrorMeta()
                }
            }
        }, category, currentPage)
    }

    fun processDownloadErrorMeta() {
        if (cache.size <= currentImageNumber) currentImageNumber = cache.size + 1
        decrementCurrentImageNumber()
        if (category != CATEGORY.RANDOM) if (currentPage != 0) currentPage--
        hideLoading()
        switchToInternetProblemFragment()
    }

    fun processNoImagesErrorMeta() {
        //TODO normal error
        hideLoading()
        setMemDescriptionText(getString(R.string.no_images))
        currentPage = 0
        decrementCurrentImageNumber()
    }

    fun processResponse(json: JSONObject) {
        if (category == CATEGORY.RANDOM) {
            cache.add(json)
        } else {
            val jsonArray = json.getJSONArray("result")
            for (i in 0 until jsonArray.length()) {
                cache.add(jsonArray.getJSONObject(i))
            }
        }

    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val ARG_SECTION_TITLE = "section_title"

        @JvmStatic
        fun newInstance(sectionNumber: Int, sectionTitle: String): MemLayoutFragment {
            return MemLayoutFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                    putString(ARG_SECTION_TITLE, sectionTitle)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun imageDownloadProblemClicked() {
        updateMemImage()
    }
}