package ywq.ares.dreamcatcher.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class TabAdapter(fm:FragmentManager,var fragments:ArrayList<Fragment>,var titles:ArrayList<String>) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {

        return fragments[position]
    }

    override fun getItemPosition(`object`: Any): Int {

        return PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    override fun getCount(): Int {

        return fragments.size
     }
}