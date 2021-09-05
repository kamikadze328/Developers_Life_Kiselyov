package com.kamikadze328.developerslife.ui

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
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
import com.kamikadze328.developerslife.App
import com.kamikadze328.developerslife.R
import com.kamikadze328.developerslife.data.Category
import com.kamikadze328.developerslife.data.ImageMeta
import com.kamikadze328.developerslife.data.State
import com.kamikadze328.developerslife.databinding.FragmentMemBinding
import com.kamikadze328.developerslife.ui.data.ImageDownloadProblemClickedListener
import com.kamikadze328.developerslife.ui.fragments.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MemFragment : Fragment(), ImageDownloadProblemClickedListener {
    private var _binding: FragmentMemBinding? = null
    private val binding get() = _binding!!

    private var category: Category? = null
    var categoryNumber = 0

    private var internetProblemFragment = InternetProblemFragment()
    private var imageDownloadProblemFragment = ImageDownloadProblemFragment()
    private var loadingFragment = LoadingFragment()
    private var noMemProblemFragment = NoMemProblemFragment()
    private var serverProblemFragment = ServerProblemFragment()


    private var cache = ArrayList<ImageMeta>()
    private var currentImageNumber = 0
    private var currentPage = 0

    private var state: State = State.INIT

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val ARG_SECTION_TITLE = "section_title"

        private const val CACHE = "cache"
        private const val STATE = "state"
        private const val CURRENT_IMAGE_NUMBER = "current_image_number"
        private const val CURRENT_PAGE = "current_page"


        @JvmStatic
        fun newInstance(sectionNumber: Int, sectionTitle: String): MemFragment {
            return MemFragment().apply {
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
        category = arguments?.getString(ARG_SECTION_TITLE)?.let {
            Category.fromString(it) ?: category
        } ?: Category.RANDOM

        if (savedInstanceState != null) {
            cache = savedInstanceState.getParcelableArrayList(toCategoryStr(CACHE)) ?: cache
            currentImageNumber = savedInstanceState.getInt(toCategoryStr(CURRENT_IMAGE_NUMBER))
            currentPage = savedInstanceState.getInt(toCategoryStr(CURRENT_PAGE))
            state = (savedInstanceState.getSerializable(toCategoryStr(STATE)) ?: state) as State
        }
    }


    private fun initState() {
        when (state) {
            State.LOADING -> childFragmentManager.findFragmentByTag(
                getFragmentTag(loadingFragment)
            )?.let {
                loadingFragment = it as LoadingFragment
                if (currentImageNumber < cache.size) updateFragmentState()
                else getNewMem()
            }

            State.PROBLEM_IMAGE_DOWNLOAD -> childFragmentManager.findFragmentByTag(
                getFragmentTag(imageDownloadProblemFragment)
            )?.let {
                imageDownloadProblemFragment = it as ImageDownloadProblemFragment
                updateMemDescriptionText()
            }

            State.PROBLEM_NO_MEM -> childFragmentManager.findFragmentByTag(
                getFragmentTag(noMemProblemFragment)
            )?.let {
                noMemProblemFragment = it as NoMemProblemFragment
                hideMemDescription()
            }

            State.PROBLEM_INTERNET -> childFragmentManager.findFragmentByTag(
                getFragmentTag(internetProblemFragment)
            )?.let {
                internetProblemFragment = it as InternetProblemFragment
                hideMemDescription()
            }

            State.PROBLEM_SERVER_ERROR -> childFragmentManager.findFragmentByTag(
                getFragmentTag(serverProblemFragment)
            )?.let {
                serverProblemFragment = it as ServerProblemFragment
                hideMemDescription()
            }

            State.INIT -> getNewMem()

            else -> updateFragmentState()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initState()
        registerNetworkCallback(requireContext())
        imageDownloadProblemFragment.addListeners(this)
    }


    override fun onSaveInstanceState(outState: Bundle) {
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
                        onNetworkAvailable()
                    }
                }
            })
    }

    private fun onNetworkAvailable() {
        if (state == State.PROBLEM_INTERNET)
            getNewMem()
    }

    private fun setMemDescriptionText(text: String) {
        binding.memDescription.text = text
    }

    private fun getCurrentMemDescription(): String = cache[currentImageNumber].description

    private fun getCurrentMemGifUrl(): String = cache[currentImageNumber].gifURL

    private fun updateMemDescriptionText() {
        setMemDescriptionText(getCurrentMemDescription())
        showMemDescription()
    }

    private fun updateMemImage() {
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
                    hideAllProblemAndLoading()
                    resource?.start()
                    state = State.OK
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    showImageDownloadProblem()
                    return false
                }
            })
            .load(getCurrentMemGifUrl())
            .fitCenter()
            .into(binding.memImageView)

    }

    fun updateFragmentState() {
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
        if (!loadingFragment.isAdded) {
            hideAllProblems()
            addFragment(loadingFragment)

            state = State.LOADING
        }
    }

    private fun hideLoading() {
        if (loadingFragment.isAdded)
            removeFragment(loadingFragment)
    }

    private fun showInternetProblem() {
        if (state != State.PROBLEM_INTERNET) {
            hideAllProblemAndLoading()
            hideMemDescriptionAndImage()
            addFragment(internetProblemFragment)

            state = State.PROBLEM_INTERNET
        }
    }

    private fun hideInternetProblem() {
        if (state == State.PROBLEM_INTERNET)
            removeFragment(internetProblemFragment)
    }

    private fun showNoMemProblem() {
        if (!noMemProblemFragment.isAdded) {
            hideAllProblemAndLoading()

            hideMemDescriptionAndImage()
            addFragment(noMemProblemFragment)

            state = State.PROBLEM_NO_MEM
        }
    }

    private fun hideNoMemProblem() {
        if (noMemProblemFragment.isAdded)
            removeFragment(noMemProblemFragment)
    }

    private fun showServerErrorProblem() {
        if (!serverProblemFragment.isAdded) {
            hideAllProblemAndLoading()
            hideMemDescriptionAndImage()
            addFragment(serverProblemFragment)

            state = State.PROBLEM_SERVER_ERROR
        }
    }

    private fun hideServerErrorProblem() {
        if (serverProblemFragment.isAdded)
            removeFragment(serverProblemFragment)
    }

    private fun showImageDownloadProblem() {
        if (!imageDownloadProblemFragment.isAdded) {
            hideAllProblemAndLoading()
            hideMemImage()
            addFragment(imageDownloadProblemFragment)

            state = State.PROBLEM_IMAGE_DOWNLOAD
        }
    }

    private fun hideImageDownloadProblem() {
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
        if (cache.size != 0 && cache.size != currentImageNumber) currentImageNumber++
        if (cache.size > currentImageNumber) {
            updateFragmentState()
        } else {
            if (category != Category.RANDOM && cache.size != 0) currentPage++
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
        hideMemDescription()
        showLoading()
        (requireActivity().application as App).downloader.getData(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    processInternetError()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful && response.body != null) {
                        processResponse(JSONObject(response.body!!.string()))
                        if (cache.size > 0) updateFragmentState()
                        else processNoImagesErrorMeta()
                    } else processServerError()
                }
            }
        }, category ?: Category.RANDOM, currentPage)
    }

    private fun processDownloadMetaError() {
        if (cache.size <= currentImageNumber) currentImageNumber = cache.size + 1
        decrementCurrentImageNumber()
        if (category != Category.RANDOM) if (currentPage != 0) currentPage--
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
        showNoMemProblem()
        currentPage = 0
        decrementCurrentImageNumber()
    }

    fun processResponse(json: JSONObject) {
        if (category == Category.RANDOM) {
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