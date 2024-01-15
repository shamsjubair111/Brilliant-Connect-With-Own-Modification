package sdk.chat.app.xmpp.telco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.fragments.PrivateThreadsFragment
import sdk.chat.ui.provider.MenuItemProvider

class BrilliantPrivateThreadsFragment: PrivateThreadsFragment() {

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_threads
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  super.onCreateView(inflater, container, savedInstanceState)

        view?.findViewById<ImageView>(R.id.fab)?.setOnClickListener {
            ChatSDK.ui().startCreateThreadActivity(context)
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.removeItem(MenuItemProvider.addItemId)
    }

//    fun onCreateView(
//        inflater: LayoutInflater?,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = super.onCreateView(inflater!!, container, savedInstanceState)
//
//        view?.findViewById<ImageView>(R.id.fab)?.setOnClickListener {
//            ChatSDK.ui().startCreateThreadActivity(context)
//        }
//
//        return view
//    }
//
//    fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
//        super.onCreateOptionsMenu(menu, inflater!!)
//        menu.removeItem(MenuItemProvider.addItemId)
//    }

}