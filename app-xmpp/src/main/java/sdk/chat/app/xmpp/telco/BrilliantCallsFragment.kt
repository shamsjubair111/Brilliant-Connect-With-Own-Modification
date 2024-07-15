package sdk.chat.app.xmpp.telco

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.annotation.RequiresApi
import com.codewithkael.webrtcprojectforrecord.CallRecords
import com.codewithkael.webrtcprojectforrecord.SQLiteCallFragmentHelper
import com.lassi.common.utils.Logger
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.fragments.BaseFragment
import sdk.chat.ui.interfaces.SearchSupported
import java.util.Locale

class BrilliantCallsFragment : BaseFragment(), SearchSupported {
    private lateinit var listViewContacts: ListView
    private lateinit var fab: ImageView
    private lateinit var adapter: CustomAdapter

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var allRecords: List<CallRecords> = mutableListOf()
    private var filteredContacts: MutableList<CallRecords> = mutableListOf()

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_calls
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_brilliant_calls, container, true)
        listViewContacts = view.findViewById(R.id.contactListView)
        fab = view.findViewById(R.id.fab)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            @SuppressLint("CheckResult")
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (ChatSDK.auth().cachedCredentialsAvailable()) {
                    ChatSDK.auth().authenticate().subscribe({
                        Logger.d("Re Authentication", "Authentication succeeded")
                    }, { error ->
                        Logger.d("Re Authentication", "Authentication failed")
                    })
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        loadDataFromDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    @SuppressLint("Range")
    override fun initViews() {
        Logger.d("reloading", "Text is null or empty")
    }

    override fun clearData() {
        // TODO: Implement clearing data if needed
    }

    override fun reloadData() {
        // TODO: Implement reloading data if needed
    }

    override fun filter(text: String?) {
        if (text != null) {
            filteredContacts = allRecords.filter { contact ->
                contact.contactName.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            }.toMutableList()
            updateAdapter(filteredContacts)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateAdapter(allRecords)
    }

    override fun onResume() {
        super.onResume()
        updateAdapter(allRecords)
    }

    private fun loadDataFromDatabase() {
        val sqLiteCallFragmentHelper = context?.let { SQLiteCallFragmentHelper(it) }
        if (sqLiteCallFragmentHelper != null) {
            allRecords = sqLiteCallFragmentHelper.allRecords
        } else {
            Logger.d("LoadData", "SQLiteCallFragmentHelper is null")
        }
        updateAdapter(allRecords)
    }

    private fun updateAdapter(records: List<CallRecords>) {
        context?.let {
            adapter = CustomAdapter(it, records)
            listViewContacts.adapter = adapter
        }
    }
}
