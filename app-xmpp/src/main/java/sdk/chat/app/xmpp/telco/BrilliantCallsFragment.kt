package sdk.chat.app.xmpp.telco

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.api.RegisteredUserService
import sdk.chat.ui.fragments.BaseFragment
import sdk.chat.ui.interfaces.SearchSupported

data class Contact(
    var id: Int,
    var name: String,
    var number: String
)
class BrilliantCallsFragment: BaseFragment(), SearchSupported, LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var listViewContacts: ListView
    private lateinit var contactsAdapter: SimpleCursorAdapter
    private val CONTACTS_PERMISSION_CODE = 101
    var registeredUsers = hashSetOf<String>()
    private lateinit var adapter: CustomAdapter
    private var phones: HashMap<Int, MutableList<String>> = hashMapOf()
    protected val contacts: MutableList<Contact> = ArrayList()

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_calls
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_brilliant_calls, container, true)
        listViewContacts = view.findViewById(R.id.contactListView)
        return view
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == CONTACTS_PERMISSION_CODE && grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            //initViews()
        }
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
        contactsAdapter.filter.filter(text)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loaderManager.initLoader(0, null, this)
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
                        val contactId = it.getInt(0)
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
                        val contactId = it.getInt(0)
                        val name = it.getString(1)
                        //val photo = it.getString(2)
                        var contactPhones = phones[contactId]
                        contactPhones?.forEach { phone ->
                            var validPhoneNumber = validPhoneNumber(phone)
                            if(validPhoneNumber != null && registeredUsers.contains(validPhoneNumber) && !contacts.contains(Contact(contactId, name, validPhoneNumber))){
                                contacts.add(Contact(contactId, name, validPhoneNumber))
                            }
                        }
                    }
                    it.close()
                    adapter = context?.let { CustomAdapter(it, contacts) }!!
                    listViewContacts.adapter = adapter

                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.clear()
    }

    fun validPhoneNumber(mobileNumber: String): String? {
        var mobileNumber = mobileNumber
        mobileNumber = mobileNumber.replace("[\\s-]+".toRegex(), "")
        if (mobileNumber.length < 11) return mobileNumber
        mobileNumber = mobileNumber.substring(mobileNumber.length - 11)
        mobileNumber = "88$mobileNumber"
        return mobileNumber
    }
}