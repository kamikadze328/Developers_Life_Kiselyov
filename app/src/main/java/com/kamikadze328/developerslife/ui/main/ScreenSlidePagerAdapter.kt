package com.kamikadze328.developerslife.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kamikadze328.developerslife.additional.CATEGORY

class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    val fragments: MutableList<MemLayoutFragment> = mutableListOf()
    override fun getItemCount(): Int = CATEGORY.values().size

    override fun createFragment(position: Int): Fragment {
        val f = MemLayoutFragment.newInstance(position + 1, CATEGORY.values()[position].category)
        fragments.add(f)
        return f
    }
}
