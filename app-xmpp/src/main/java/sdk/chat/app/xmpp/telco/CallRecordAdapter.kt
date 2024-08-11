import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codewithkael.webrtcprojectforrecord.AppToAppCall
import com.codewithkael.webrtcprojectforrecord.CallRecord
import com.codewithkael.webrtcprojectforrecord.OutgoingCall
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber
import java.util.Locale

class CallRecordAdapter(private val context: Context, private var callRecords: List<CallRecord>) :
    RecyclerView.Adapter<CallRecordAdapter.ViewHolder>() {

    private val colors: ArrayList<String> = arrayListOf(
        "#A1DD70", "#EE4E4E", "#E49BFF", "#3ABEF9", "#ffffff", "#FF7F3E"
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val letterImage: TextView = view.findViewById(R.id.letterImage)
        val displayNameTextView: TextView = view.findViewById(R.id.userContactName)
        val phoneNumberTextView: TextView = view.findViewById(R.id.userContactNumber)
        val imageViewAppToSip: ImageView = view.findViewById(R.id.imageViewAppToSip)
        val imageViewVideo: ImageView = view.findViewById(R.id.imageViewVideo)
        val imageViewAppToApp: ImageView = view.findViewById(R.id.imageViewAppToApp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.call_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val callRecord = callRecords[position]
        val name = callRecord.name
        val number = callRecord.number
        val photo = callRecord.photo

        if (photo != null && photo.isNotEmpty()) {
            holder.userImage.setImageURI(Uri.parse(photo))
            holder.userImage.visibility = View.VISIBLE
            holder.letterImage.visibility = View.GONE
        } else {
            val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
            holder.letterImage.text = initials.uppercase(Locale.getDefault())
            holder.letterImage.setTextColor(Color.parseColor(colors[position % colors.size]))
            holder.letterImage.setTypeface(null, Typeface.BOLD)
            holder.letterImage.visibility = View.VISIBLE

            holder.userImage.setImageResource(R.drawable.profile_circle)
            holder.userImage.setColorFilter(android.R.color.darker_gray)
        }

        holder.displayNameTextView.text = name
        holder.phoneNumberTextView.text = number

        holder.imageViewAppToSip.setOnClickListener {
            if (ChatSDK.auth().currentUserEntityID != null) {
                val intent = Intent(context, OutgoingCall::class.java)
                intent.putExtra("receiverNumber", number?.let { validPhoneNumber(it) })
                intent.putExtra("contactName", name)
                intent.putExtra("photo", photo)
                context.startActivity(intent)
            }
        }

        holder.imageViewVideo.setOnClickListener {
            holder.imageViewVideo.animate()
            if (ChatSDK.auth().currentUserEntityID != null) {
                val intent = Intent(context, AppToAppCall::class.java)
                intent.putExtra("type", "video")
                intent.putExtra("receiverNumber", number)
                intent.putExtra("contactName", name)
                intent.putExtra("photo", photo)
                context.startActivity(intent)
            }
        }

        holder.imageViewAppToApp.setOnClickListener {
            holder.imageViewAppToApp.animate()
            if (ChatSDK.auth().currentUserEntityID != null) {
                val intent = Intent(context, AppToAppCall::class.java)
                intent.putExtra("type", "audio")
                intent.putExtra("receiverNumber", number)
                intent.putExtra("contactName", name)
                intent.putExtra("photo", photo)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = Intent.ACTION_MAIN
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return callRecords.size
    }

    fun updateData(newRecords: List<CallRecord>) {
        callRecords = newRecords
        notifyDataSetChanged()
    }
}
