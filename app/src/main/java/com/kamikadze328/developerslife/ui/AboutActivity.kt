package com.kamikadze328.developerslife.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import com.kamikadze328.developerslife.BuildConfig
import com.kamikadze328.developerslife.R
import com.kamikadze328.developerslife.databinding.AboutActivityBinding

class AboutActivity : MenuActivity(R.layout.about_activity) {

    private lateinit var binding: AboutActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AboutActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.developerslifeRef.movementMethod = LinkMovementMethod.getInstance()
        binding.privacyPolicyRef.movementMethod = LinkMovementMethod.getInstance()
        binding.githubRef.movementMethod = LinkMovementMethod.getInstance()

        binding.appVersion.text = resources.getString(R.string.app_version, BuildConfig.VERSION_NAME)
    }
}