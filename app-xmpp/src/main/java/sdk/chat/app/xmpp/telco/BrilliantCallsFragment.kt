package sdk.chat.app.xmpp.telco

import CallRecordAdapter
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
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codewithkael.webrtcprojectforrecord.CallRecord
import com.codewithkael.webrtcprojectforrecord.SQLiteCallFragmentHelper
import com.lassi.common.utils.Logger
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.fragments.BaseFragment
import sdk.chat.ui.interfaces.SearchSupported
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BrilliantCallsFragment : BaseFragment(), SearchSupported {
    private lateinit var recyclerViewCalls: RecyclerView
    private lateinit var fab: ImageView
    private lateinit var adapter: CallRecordAdapter

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var allRecords: List<CallRecord> = mutableListOf()
    private var filteredContacts: MutableList<CallRecord> = mutableListOf()

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_calls
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_brilliant_calls, container, false)
        recyclerViewCalls = view.findViewById(R.id.recyclerViewCalls)
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

        // Set up RecyclerView
        recyclerViewCalls.layoutManager = LinearLayoutManager(requireContext())
        adapter = CallRecordAdapter(requireContext(), filteredContacts)
        recyclerViewCalls.adapter = adapter

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
        loadDataFromDatabase()
    }

    override fun filter(text: String?) {
        if (text != null) {
            filteredContacts = allRecords.filter { contact ->
                contact.name?.lowercase(Locale.getDefault())
                    ?.contains(text.lowercase(Locale.getDefault())) == true
            }.toMutableList()
            updateAdapter(filteredContacts)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadDataFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        loadDataFromDatabase()
    }

    private fun loadDataFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val sqLiteCallFragmentHelper = context?.let { SQLiteCallFragmentHelper(it) }
            if (sqLiteCallFragmentHelper != null) {
                allRecords = sqLiteCallFragmentHelper.allRecords
                Logger.d("LoadData", "Fetched ${allRecords.size} records")
                CoroutineScope(Dispatchers.Main).launch {
                    updateAdapter(allRecords)
                }
            } else {
                Logger.d("LoadData", "SQLiteCallFragmentHelper is null")
            }
        }
    }

    private fun updateAdapter(records: List<CallRecord>) {
        Logger.d("UpdateAdapter", "Updating adapter with ${records.size} records")
        adapter.updateData(records)
    }
}
