package com.kamikadze328.developerslife.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kamikadze328.developerslife.data.Category
import com.kamikadze328.developerslife.ui.MemFragment

class ScreenSlidePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val context: Context
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    val fragments: MutableList<MemFragment?> = MutableList(itemCount) { null }
    override fun getItemCount(): Int = Category.entries.size

    override fun createFragment(position: Int): Fragment {
        val f = MemFragment.newInstance(Category.byId(position))
        fragments.add(position, f)
        return f
    }
}
