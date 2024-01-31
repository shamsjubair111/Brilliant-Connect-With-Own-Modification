package sdk.chat.app.xmpp.telco

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.ContactListViewAdapter
import sdk.chat.ui.ContactUtils.contactArrayList
import sdk.chat.ui.api.RegisteredUserService
import sdk.chat.ui.fragments.BaseFragment
import sdk.chat.ui.interfaces.SearchSupported

data class Contact(val displayName: String, val phoneNumber: String)
class BrilliantCallsFragment: BaseFragment(), SearchSupported {
    private lateinit var listViewContacts: ListView
    //private lateinit var contactsAdapter: ArrayAdapter<String> // Replace String with your Contact model class
    private lateinit var contactsAdapter: SimpleCursorAdapter
    private val CONTACTS_PERMISSION_CODE = 101
    var registeredUsers = hashSetOf<String>()

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_calls
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initViews()

        registeredUsers = RegisteredUserService.listRegisteredUsers() as HashSet<String>
        requestContactsPermission()
    }

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
            initViews()
        }
    }

    @SuppressLint("Range")
    override fun initViews() {
        //listViewContacts = requireView().findViewById(R.id.listViewContacts)

//        val cursor = getContactsCursor()
//        //val fromColumns = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
//        val fromColumns = arrayOf(
//            ContactsContract.Contacts._ID,
//            ContactsContract.Contacts.DISPLAY_NAME
//        )

        //val toViews = intArrayOf(android.R.id.text1)
        val toViews = intArrayOf(android.R.id.text1, android.R.id.text2)
//        contactsAdapter = SimpleCursorAdapter(
//            requireContext(),
//            android.R.layout.simple_list_item_2,
//            cursor,
//            fromColumns,
//            toViews,
//            0
//        )
        listViewContacts = requireView().findViewById(R.id.contactListView)
        val contactData = getContacts().map {
            mapOf("displayName" to it.displayName, "phoneNumber" to it.phoneNumber)
        }

        val adapter = context?.let { CustomAdapter(it, contactData) }
        listViewContacts.adapter = adapter

//        val adapter = SimpleAdapter(
//                context,
//                contactData,
//                R.layout.call_item,
//                arrayOf("displayName", "phoneNumber"),
//                intArrayOf(R.id.userContactName, R.id.userContactNumber)
//        )

        //listViewContacts.adapter = adapter
        //listViewContacts.adapter = contactsAdapter
        //listViewContacts.adapter = contactsAdapter
//        listViewContacts.setOnItemClickListener { _, _, position, _ ->
//            cursor?.let {
//                if (it.moveToPosition(position)) {
//                    val contactId = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
//                    val phoneNumber = getContactPhoneNumber(contactId)
//                    // Do something with the phone number, for example, display it
//                    Toast.makeText(requireContext(), "Phone Number: $phoneNumber", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
    }

    @SuppressLint("Range")
    private fun getContactPhoneNumber(contactId: String): String? {
        val contentResolver = requireActivity().contentResolver
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
        val selectionArgs = arrayOf(contactId)
        val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        )

        cursor?.use {
            if (it.moveToNext()) {
                return it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }

        return null
    }

    private fun getContactsCursor(): Cursor? {
        val contentResolver = requireActivity().contentResolver
        val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0"
        return contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                selection,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
        )
    }

//    override fun initViews() {
//        listViewContacts = requireView().findViewById(R.id.listView)
//        contactsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, getContactsList())
//        listViewContacts.adapter = contactsAdapter
//    }

    private fun getContactsList(): List<Contact> {
        // Replace this with your logic to fetch the list of contacts
        val contacts = listOf(
                Contact("1", "Contact1"),
                Contact("2", "Contact2"),
                Contact("3", "Contact3")
        )
        //return listOf("Contact1", "Contact2", "Contact3")
        return contacts;
    }

    @SuppressLint("Range")
    fun getContacts(): List<Contact> {
        val contentResolver: ContentResolver = requireActivity().contentResolver //context.contentResolver
        val contactArrayList: MutableList<Contact> = mutableListOf()
        val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        )
        val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0"

        val cursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                selection,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                println("contact_id"+ cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)))
                val contactId: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val contactName: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                val selectionArgs = arrayOf(contactId)
                // Get phone numbers for the contact
                val phoneCursor: Cursor? = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                )

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    do {
                        var phoneNumber: String = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        phoneNumber = ContactListViewAdapter.validPhoneNumber(phoneNumber)
                        val contactList = Contact(contactName, phoneNumber)
                        if(registeredUsers.contains(phoneNumber) && !contactArrayList.contains(contactList)){
                            contactArrayList.add(contactList)
                        }
                    } while (phoneCursor.moveToNext())

                    phoneCursor.close()
                }
            }
            cursor.close()
        }

        return contactArrayList
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

//    override fun getLayout(): Int {
//        return R.layout.fragment_brilliant_calls
//    }
//
//
//    override fun initViews() {
//
//    }
//
//    override fun clearData() {
//        // TODO: Implement clearing data if needed
//    }
//
//    override fun reloadData() {
//        // TODO: Implement reloading data if needed
//    }
//
//    override fun filter(text: String?) {
//        // TODO: Implement filtering data if needed
//    }
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