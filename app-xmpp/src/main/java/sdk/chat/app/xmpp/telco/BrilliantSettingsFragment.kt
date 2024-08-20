package sdk.chat.app.xmpp.telco

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.fragments.BaseFragment

data class SettingsItem(val settingsMenu: String, val settingsIcon: Int)

class BrilliantSettingsFragment : BaseFragment() {
    private lateinit var recyclerViewSettings: RecyclerView
    private lateinit var adapter: SettingsAdapter

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_settings
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brilliant_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initViews()
        } catch (e: Exception) {
            Log.e("BrilliantSettingsFragment", "Error initializing views", e)
        }
    }

    private fun getSettingsItems(): List<SettingsItem> {
        return listOf(
            SettingsItem("Help", R.drawable.icon_help),
            SettingsItem("My Balance", R.drawable.icon_my_balance),
            SettingsItem("Add Balance", R.drawable.icon_my_balance),
            SettingsItem("Paid Calls and Credit history", R.drawable.icon_paid_calls),
            SettingsItem("Personal Messages", R.drawable.icon_personal_message),
            SettingsItem("Extended Services", R.drawable.icon_extended_services),
            SettingsItem("Stickers", R.drawable.icon_stickers),
            SettingsItem("Invite Friends", R.drawable.icon_invite_friends),
            SettingsItem("NID", R.drawable.icon_nid),
            SettingsItem("Brilliant Connect Number", R.drawable.icon_brilliant_number),
            SettingsItem("Notification", R.drawable.icon_notifications),
            SettingsItem("Chat Settings", R.drawable.icon_chatsettings),
            SettingsItem("Data Storage Settings", R.drawable.icon_data_storage),
            SettingsItem("Low Data Usage", R.drawable.icon_low_data),
            SettingsItem("Privacy", R.drawable.icon_privacy),
            SettingsItem("Brilliant Language", R.drawable.icon_brilliant_language),
            SettingsItem("Why Brilliant Connect?", R.drawable.icon_why_brilliant),
            SettingsItem("Version 2.4.6", R.drawable.icon_version)
        )
    }

    override fun initViews() {
        try {
            val items = getSettingsItems()

            recyclerViewSettings = requireView().findViewById(R.id.recyclerView)
            adapter = SettingsAdapter(requireContext(), items)
            recyclerViewSettings.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewSettings.adapter = adapter

            Log.d("BrilliantSettingsFragment", "RecyclerView and Adapter initialized successfully")
        } catch (e: Exception) {
            Log.e("BrilliantSettingsFragment", "Error initializing views", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("BrilliantSettingsFragment", "Fragment resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d("BrilliantSettingsFragment", "Fragment paused")
    }

    override fun onStop() {
        super.onStop()
        Log.d("BrilliantSettingsFragment", "Fragment stopped")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewSettings.adapter = null
        Log.d("BrilliantSettingsFragment", "View destroyed")
    }

    override fun clearData() {
    }

    override fun reloadData() {
    }
}
