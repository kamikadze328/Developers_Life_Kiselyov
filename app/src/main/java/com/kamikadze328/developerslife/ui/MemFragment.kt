package com.kamikadze328.developerslife.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.kamikadze328.developerslife.App
import com.kamikadze328.developerslife.R
import com.kamikadze328.developerslife.data.*
import com.kamikadze328.developerslife.databinding.FragmentMemBinding
import com.kamikadze328.developerslife.ui.data.ImageDownloadProblemClickedListener
import com.kamikadze328.developerslife.ui.data.MemOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MemFragment : Fragment(), ImageDownloadProblemClickedListener, MemOptions {
    private var _binding: FragmentMemBinding? = null
    private val binding get() = _binding!!

    lateinit var category: Category

    private var internetProblemFragment = InternetProblemFragment()
    private var imageDownloadProblemFragment = ImageDownloadProblemFragment()
    private var noMemProblemFragment = NoMemProblemFragment()
    private var serverProblemFragment = ServerProblemFragment()

    private val downloader: Downloader get() = (requireActivity().application as App).downloader

    private var cache = ArrayList<ImageMeta>()
    private val currentMem: ImageMeta get() = cache[currentImageNumber]
    private val currentMemSafe: ImageMeta?
        get() = try {
            currentMem
        } catch (e: IndexOutOfBoundsException) {
            null
        }

    private var currentImageNumber = 0
    private var currentPage = 0

    private var state: State = State.INIT

    companion object {
        private const val ARG_CATEGORY = "section_category"

        private const val CACHE_KEY = "cache"
        private const val STATE_KEY = "state"
        private const val CURRENT_IMAGE_NUMBER_KEY = "current_image_number"
        private const val CURRENT_PAGE_KEY = "current_page"


        @JvmStatic
        fun newInstance(category: Category): MemFragment {
            return MemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                }
                this.category = category
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            category = (it.getSerializable(ARG_CATEGORY) ?: Category.RANDOM) as Category
        }

        if (savedInstanceState != null) {

            cache = savedInstanceState.getParcelableArrayList(toCategoryStr(CACHE_KEY)) ?: cache
            currentImageNumber = savedInstanceState.getInt(toCategoryStr(CURRENT_IMAGE_NUMBER_KEY))
            currentPage = savedInstanceState.getInt(toCategoryStr(CURRENT_PAGE_KEY))
            state = (savedInstanceState.getSerializable(toCategoryStr(STATE_KEY)) ?: state) as State
        }
    }


    private fun initState() {
        when (state) {
            State.LOADING -> {
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

    private fun setupRatingVisibility() {
        if (binding.memRating.visibility == View.VISIBLE && doHideRatings())
            binding.memRating.visibility = View.INVISIBLE
        else if (binding.memRating.visibility != View.VISIBLE && !doHideRatings() && state == State.OK)
            binding.memRating.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        setupRatingVisibility()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initState()
        registerNetworkCallback(requireContext())

        setupOpenLinkListener()
        imageDownloadProblemFragment.addListeners(this)
    }

    private fun setupOpenLinkListener() {
        binding.open.setOnClickListener {
            currentMemSafe?.let {
                val url = downloader.getMemUrl(it.id)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        }
        binding.open.setOnLongClickListener {
            currentMemSafe?.let {
                val url = downloader.getMemUrl(it.id)
                val clipboard =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("developerslive.ru", url)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(
                    requireContext(),
                    getString(R.string.text_copied, url),
                    Toast.LENGTH_LONG
                ).show()
            }
            true
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(toCategoryStr(CURRENT_IMAGE_NUMBER_KEY), currentImageNumber)
        outState.putInt(toCategoryStr(CURRENT_PAGE_KEY), currentPage)
        outState.putParcelableArrayList(toCategoryStr(CACHE_KEY), cache)
        outState.putSerializable(toCategoryStr(STATE_KEY), state)
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

    private fun isCurrentMemHasGif(): Boolean = currentMem.gifURL.isNotBlank()

    private fun updateMemDescriptionText() {
        binding.memDescription.text = currentMem.description
        showMemDescription()
    }

    private fun updateMemRatingText() {
        showMemRating()

        binding.memRating.text = currentMem.votes.toString()
        val color = ContextCompat.getColor(
            requireContext(),
            if (currentMem.votes < 0) R.color.orange else R.color.green
        )
        TextViewCompat.setCompoundDrawableTintList(binding.memRating, ColorStateList.valueOf(color))
    }

    private fun updateMemImageWithPreview() {
        showLoading()
        val loadedMemNumber = currentImageNumber
        Glide.with(this)
            .load(currentMem.previewURL)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(10)))
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    Log.d(
                        "kek",
                        "updateMemImageWithPreview resource ready($loadedMemNumber) id=${cache[loadedMemNumber].id}"
                    )
                    if (currentImageNumber != loadedMemNumber) return
                    if (isCurrentMemHasGif()) updateMemGifImage(resource)
                    else updateMemImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun updateMemImage(image: Drawable) {
        Glide.with(this).load(image).into(binding.memImageView)
    }

    private fun updateMemGifImage(placeholder: Drawable? = null) {
        if (placeholder == null) showLoading()
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
                    Log.d("kek", "${e?.toString()}")
                    e?.printStackTrace()
                    Log.d("kek", "${e?.stackTraceToString()}")

                    showImageDownloadProblem()
                    return false
                }
            })
            .load(currentMem.gifURL)
            .placeholder(placeholder)
            .fitCenter()
            .into(binding.memImageView)
    }

    fun updateFragmentState() {
        Log.d("kek", "updateFragmentState")
        Log.d("kek", "$currentImageNumber")
        Log.d("kek", "${cache.size}")

        updateMemDescriptionText()
        updateMemRatingText()
        updateMemImageWithPreview()
        showLink()
    }

    private fun decrementCurrentImageNumber() {
        if (currentImageNumber != 0) currentImageNumber--
    }

    private fun showMemDescription() {
        binding.memDescription.visibility = View.VISIBLE
    }

    private fun hideMemRating() {
        binding.memRating.visibility = View.INVISIBLE
    }

    private fun showMemRating() {
        binding.memRating.visibility = if (doHideRatings()) View.INVISIBLE else View.VISIBLE
    }

    private fun doHideRatings() = PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(resources.getString(R.string.hide_rating_key), false)

    private fun hideMemDescription() {
        binding.memDescription.visibility = View.INVISIBLE
    }

    private fun hideMemDescriptionAndImage() {
        hideMemDescription()
        hideMemImage()
        hideMemRating()
        hideLink()
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
        hideAllProblems()
        binding.loading.visibility = View.VISIBLE
        state = State.LOADING
    }

    private fun hideLoading() {
        binding.loading.visibility = View.GONE
    }

    private fun showLink() {
        binding.open.visibility = View.VISIBLE
    }

    private fun hideLink() {
        binding.open.visibility = View.GONE
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

    override fun next() {
        Log.d("kek", "next image click")
        if (cache.size != 0 && cache.size != currentImageNumber) currentImageNumber++
        if (cache.size > currentImageNumber) {
            updateFragmentState()
        } else {
            if (category != Category.RANDOM && cache.size != 0) currentPage++
            Log.d("kek", "new mem image")
            getNewMem()
        }
    }

    override fun prev() {
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
        Log.d("kek", "getNewMem")
        hideMemDescriptionAndImage()
        showLoading()
        downloader.getData(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    processInternetError()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful && response.body != null) {
                        val jsonObj = JSONObject(response.body!!.string())
                        if (!processResponse(jsonObj)) {
                            processInternetError()
                            return@runOnUiThread
                        }
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

    fun processResponse(json: JSONObject): Boolean {
        Log.d("kek", "processResponse")
        if (category == Category.RANDOM) {
            Log.d("kek", "${ImageMeta.jsonObjectToImageMeta(json)}")
            val imageMeta = ImageMeta.jsonObjectToImageMeta(json)
            if (imageMeta.gifURL.isEmpty()) return false
            return cache.add(imageMeta)
        } else {
            val jsonArray = json.getJSONArray("result")
            for (i in 0 until jsonArray.length()) {
                cache.add(ImageMeta.jsonObjectToImageMeta(jsonArray.getJSONObject(i)))
            }
            return true
        }
    }

    private fun showSharingProblem(isTooEarly: Boolean = false) {
        val textId =
            if (isTooEarly) R.string.is_too_early_sharing_problem_description
            else R.string.no_images_problem_description
        val toast = Toast.makeText(
            requireContext(),
            resources.getText(textId),
            Toast.LENGTH_SHORT
        )
        activity?.runOnUiThread {
            toast.show()
        }
    }

    override fun share() {
        val curMem = currentMemSafe
        if (curMem == null)
            showSharingProblem()
        else {
            Glide.with(this)
                .asGif()
                .onlyRetrieveFromCache(true)
                .addListener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        showSharingProblem(true)
                        return false
                    }

                    override fun onResourceReady(
                        resource: GifDrawable?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        var isOk = false
                        resource?.let { gif ->
                            saveGif(gif, requireContext())?.let { uri ->
                                shareCurrentGif(uri, curMem.description)
                                isOk = true
                            }
                        }
                        if (!isOk) showSharingProblem()
                        return false
                    }
                })
                .load(curMem.gifURL)
                .submit()
        }
    }

    private fun shareCurrentGif(uri: Uri, description: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/gif"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, description)
        }

        startActivity(Intent.createChooser(shareIntent, null))
    }


    private fun getFragmentTag(fr: Fragment) = "${fr.javaClass.simpleName}$currentPage"

    private fun toCategoryStr(str: String) = str + category.id.toString()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun imageDownloadProblemClicked() {
        updateFragmentState()
    }

    fun clearHistory() {
        Log.d("kek", "clearHistory - added($isAdded")
        if (!isAdded) return
        if (category == Category.RANDOM && cache.isNotEmpty()) {
            val last = cache.last()
            cache.clear()
            cache.add(last)
            currentImageNumber = 0
        } else {
            currentImageNumber = 0
            cache.clear()
            getNewMem()
        }
    }


}