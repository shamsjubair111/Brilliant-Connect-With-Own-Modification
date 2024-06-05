package sdk.chat.app.xmpp.telco

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.codewithkael.webrtcprojectforrecord.AppToAppAudio
import com.codewithkael.webrtcprojectforrecord.AppToAppVideo
import com.codewithkael.webrtcprojectforrecord.CallRecords
import com.codewithkael.webrtcprojectforrecord.OutgoingCall
import sdk.chat.demo.xmpp.R
import java.util.Locale

//class CustomAdapter(private val context: Context, private val contactData: List<Map<String, String>>) : BaseAdapter() {
class CustomAdapter(private val context: Context, private var contactData: List<CallRecords>) : BaseAdapter() {

    fun clear() {
//        contactData = emptyList()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return contactData.size
    }

    override fun getItem(position: Int): Any {
        return contactData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.call_item, parent, false)

        val userImage: ImageView = view.findViewById(R.id.userImage)
        val letterImage:TextView = view.findViewById(R.id.letterImage);
        val displayNameTextView: TextView = view.findViewById(R.id.userContactName)
        val phoneNumberTextView: TextView = view.findViewById(R.id.userContactNumber)
        val imageViewAppToSip: ImageView = view.findViewById(R.id.imageViewAppToSip)
        val imageViewVideo: ImageView = view.findViewById(R.id.imageViewVideo)
        val imageViewAppToApp: ImageView = view.findViewById(R.id.imageViewAppToApp)

        val contact = contactData[position]
        val phoneNumber = contact.contactNumber
        //val contactId = contact["id"]  // Assuming you have an ID field in your data

//        if (contact.photo != null) {
//            val contactUri =
//                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.id.toLong())
//            val photoUri =
//                Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
//            val cursor = context.contentResolver.query(
//                photoUri,
//                arrayOf(ContactsContract.Contacts.Photo.PHOTO),
//                null,
//                null,
//                null
//            )
//            if (cursor != null && cursor.moveToFirst()) {
//                val photoData = cursor.getBlob(0)
//                val photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.size)
//                userImage.setImageBitmap(photoBitmap)
//                cursor.close()
//            }
//            letterImage.setVisibility(View.GONE)
//        }
//        else {
        letterImage.setVisibility(View.VISIBLE)

        val splittedArray = contact.contactName.trim { it <= ' ' }.split("[\\s]+".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val st =
            if (splittedArray.size < 2) splittedArray[0][0].toString() else splittedArray[0][0].toString() + "" + splittedArray[1][0]
        letterImage.setText(st.uppercase(Locale.getDefault()))



        userImage.setImageResource(R.drawable.profile_circle)
//            userImage.setColorFilter(android.R.color.darker_gray);
//        }
        displayNameTextView.text = contact.contactName
        phoneNumberTextView.text = phoneNumber

        // Set OnClickListener for each button based on the phoneNumber
        imageViewAppToSip.setOnClickListener {
            // Perform action for button1 based on phoneNumber

            val intent = Intent(context, OutgoingCall::class.java)
            intent.putExtra("receiverNumber", phoneNumber?.let { it1 -> validPhoneNumber(it1) })
            intent.putExtra("contactName",contact.contactName);
            context.startActivity(intent)
        }

        imageViewVideo.setOnClickListener {
            imageViewVideo.animate();
            // Perform action for button2 based on phoneNumber
//            val intent = Intent(context, VideoActivity::class.java)
            val intent = Intent(context, AppToAppVideo::class.java)
            intent.putExtra("type", "video")
            intent.putExtra("receiverNumber",phoneNumber);
            intent.putExtra("contactName",contact.contactName);
            context.startActivity(intent)

        }

        imageViewAppToApp.setOnClickListener {
            imageViewAppToApp.animate();
            // Perform action for button3 based on phoneNumber
//            val intent = Intent(context, VideoActivity::class.java)
            val intent = Intent(context, AppToAppAudio::class.java)
            intent.putExtra("type", "audio")
            intent.putExtra("receiverNumber",phoneNumber);
            intent.putExtra("contactName",contact.contactName);
            context.startActivity(intent)
        }

        return view
    }

    fun validPhoneNumber(mobileNumber: String): String? {
        var mobileNumber = mobileNumber
        if (mobileNumber.length < 11) return mobileNumber
        mobileNumber = mobileNumber.replace("[\\s-]+".toRegex(), "")
        mobileNumber = mobileNumber.substring(mobileNumber.length - 11)
        mobileNumber = "88$mobileNumber"
        return mobileNumber
    }
}