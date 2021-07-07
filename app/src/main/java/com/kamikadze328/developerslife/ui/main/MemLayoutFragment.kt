package com.kamikadze328.developerslife.ui.main

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.kamikadze328.developerslife.additional.ImageMeta
import com.kamikadze328.developerslife.additional.STATES
import com.kamikadze328.developerslife.databinding.FragmentMemLaytoutBinding
import com.kamikadze328.developerslife.ui.fragments.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MemLayoutFragment : Fragment(), ImageDownloadProblemClickedListener {
    private var _binding: FragmentMemLaytoutBinding? = null
    private val binding get() = _binding!!

    private var category: CATEGORY = CATEGORY.RANDOM
    var categoryNumber = 0

    private var internetProblemFragment = InternetProblemFragment()
    private var imageDownloadProblemFragment = ImageDownloadProblemFragment()
    private var loadingFragment = LoadingFragment()
    private var noMemProblemFragment = NoMemProblemFragment()
    private var serverProblemFragment = ServerProblemFragment()

    private var downloader = Downloader()

    private var cache = ArrayList<ImageMeta>()
    private var currentImageNumber = 0
    private var currentPage = 0

    private var state: STATES = STATES.INIT


    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val ARG_SECTION_TITLE = "section_title"

        private const val CACHE = "cache"
        private const val STATE = "state"
        private const val CURRENT_IMAGE_NUMBER = "current_image_number"
        private const val CURRENT_PAGE = "current_page"


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryNumber = arguments?.getInt(ARG_SECTION_NUMBER) ?: 0
        arguments?.getString(ARG_SECTION_TITLE)?.let {
            category = CATEGORY.fromString(it) ?: category
        }

        if (savedInstanceState != null) {
            cache = savedInstanceState.getParcelableArrayList(toCategoryStr(CACHE)) ?: cache
            currentImageNumber = savedInstanceState.getInt(toCategoryStr(CURRENT_IMAGE_NUMBER))
            currentPage = savedInstanceState.getInt(toCategoryStr(CURRENT_PAGE))
            state = (savedInstanceState.getSerializable(toCategoryStr(STATE)) ?: state) as STATES
            Log.d("kek$categoryNumber", "currentImageNumber - $currentImageNumber")
            Log.d("kek$categoryNumber", "state - $state")

            Log.d("kek$categoryNumber", "onCreate mem${categoryNumber}. hashCode - ${hashCode()}")
        }

        context?.let { registerNetworkCallback(it) }

    }

    private fun initState() {
        when (state) {
            STATES.LOADING -> childFragmentManager.findFragmentByTag(
                getFragmentTag(loadingFragment)
            )?.let {
                loadingFragment = it as LoadingFragment
                if (currentImageNumber < cache.size) updateFragmentState()
                else getNewMem()
            }

            STATES.PROBLEM_IMAGE_DOWNLOAD -> childFragmentManager.findFragmentByTag(
                getFragmentTag(imageDownloadProblemFragment)
            )?.let {
                imageDownloadProblemFragment = it as ImageDownloadProblemFragment
                updateMemDescriptionText()
            }

            STATES.PROBLEM_NO_MEM -> childFragmentManager.findFragmentByTag(
                getFragmentTag(noMemProblemFragment)
            )?.let {
                noMemProblemFragment = it as NoMemProblemFragment
                hideMemDescription()
            }

            STATES.PROBLEM_INTERNET -> childFragmentManager.findFragmentByTag(
                getFragmentTag(internetProblemFragment)
            )?.let {
                internetProblemFragment = it as InternetProblemFragment
                hideMemDescription()
            }

            STATES.PROBLEM_SERVER_ERROR -> childFragmentManager.findFragmentByTag(
                getFragmentTag(serverProblemFragment)
            )?.let {
                serverProblemFragment = it as ServerProblemFragment
                hideMemDescription()
            }

            STATES.INIT -> getNewMem()

            else -> updateFragmentState()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemLaytoutBinding.inflate(inflater, container, false)
        val root = binding.root
        initState()
        imageDownloadProblemFragment.addListeners(this)
        Log.d("kek$categoryNumber", "onCreateView")
        return root
    }


    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("kek$categoryNumber", "onSaveInstanceState mem$categoryNumber")
        outState.putInt(toCategoryStr(CURRENT_IMAGE_NUMBER), currentImageNumber)
        outState.putInt(toCategoryStr(CURRENT_PAGE), currentPage)
        outState.putParcelableArrayList(toCategoryStr(CACHE), cache)
        outState.putSerializable(toCategoryStr(STATE), state)
        super.onSaveInstanceState(outState)
    }

    private fun registerNetworkCallback(context: Context) {
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build(),
            object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    activity?.runOnUiThread {
                        if(state == STATES.PROBLEM_INTERNET)
                            getNewMem()
                    }
                }
            })
    }

    private fun setMemDescriptionText(text: String) {
        binding.memDescription.text = text
    }

    private fun getCurrentMemDescription(): String = cache[currentImageNumber].description

    private fun getCurrentMemGifUrl(): String = cache[currentImageNumber].gifURL

    private fun updateMemDescriptionText() {
        Log.v("kek", "updateMemDescriptionText")
        setMemDescriptionText(getCurrentMemDescription())
        showMemDescription()
    }

    private fun updateMemImage() {
        Log.d("kek$categoryNumber", "updateMemImage")

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
                    Log.d("kek$categoryNumber", "onResourceReady")
                    hideAllProblemAndLoading()
                    resource?.start()
                    state = STATES.OK
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("kek$categoryNumber", "onLoadFailed")
                    showImageDownloadProblem()
                    return false
                }
            })
            .load(getCurrentMemGifUrl())
            .fitCenter()
            .into(binding.memImageView)

    }

    fun updateFragmentState() {
        Log.d("kek$categoryNumber", "updateFragmentState")
        updateMemDescriptionText()
        updateMemImage()
    }

    private fun decrementCurrentImageNumber() {
        if (currentImageNumber != 0) currentImageNumber--
    }


    private fun showMemDescription() {
        binding.memDescription.visibility = View.VISIBLE
    }

    private fun hideMemDescription() {
        Log.v("kek", "hideMemDescription")
        binding.memDescription.visibility = View.INVISIBLE
    }

    private fun hideMemDescriptionAndImage() {
        hideMemDescription()
        hideMemImage()
    }


    private fun hideMemImage() {
        binding.memImageView.setImageDrawable(null)
    }

    private fun hideAllProblemAndLoading() {
        hideLoading()
        hideAllProblems()
    }

    private fun hideAllProblems() {
        hideImageDownloadProblem()
        hideNoMemProblem()
        hideInternetProblem()
        hideServerErrorProblem()
    }

    private fun showLoading() {
        Log.d("kek$categoryNumber", "showLoading - ${!loadingFragment.isAdded}")

        if (!loadingFragment.isAdded) {
            hideAllProblems()
            addFragment(loadingFragment)

            state = STATES.LOADING
        }
    }

    private fun hideLoading() {
        Log.d("kek$categoryNumber", "hideLoading - ${loadingFragment.isAdded}")

        if (loadingFragment.isAdded)
            removeFragment(loadingFragment)
    }

    private fun showInternetProblem() {
        Log.d("kek$categoryNumber", "showInternetProblem - ${state != STATES.PROBLEM_INTERNET}")

        if (state != STATES.PROBLEM_INTERNET) {
            hideAllProblemAndLoading()
            hideMemDescriptionAndImage()
            addFragment(internetProblemFragment)

            state = STATES.PROBLEM_INTERNET
        }
    }

    private fun hideInternetProblem() {
        Log.d("kek$categoryNumber", "hideInternetProblem - ${state == STATES.PROBLEM_INTERNET}")

        if (state == STATES.PROBLEM_INTERNET)
            removeFragment(internetProblemFragment)
    }

    private fun showNoMemProblem() {
        Log.d("kek$categoryNumber", "showNoMemProblem - ${!noMemProblemFragment.isAdded}")

        if (!noMemProblemFragment.isAdded) {
            hideAllProblemAndLoading()

            hideMemDescriptionAndImage()
            addFragment(noMemProblemFragment)

            state = STATES.PROBLEM_NO_MEM
        }
    }

    private fun hideNoMemProblem() {
        Log.d("kek$categoryNumber", "hideNoMemProblem - ${noMemProblemFragment.isAdded}")

        if (noMemProblemFragment.isAdded)
            removeFragment(noMemProblemFragment)
    }

    private fun showServerErrorProblem() {
        Log.d("kek$categoryNumber", "showNoMemProblem - ${!serverProblemFragment.isAdded}")

        if (!serverProblemFragment.isAdded) {
            hideAllProblemAndLoading()
            hideMemDescriptionAndImage()
            addFragment(serverProblemFragment)

            state = STATES.PROBLEM_SERVER_ERROR
        }
    }

    private fun hideServerErrorProblem() {
        Log.d("kek$categoryNumber", "hideNoMemProblem - ${serverProblemFragment.isAdded}")

        if (serverProblemFragment.isAdded)
            removeFragment(serverProblemFragment)
    }

    private fun showImageDownloadProblem() {
        Log.d(
            "kek$categoryNumber",
            "showImageDownloadProblem - ${!imageDownloadProblemFragment.isAdded}"
        )

        if (!imageDownloadProblemFragment.isAdded) {
            hideAllProblemAndLoading()
            hideMemImage()
            addFragment(imageDownloadProblemFragment)

            state = STATES.PROBLEM_IMAGE_DOWNLOAD
        }
    }

    private fun hideImageDownloadProblem() {
        Log.d(
            "kek$categoryNumber",
            "hideImageDownloadProblem - ${imageDownloadProblemFragment.isAdded}"
        )

        if (imageDownloadProblemFragment.isAdded)
            removeFragment(imageDownloadProblemFragment)
    }

    private fun addFragment(fr: Fragment) {
        childFragmentManager.beginTransaction()
            .add(R.id.memFragment, fr, getFragmentTag(fr))
            .commitNow()
    }

    private fun removeFragment(fr: Fragment) {
        childFragmentManager.beginTransaction()
            .remove(fr).commitNow()
    }

    fun nextImage() {
        Log.d("kek$categoryNumber", "nextImage from $currentImageNumber")
        if (cache.size != 0 && cache.size != currentImageNumber) currentImageNumber++
        Log.d("kek$categoryNumber", "nextImage to $currentImageNumber")
        if (cache.size > currentImageNumber) {
            updateFragmentState()
        } else {
            if (category != CATEGORY.RANDOM && cache.size != 0) currentPage++
            getNewMem()
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

    private fun getNewMem() {
        Log.d("kek$categoryNumber", "getNewMemImage")
        hideMemDescription()
        showLoading()
        Log.d("kek$categoryNumber", "start download")
        downloader.getData(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("kek$currentImageNumber", "onFailure")
                activity?.runOnUiThread {
                    processInternetError()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("kek$categoryNumber", "onResponse")

                activity?.runOnUiThread {
                    if (response.isSuccessful && response.body != null) {
                        processResponse(JSONObject(response.body!!.string()))
                        if (cache.size > 0) updateFragmentState()
                        else processNoImagesErrorMeta()
                    } else processServerError()
                }
            }
        }, category, currentPage)
    }

    private fun processDownloadMetaError() {
        if (cache.size <= currentImageNumber) currentImageNumber = cache.size + 1
        decrementCurrentImageNumber()
        if (category != CATEGORY.RANDOM) if (currentPage != 0) currentPage--
    }

    private fun processInternetError() {
        processDownloadMetaError()
        showInternetProblem()
    }

    private fun processServerError() {
        processDownloadMetaError()
        showServerErrorProblem()
    }

    private fun processNoImagesErrorMeta() {
        Log.d("kek", "processNoImagesErrorMeta")
        showNoMemProblem()
        currentPage = 0
        decrementCurrentImageNumber()
    }

    fun processResponse(json: JSONObject) {
        if (category == CATEGORY.RANDOM) {
            cache.add(ImageMeta.jsonObjectToImageMeta(json))
        } else {
            val jsonArray = json.getJSONArray("result")
            for (i in 0 until jsonArray.length()) {
                cache.add(ImageMeta.jsonObjectToImageMeta(jsonArray.getJSONObject(i)))
            }
        }
    }

    private fun getFragmentTag(fr: Fragment) = "${fr.javaClass.simpleName}$currentPage"

    private fun toCategoryStr(str: String) = str + categoryNumber.toString()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun imageDownloadProblemClicked() {
        updateFragmentState()
    }
}