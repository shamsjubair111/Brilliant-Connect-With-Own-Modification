package sdk.chat.app.xmpp.telco

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.fragments.BaseFragment
data class SettingsItem(val settingsMenu: String, val settingsIcon: Int)
class BrilliantSettingsFragment: BaseFragment() {
    private lateinit var recyclerViewSettings: RecyclerView
    private lateinit var adapter: SettingsAdapter
    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_settings
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun initViews() {
        val items: MutableList<SettingsItem> = ArrayList()

        items.add(SettingsItem("Help", R.drawable.icon_help))
        items.add(SettingsItem("My Balance", R.drawable.icon_my_balance))
        items.add(SettingsItem("Add Balance", R.drawable.icon_my_balance))
        items.add(SettingsItem("Paid Calls and Credit history", R.drawable.icon_paid_calls))
        items.add(SettingsItem("Personal Messages", R.drawable.icon_personal_message))
        items.add(SettingsItem("Extended Services", R.drawable.icon_extended_services))
        items.add(SettingsItem("Stickers", R.drawable.icon_stickers))
        items.add(SettingsItem("Invite Friends", R.drawable.icon_invite_friends))
        items.add(SettingsItem("NID", R.drawable.icon_nid))
        items.add(SettingsItem("Brilliant Connect Number", R.drawable.icon_brilliant_number))
        items.add(SettingsItem("Notification", R.drawable.icon_notifications))
        items.add(SettingsItem("Chat Settings", R.drawable.icon_chatsettings))
        items.add(SettingsItem("Data Storage Settings", R.drawable.icon_data_storage))
        items.add(SettingsItem("Low Data Usage", R.drawable.icon_low_data))
        items.add(SettingsItem("Privacy", R.drawable.icon_privacy))
        items.add(SettingsItem("Brilliant Language", R.drawable.icon_brilliant_language))
        items.add(SettingsItem("Why Brilliant Connect?", R.drawable.icon_why_brilliant))
        items.add(SettingsItem("Version 2.4.6", R.drawable.icon_version))


        recyclerViewSettings = requireView().findViewById(R.id.recyclerView)
        adapter = SettingsAdapter(requireContext(), items)
        recyclerViewSettings.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        recyclerViewSettings.adapter = adapter
    }

    override fun clearData() {

    }

    override fun reloadData() {

    }
}