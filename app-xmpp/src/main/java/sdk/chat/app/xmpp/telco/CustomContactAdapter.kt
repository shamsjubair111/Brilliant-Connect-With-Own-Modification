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
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.AudioActivity
import sdk.chat.ui.VideoActivity

//class CustomAdapter(private val context: Context, private val contactData: List<Map<String, String>>) : BaseAdapter() {
    class CustomAdapter(private val context: Context, private var contactData: List<Contact>) : BaseAdapter() {

    fun clear() {
        contactData = emptyList()
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

        val displayNameTextView: TextView = view.findViewById(R.id.userContactName)
        val phoneNumberTextView: TextView = view.findViewById(R.id.userContactNumber)
        val imageViewAppToSip: ImageView = view.findViewById(R.id.imageViewAppToSip)
        val imageViewVideo: ImageView = view.findViewById(R.id.imageViewVideo)
        val imageViewAppToApp: ImageView = view.findViewById(R.id.imageViewAppToApp)

        val contact = contactData[position]
        val phoneNumber = contact.number
        //val contactId = contact["id"]  // Assuming you have an ID field in your data

        displayNameTextView.text = contact.name
        phoneNumberTextView.text = phoneNumber

        // Set OnClickListener for each button based on the phoneNumber
        imageViewAppToSip.setOnClickListener {
            // Perform action for button1 based on phoneNumber

            val intent = Intent(context, AudioActivity::class.java)
                intent.putExtra("callee", phoneNumber?.let { it1 -> validPhoneNumber(it1) })
                context.startActivity(intent)
        }

        imageViewVideo.setOnClickListener {
            imageViewVideo.animate();
            // Perform action for button2 based on phoneNumber
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra("type", "video")
            intent.putExtra("receiverNumber",phoneNumber);
            context.startActivity(intent)

        }

        imageViewAppToApp.setOnClickListener {
            imageViewAppToApp.animate();
            // Perform action for button3 based on phoneNumber
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra("type", "audio")
            intent.putExtra("receiverNumber",phoneNumber);
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