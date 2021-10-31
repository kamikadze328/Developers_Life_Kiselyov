package com.kamikadze328.developerslife

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kamikadze328.developerslife.adapter.ScreenSlidePagerAdapter
import com.kamikadze328.developerslife.data.Category
import com.kamikadze328.developerslife.databinding.ActivityMainBinding
import com.kamikadze328.developerslife.ui.MemFragment
import com.kamikadze328.developerslife.ui.SettingsActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sectionsPagerAdapter: ScreenSlidePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupPagerAdapter()
        setupButtons()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.about -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupPagerAdapter() {
        sectionsPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager, lifecycle, this)
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
    }

    fun setupButtons() {

        binding.nextGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[binding.viewPager.currentItem]!!.nextImage()
        }
        binding.prevGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[binding.viewPager.currentItem]!!.prevImage()
        }
    }
}