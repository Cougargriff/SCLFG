package org.griffin.sclfg.View.Home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class PageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    var mfm = fm
    var mFragmentItems: ArrayList<Fragment> = ArrayList()
    var mFragmentTitles: ArrayList<String> = ArrayList()

    fun addFragments(fragmentItem: Fragment, fragmentTitle: String) {
        mFragmentItems.add(fragmentItem)
        mFragmentTitles.add(fragmentTitle)
    }

    override fun getCount(): Int {
        return mFragmentItems.size
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentItems[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitles[position]
    }

}