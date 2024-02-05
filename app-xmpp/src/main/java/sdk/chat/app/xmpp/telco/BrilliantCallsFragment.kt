package sdk.chat.app.xmpp.telco

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.ContactListViewAdapter
import sdk.chat.ui.fragments.BaseFragment
import sdk.chat.ui.interfaces.SearchSupported
import kotlin.random.Random

//data class Contact(val displayName: String, val phoneNumber: String)

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

       //requestContactsPermission()
        return view
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        registeredUsers = RegisteredUserService.listRegisteredUsers() as HashSet<String>
//        requestContactsPermission()
//    }

    private fun requestContactsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    CONTACTS_PERMISSION_CODE
            )
        } else {
            initViews()
        }
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

        listViewContacts = requireView().findViewById(R.id.contactListView)
//        val contacts = getContacts()
//
//        val adapter = context?.let { CustomAdapter(it, contacts) }
//        listViewContacts.adapter = adapter

//        listViewContacts = requireView().findViewById(R.id.contactListView)
//
//        // Launch a coroutine to fetch contacts asynchronously
//        GlobalScope.launch(Dispatchers.Main) {
//            val contacts = withContext(Dispatchers.IO) {
//                // Perform the blocking operation (getContacts) in IO dispatcher
//                getContacts()
//            }
//
//            // Update UI on the main (UI) thread
//            val adapter = context?.let { CustomAdapter(it, contacts) }
//            listViewContacts.adapter = adapter
//        }
    }


//    @SuppressLint("Range")
//    fun getContacts(): List<Contact> {
//        val contentResolver: ContentResolver = requireActivity().contentResolver //context.contentResolver
//        val contactArrayList: MutableList<Contact> = mutableListOf()
//        val projection = arrayOf(
//                ContactsContract.Contacts._ID,
//                ContactsContract.Contacts.DISPLAY_NAME
//        )
//        val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0"
//
//        val cursor: Cursor? = contentResolver.query(
//                ContactsContract.Contacts.CONTENT_URI,
//                projection,
//                selection,
//                null,
//                ContactsContract.Contacts.DISPLAY_NAME
//        )
//
//        if (cursor != null && cursor.count > 0) {
//            while (cursor.moveToNext()) {
//                println("contact_id"+ cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)))
//                val contactId: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
//                val contactName: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//
//                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
//                val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
//                val selectionArgs = arrayOf(contactId)
//                // Get phone numbers for the contact
//                val phoneCursor: Cursor? = contentResolver.query(
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null
//                )
//
//                if (phoneCursor != null && phoneCursor.moveToFirst()) {
//                    do {
//                        var phoneNumber: String = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                        phoneNumber = ContactListViewAdapter.validPhoneNumber(phoneNumber)
//                        val contactList = Contact(contactName, phoneNumber)
//                        if(registeredUsers.contains(phoneNumber) && !contactArrayList.contains(contactList)){
//                            contactArrayList.add(contactList)
//                        }
//                    } while (phoneCursor.moveToNext())
//
//                    phoneCursor.close()
//                }
//            }
//            cursor.close()
//        }
//
//        return contactArrayList
//    }

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

        // Initialize Loader
        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0"

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
                    val size = it.count
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
                data?.let {
                    val size = it.count
                    while (!it.isClosed && it.moveToNext()) {

                        val contactId = it.getInt(0)
                        val name = it.getString(1)
                        //val photo = it.getString(2)
                        val contactPhones = phones[contactId]
                        contactPhones?.forEach { phone ->
                            contacts.add(Contact(contactId, name, phone))
                        }
                    }

                    val adapter = context?.let { CustomAdapter(it, contacts) }
                    listViewContacts.adapter = adapter
                    //loadAdapter()
                    it.close()
                }
            }
        }

//        val emptyMutableContactsList: MutableList<Contact> = mutableListOf()
//        GlobalScope.launch(Dispatchers.Main) {
//            val contacts = withContext(Dispatchers.IO) {
//                // Convert Cursor to a list of contacts
//                processCursor(data)
//            }
//
//            adapter = context?.let { CustomAdapter(it, contacts) } ?: return@launch
//            listViewContacts.adapter = adapter
//        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Clear data when the loader is reset
        adapter.clear()
    }
    fun generateRandomPhoneNumber(): String {
        return "${Random.nextInt(100, 999)}-${Random.nextInt(100, 999)}-${Random.nextInt(1000, 9999)}"
    }

    fun generateRandomContact(index: Int): Contact {
        return Contact(index,"User $index", generateRandomPhoneNumber())
    }

    fun generateContactsList(size: Int): MutableList<Contact> {
        return MutableList(size) { index -> generateRandomContact(index + 1) }
    }
