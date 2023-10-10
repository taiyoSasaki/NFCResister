package jp.co.ods.nfcresister

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    private var fragments : ArrayList<NfcCardFragment> = arrayListOf()

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun addFragment(fragment: NfcCardFragment) {
        fragments.add(fragment)
    }

    fun clearFragment() {
        fragments.clear()
    }

}