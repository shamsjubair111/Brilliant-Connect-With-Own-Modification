package sdk.chat.app.xmpp.telco

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import sdk.chat.core.Tab
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.BaseInterfaceAdapter
import sdk.chat.ui.ChatSDKUI

class BrilliantInterfaceAdapter: BaseInterfaceAdapter() {

    val callsFragment = BrilliantCallsFragment()
    val settingsFragment = BrilliantSettingsFragment()

    val bptf = BrilliantPrivateThreadsFragment()

    override fun privateThreadsFragment(): Fragment? {
        return bptf
    }

    override fun defaultTabs(): List<Tab>? {
        val tabs = ArrayList<Tab>()

        val ctx = ChatSDK.ctx()
        val chatsTab = Tab(
            ChatSDK.getString(R.string.chats),
            ChatSDKUI.icons()[ctx, ContextCompat.getDrawable(ctx, R.drawable.ic_chats), ChatSDKUI.icons().tabIconColor],
            privateThreadsFragment()
        )

//        val callsTab = Tab(
//            ChatSDK.getString(R.string.call),
//            ChatSDKUI.icons()[ChatSDK.ctx(), ChatSDKUI.icons().call, ChatSDKUI.icons().tabIconColor],
//            callsFragment
//        )

        val callsTab = Tab(
            ChatSDK.getString(R.string.call),
            ChatSDKUI.icons()[ctx, ContextCompat.getDrawable(ctx, R.drawable.ic_calls), ChatSDKUI.icons().tabIconColor],
            callsFragment
        )

        val contactsTab = Tab(
            ChatSDK.getString(R.string.contacts),
            ChatSDKUI.icons()[ctx, ContextCompat.getDrawable(ctx, R.drawable.ic_contacts), ChatSDKUI.icons().tabIconColor],
            contactsFragment
        )

        val settingsTab = Tab(
            ChatSDK.getString(R.string.settings),
            ChatSDKUI.icons()[ctx, ContextCompat.getDrawable(ctx, R.drawable.ic_settings), ChatSDKUI.icons().tabIconColor],
            settingsFragment
        )

        tabs.add(callsTab)
        tabs.add(chatsTab)
        tabs.add(contactsTab)
        tabs.add(settingsTab)
        return tabs
    }

//    val chatsTab = Tab(
//        ChatSDK.getString(R.string.chats),
//        ChatSDKUI.icons()[ctx, ContextCompat.getDrawable(ctx, R.drawable.ic_chat), ChatSDKUI.icons().tabIconColor],
//        privateThreadsFragment()
//    )
//
//
//    val settingsTab = Tab(
//        ChatSDK.getString(R.string.settings),
//        ChatSDKUI.icons()[ctx, ContextCompat.getDrawable(ctx, R.drawable.ic_chat), ChatSDKUI.icons().tabIconColor],
//        settingsFragment
//    )

}