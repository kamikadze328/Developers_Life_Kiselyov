package com.kamikadze328.developerslife

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kamikadze328.developerslife.adapter.ScreenSlidePagerAdapter
import com.kamikadze328.developerslife.data.Category
import com.kamikadze328.developerslife.databinding.ActivityMainBinding
import com.kamikadze328.developerslife.ui.AboutActivity
import com.kamikadze328.developerslife.ui.MemFragment
import com.kamikadze328.developerslife.ui.SettingsActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sectionsPagerAdapter: ScreenSlidePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )

            WindowInsetsCompat.CONSUMED
        }

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
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }

            R.id.clear_mem_history -> {
                sectionsPagerAdapter.fragments.forEach {
                    it?.clearHistory()
                }
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
            tab.text = "${Category.entries[(position)]}"
        }.attach()


        //for activity recreate
        sectionsPagerAdapter.fragments.clear()
        supportFragmentManager.fragments.forEach {
            if (it is MemFragment) {
                sectionsPagerAdapter.fragments.add(it.category.id, it)
            }
        }
    }

    private fun setupButtons() {
        binding.nextGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[binding.viewPager.currentItem]?.next()
        }
        binding.prevGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[binding.viewPager.currentItem]?.prev()
        }
        binding.shareGifButton.setOnClickListener {
            sectionsPagerAdapter.fragments[binding.viewPager.currentItem]?.share()
        }
    }
}