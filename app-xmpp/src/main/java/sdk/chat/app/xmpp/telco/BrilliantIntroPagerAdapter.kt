package sdk.chat.app.xmpp.telco

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import sdk.chat.demo.xmpp.R

class BrilliantIntroPagerAdapter: FragmentStateAdapter {

    public val fragments: MutableList<Fragment> = ArrayList()

    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle) : super(fragmentManager, lifecycle) {

        // Make the fragments
        fragments.add(BrilliantIntroFragment.newInstance(R.string.intro_1_title, R.string.intro_1_subtitle, R.string.intro_1_text, R.drawable.rnd_brilliant_connect))
        fragments.add(BrilliantIntroFragment.newInstance(R.string.intro_2_title, R.string.intro_2_subtitle, R.string.intro_2_text, R.drawable.rnd_wave))
        fragments.add(BrilliantIntroFragment.newInstance(R.string.intro_3_title, R.string.intro_3_subtitle, R.string.intro_3_text, R.drawable.rnd_dial))
        fragments.add(BrilliantIntroFragment.newInstance(R.string.intro_4_title, R.string.intro_4_subtitle, R.string.intro_4_text, R.drawable.rnd_timer))
        fragments.add(BrilliantIntroFragment.newInstance(R.string.intro_5_title, R.string.intro_5_subtitle, R.string.intro_5_text, R.drawable.rnd_dl))

    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getItemPosition(item: Any?): Int {
        val index = fragments.indexOf(item)
        return if (index >= 0 && index < 3) {
            PagerAdapter.POSITION_UNCHANGED
        } else PagerAdapter.POSITION_NONE
    }

    fun setFragments(fragments: List<Fragment>?) {
        this.fragments.clear()
        this.fragments.addAll(fragments!!)
    }


    fun get(): List<Fragment>? {
        return fragments
    }

}