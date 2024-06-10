package sdk.chat.app.xmpp.telco

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.codewithkael.webrtcprojectforrecord.CallRecords
import com.codewithkael.webrtcprojectforrecord.SQLiteCallFragmentHelper
import com.lassi.common.utils.Logger
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.api.RegisteredUserService
import sdk.chat.ui.fragments.BaseFragment
import sdk.chat.ui.interfaces.SearchSupported

data class Contact(
    var id: Long,
    var name: String,
    var number: String,
    var photo: String?
)
class BrilliantCallsFragment: BaseFragment(), SearchSupported, LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var listViewContacts: ListView
    private lateinit var contactsAdapter: SimpleCursorAdapter
    private lateinit var fab: ImageView
    private val CONTACTS_PERMISSION_CODE = 101
    var registeredUsers = hashSetOf<String>()
    private lateinit var adapter: CustomAdapter
    private var phones: HashMap<Long, MutableList<String>> = hashMapOf()
    protected val contacts: MutableList<Contact> = ArrayList()

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_calls
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_brilliant_calls, container, true)
        listViewContacts = view.findViewById(R.id.contactListView)
        fab = view.findViewById(R.id.fab)


        return view
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            @SuppressLint("CheckResult")
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if(ChatSDK.auth().cachedCredentialsAvailable()) {
                    ChatSDK.auth().authenticate().subscribe({
                        Logger.d("Re Authentication","Authentication succeeded")
                    }, { error ->
                        Logger.d("Re Authentication","Authentication failed")
                    })
                }
            }

        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


    @SuppressLint("Range")
    override fun initViews() {

    }

    override fun clearData() {
        // TODO: Implement clearing data if needed
    }

    override fun reloadData() {
        // TODO: Implement reloading data if needed
    }

    override fun filter(text: String?) {
        // TODO: Implement filtering data if needed
       // contactsAdapter.filter.filter(text)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sqLiteCallFragmentHelper = SQLiteCallFragmentHelper(context)
        val list: List<CallRecords> = sqLiteCallFragmentHelper.getAllRecodrs()
        adapter = context?.let { CustomAdapter(it, list) }!!

        listViewContacts.adapter = adapter

        //loaderManager.initLoader(0, null, this)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            //requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_CODE)
        } else {
            // Permission already granted, you can initiate loading contacts or any other action
//            loaderManager.initLoader(0, null, this)








        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        return when (id) {
            0 -> CursorLoader(
                requireActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                Constants.PROJECTION_NUMBERS,
                null,
                null,
                null
            )
            else -> CursorLoader(
                requireActivity(),
                ContactsContract.Contacts.CONTENT_URI,
                Constants.PROJECTION_DETAILS,
                null,
                null,
                null
            )
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        // Process the loaded data and update UI
        when (loader.id) {
            0 -> {
                data?.let {
                    while (!it.isClosed && it.moveToNext()) {
                        val contactId = it.getLong(0)
                        val phone = it.getString(1)
                        val list = phones.getOrPut(contactId) { mutableListOf() }
                        list.add(phone)
                    }
                    it.close()
                }
                loaderManager.initLoader(1, null, this)
            }
            1 -> {
                registeredUsers = RegisteredUserService.listRegisteredUsers() as HashSet<String>
                data?.let {
                    while (!it.isClosed && it.moveToNext()) {
                        val contactId = it.getLong(0)
                        val name = it.getString(1)
                        val photo = it.getString(2)
                        var contactPhones = phones[contactId]
                        contactPhones?.forEach { phone ->
                            var validPhoneNumber = validPhoneNumber(phone)
                            if(validPhoneNumber != null && registeredUsers.contains(validPhoneNumber) && !contacts.contains(Contact(contactId, name, validPhoneNumber, photo))){
                                contacts.add(Contact(contactId, name, validPhoneNumber, photo))
                            }
                        }
                    }
                    it.close()
//                    adapter = context?.let { CustomAdapter(it, contacts) }!!
//                    listViewContacts.adapter = adapter

                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.clear()
    }

    fun validPhoneNumber(mobileNumber: String): String {
        var mobileNumber = mobileNumber
        mobileNumber = mobileNumber.replace("[\\s-]+".toRegex(), "")
        if (mobileNumber.length < 11) return mobileNumber
        mobileNumber = mobileNumber.substring(mobileNumber.length - 11)
        mobileNumber = "88$mobileNumber"
        return mobileNumber
    }

    override fun onResume() {
        super.onResume()
        val sqLiteCallFragmentHelper = SQLiteCallFragmentHelper(context)
        val list: List<CallRecords> = sqLiteCallFragmentHelper.getAllRecodrs()
        adapter = context?.let { CustomAdapter(it, list) }!!

        listViewContacts.adapter = adapter
    }
}