//    @SuppressLint("Range")
//    private fun processCursor(cursor: Cursor?): List<Contact> {
//
//
//        val contactsList = generateContactsList(0)
//
//
//        // Manually add contacts one by one
////        contactsList.add(Contact("John Doe", "555-1234"))
////        contactsList.add(Contact("Jane Smith", "555-5678"))
//
//
//        val contentResolver: ContentResolver = requireActivity().contentResolver //context.contentResolver
//        val contactArrayList: MutableList<Contact> = mutableListOf()
////        val projection = arrayOf(
////            ContactsContract.Contacts._ID,
////            ContactsContract.Contacts.DISPLAY_NAME
////        )
////        val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0"
////
////        val cursor: Cursor? = contentResolver.query(
////            ContactsContract.Contacts.CONTENT_URI,
////            projection,
////            selection,
////            null,
////            ContactsContract.Contacts.DISPLAY_NAME
////        )
//
//        if (cursor != null) {
//            val count = cursor.count
//            println("count"+count)
//        }
//        if (cursor != null && cursor.count > 0) {
//            while (cursor.moveToNext()) {
//                println("contact_id"+ cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)))
//                val contactId: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
//                val contactName: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//
//                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
//                val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
//                val selectionArgs = arrayOf(contactId)
//                // Get phone numbers for the contact
//                val phoneCursor: Cursor? = contentResolver.query(
//                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                    projection,
//                    selection,
//                    selectionArgs,
//                    null
//                )
//
//                if (phoneCursor != null && phoneCursor.moveToFirst()) {
//                    do {
//                        var phoneNumber: String = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                        phoneNumber = ContactListViewAdapter.validPhoneNumber(phoneNumber)
//                        val contact = Contact(1,contactName, phoneNumber, phoneNumber)
//                        if(!contactsList.contains(contact)){
//                            contactsList.add(contact)
//                        }
//                    } while (phoneCursor.moveToNext())
//
//                    phoneCursor.close()
//                }
//            }
//            cursor.close()
//        }
//
//        return contactsList
//    }

    // You can define a function to convert Cursor to a list of contacts here

    companion object {
        private const val LOADER_ID = 1
    }
}



//old code
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ListView
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import sdk.chat.demo.xmpp.R
//import sdk.chat.ui.CallListViewAdapter
//import sdk.chat.ui.ContactUtils
//import sdk.chat.ui.ContactUtils.contactArrayList
//import sdk.chat.ui.fragments.BaseFragment
//import sdk.chat.ui.fragments.ContactsFragment
//import sdk.chat.ui.interfaces.SearchSupported
//
//
//class BrilliantCallsFragment: BaseFragment(), SearchSupported {
//
//    var adapter1: CallListViewAdapter? = null
//    var listViewId: ListView? = null
//    private val REQUEST_READ_CONTACTS = 123
//
//
//
//    override fun getLayout(): Int {
//        return R.layout.fragment_brilliant_calls
//    }
//
//    fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = super.onCreateView(inflater!!, container, savedInstanceState)
//
////        recyclerView = view.findViewById(R.id.recyclerView);
//        listViewId = view!!.findViewById(R.id.listViewId)
//
//        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted, request it
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS);
//        } else {
//            // Permission is already granted, you can proceed with accessing contacts
//            ContactUtils.getContacts(activity)
//        }
//        initViews()
//
//        return view
//    }
//
//
//
//    override fun initViews() {
//
//        val list = contactArrayList;
////        Toast.makeText(context,""+ contactArrayList.size,Toast.LENGTH_LONG).show();
//
////        recyclerView = view.findViewById(R.id.recyclerView);
//
//        adapter1 = CallListViewAdapter(activity, contactArrayList);
//        listViewId = view?.findViewById(sdk.chat.ui.R.id.listViewId);
//
//        listViewId?.adapter = adapter1;
//
//
//
//
//
//
//
//    }
//
//    override fun clearData() {
//        // Clear any data if needed
//    }
//
//    override fun reloadData() {
//        // Reload data if needed
//    }
//
//    override fun filter(text: String?) {
//        // Implement filtering logic here
//    }
//
//    private fun hasContactPermissions(): Boolean {
//        return ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.READ_CONTACTS
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//
//
//
//
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with accessing contacts
//                ContactUtils.getContacts(context)
//            } else {
//                // Permission denied, handle accordingly (e.g., show a message or take alternative actions)
//            }
//        }
//    }
//
//
//}