package sdk.chat.app.xmpp.telco

import android.provider.ContactsContract


object Constants {
    var PROJECTION_NUMBERS = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )
    var PROJECTION_DETAILS = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI
    )
}

