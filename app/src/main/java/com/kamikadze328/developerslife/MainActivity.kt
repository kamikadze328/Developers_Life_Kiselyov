package com.kamikadze328.developerslife

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kamikadze328.developerslife.additional.CATEGORY
import com.kamikadze328.developerslife.databinding.ActivityMainBinding
import com.kamikadze328.developerslife.ui.main.ScreenSlidePagerAdapter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = ScreenSlidePagerAdapter(this)
        val viewPager: ViewPager2 = binding.viewPager
        val tabs: TabLayout = binding.tabs

        viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = "${CATEGORY.values()[(position)]}"
        }.attach()


        binding.nextGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[viewPager.currentItem].nextImage()
        }
        binding.prevGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[viewPager.currentItem].prevImage()
        }
    }
}