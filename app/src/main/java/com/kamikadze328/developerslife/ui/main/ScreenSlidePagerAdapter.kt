package com.kamikadze328.developerslife.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kamikadze328.developerslife.data.Category

class ScreenSlidePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val context: Context
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    val fragments: MutableList<MemFragment?> = MutableList(itemCount) { null }
    override fun getItemCount(): Int = Category.values().size

    override fun createFragment(position: Int): Fragment {
        val f = MemFragment.newInstance(
            position + 1,
            context.resources.getString(Category.values()[position].resourceId)
        )
        fragments.add(position, f)
        return f
    }
}
