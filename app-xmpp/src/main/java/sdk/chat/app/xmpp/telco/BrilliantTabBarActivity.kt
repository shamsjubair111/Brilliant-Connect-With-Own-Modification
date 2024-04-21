package sdk.chat.app.xmpp.telco

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.Single
import sdk.chat.core.events.EventType
import sdk.chat.core.events.NetworkEvent
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.activities.MainAppBarActivity
import sdk.chat.ui.adapters.PagerAdapterTabs
import sdk.guru.common.DisposableMap
import sdk.guru.common.RX
import kotlin.math.max
import kotlin.math.min


class BrilliantTabBarActivity: MainAppBarActivity() {

    var unreadMessagesCount = 0
    var missedCallsCount = 0

    var unreadDisposableMap = DisposableMap()
    var selectedTab: TabLayout.Tab? = null
    override fun getLayout(): Int {
        return R.layout.activity_brilliant_main_tabs
    }

    fun addListeners() {
        dm.add(ChatSDK.events().sourceOnSingle()
            .filter(
                NetworkEvent.filterType(
                    EventType.MessageUpdated,
                    EventType.ThreadRead,
                    EventType.MessageAdded,
                    EventType.MessageRemoved
                )
            )
            .subscribe { _: NetworkEvent? ->
                updateUnreadMessages()
            })
    }

    override fun onStart() {
        super.onStart()
        addListeners()
        updateUnreadMessages()
        updateMissedCalls()
    }

    override fun onStop() {
        super.onStop()
        removeListeners()
    }
    private fun removeListeners() {
        dm.dispose()
    }

    fun updateMissedCalls() {
        // Calculate missed calls here
        missedCallsCount = 0
        updateTabs()
    }

    fun updateUnreadMessages() {
        unreadDisposableMap.dispose()
        unreadDisposableMap.add(ChatSDK.db().allThreadsAsync().flatMap { it ->
            var count = 0
            it.forEach { thread ->
                count += thread.unreadMessagesCount
            }
            Single.just(count)
        }.observeOn(RX.main()).subscribe { result ->

            //            unreadMessages?.text = "$result"
//            unreadMessages?.visibility = if(result > 0) VISIBLE else INVISIBLE
            unreadMessagesCount = result
            updateTabs()
        })
    }

    override fun doOnStart() {
        if (Brilliant.shared().debug) {
            if (supportActionBar != null) {
                supportActionBar!!.setHomeAsUpIndicator(ChatSDKUI.icons()[this, ChatSDKUI.icons().user, ChatSDKUI.icons().actionBarIconColor])
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            }
//            super.doOnStart()
        }
    }


    override fun initViews() {
        super.initViews()

        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        content = findViewById(R.id.content)
        searchView = findViewById(R.id.searchView)
        root = findViewById(R.id.root)
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        // Only creates the adapter if it wasn't initiated already
        if (adapter == null) {
            adapter = PagerAdapterTabs(this)
        }
        val tabs = adapter.tabs

        viewPager.adapter = adapter
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.setCustomView(R.layout.view_brilliant_tab).customView?.let {
                val titleView = it.findViewById<TextView>(R.id.tvTitle)
                val title = tabs[position].title
//                titleView?.text = title
                titleView?.text = if(position == 0) title else ""
//                titleView?.visibility = if(position == 0) VISIBLE else GONE

                val unread = it.findViewById<TextView>(R.id.dialogUnreadBubble)

                if (!listOf(getString(R.string.chats), getString(R.string.call)).contains(title)) {
                    unread?.visibility = GONE
                }

                val imageView = it.findViewById<ImageView>(R.id.imageView)
                imageView?.setImageDrawable(tabs[position].icon)
                imageView?.visibility = if(position == 0) GONE else VISIBLE

            }
        }.attach()

        tabLayout.post {
            tabLayout.getTabAt(0)?.let {
                updateTabWidth(it)
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab
                tabSelected(tab)
                updateTabForSelected(tab)
                tab.view.post {
                    updateTabWidth(tab)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        viewPager.offscreenPageLimit = 3
        val tab = tabLayout.getTabAt(0)
        tab?.let { tabSelected(it) }
    }

    fun updateTabForSelected(selected: TabLayout.Tab) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.let {
                val titleView = it.customView?.findViewById<TextView>(R.id.tvTitle)
                val imageView = it.customView?.findViewById<ImageView>(R.id.imageView)

                titleView?.text = if(selected == tab) adapter.tabs[i].title else ""
//                titleView?.visibility = if(selected == tab) VISIBLE else GONE
                imageView?.visibility = if(selected == tab) GONE else VISIBLE

                val lp = tab.view.layoutParams
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
                it.view.layoutParams = lp
            }
        }
    }

    fun getScreenWidth(context: Context): Int {
        val orientation = context.resources.configuration.orientation
        val metrics = Resources.getSystem().displayMetrics
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            max(metrics.heightPixels, metrics.widthPixels)
        } else {
            min(metrics.heightPixels, metrics.widthPixels)
        }
    }

    fun updateTabWidth(selected: TabLayout.Tab) {
//        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenWidth = getScreenWidth(this)
        var availableWidth = screenWidth
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.let {
                if (tab != selected) {
                    availableWidth -= it.view.width
                }
                println("Tab $i width: ${it.view.width}")
            }
        }
        val lp = selected.view.layoutParams
        lp.width = availableWidth
        selected.view.layoutParams = lp
        selected.view.post {
            println("Tab lp: ${lp.width}")
            println("Tab selected width: ${selected.view.width}")
        }
    }

    fun updateTabs() {
        val chatsIndex = chatsTabIndex()
        val callsIndex = callTabIndex()
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.let {
                val titleView = it.customView?.findViewById<TextView>(R.id.tvTitle)
                val imageView = it.customView?.findViewById<ImageView>(R.id.imageView)
                val unread = it.customView?.findViewById<TextView>(R.id.dialogUnreadBubble)

                if (chatsIndex == i) {
                    unread?.text = "$unreadMessagesCount"
                    unread?.visibility = if(unreadMessagesCount > 0) VISIBLE else GONE
                }
                if (callsIndex == i) {
                    unread?.text = "$missedCallsCount"
                    unread?.visibility = if(missedCallsCount > 0) VISIBLE else GONE
                }

            }
        }
        selectedTab?.let {
            updateTabWidth(it)
        }
//        updateTabWidth()
    }

    fun chatsTabIndex(): Int? {
        val tabs = ChatSDK.ui().tabs()
        for (index in 0 until tabs.size) {
            if (tabs[index].title == getString(R.string.chats)) {
                return index
            }
        }
        return null
    }

    fun callTabIndex(): Int? {
        val tabs = ChatSDK.ui().tabs()
        for (index in 0 until tabs.size) {
            if (tabs[index].title == getString(R.string.call)) {
                return index
            }
        }
        return null
    }

}
