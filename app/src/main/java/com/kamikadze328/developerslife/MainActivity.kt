package com.kamikadze328.developerslife

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kamikadze328.developerslife.data.Category
import com.kamikadze328.developerslife.databinding.ActivityMainBinding
import com.kamikadze328.developerslife.ui.MemFragment
import com.kamikadze328.developerslife.adapter.ScreenSlidePagerAdapter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager, lifecycle, this)
        val viewPager: ViewPager2 = binding.viewPager
        val tabs: TabLayout = binding.tabs

        viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = "${Category.values()[(position)]}"
        }.attach()

        supportFragmentManager.fragments.forEach {
            if (it is MemFragment) {
                sectionsPagerAdapter.fragments.add(it.categoryNumber - 1, it)
            }
        }
        binding.nextGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[viewPager.currentItem]!!.nextImage()
        }
        binding.prevGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[viewPager.currentItem]!!.prevImage()
        }
    }

}