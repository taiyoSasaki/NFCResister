package jp.co.ods.nfcresister

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    val titleIds = listOf(R.string.tab_title_A, R.string.tab_title_B, R.string.tab_title_C)

    val fragments = listOf(NfcCardAFragment(), NfcCardBFragment(), NfcCardCFragment())

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}