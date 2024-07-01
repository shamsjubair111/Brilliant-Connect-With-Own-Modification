
package sdk.chat.app.xmpp.telco

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.ExtendServices
import sdk.chat.ui.utils.ToastHelper
import sdk.guru.common.RX

class SettingsAdapter(private val context: Context, private val settingsData: List<SettingsItem>) :
    RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.settings_item, parent, false)
        //println("settings count" + getItemCount())
        return SettingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        println("position" + position)
        val settings = settingsData[position]
        println("settings.settingsMenu" + settings.settingsMenu)
        holder.bind(settings)
    }

    override fun getItemCount(): Int {
        return settingsData.size
    }

//    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
//        private val menuName: TextView = itemView.findViewById(R.id.menuName)
//
//        fun bind(tr: SettingsItem) {
//            //println("settings" + settings.settingsIcon)
//            menuIcon.setImageResource(tr.settingsIcon)
//            menuName.text = tr.settingsMenu
//        }
//    }

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
        private val menuName: TextView = itemView.findViewById(R.id.menuName)
        private lateinit var currentItem: SettingsItem

        init {
            // Set click listener on the itemView
            itemView.setOnClickListener(this)
        }

        // Implement onClick method
        override fun onClick(view: View?) {

            when (currentItem.settingsMenu) {
                "Extended Services" -> {
                    val intent = Intent(context, ExtendServices::class.java)
                    context.startActivity(intent)
                }
                "My Balance" -> {
                    checkBalance()
                }
                "Add Balance" -> {
                    addBalance()
                }
                // Add more cases as needed
                else -> {

                }
            }
        }

        fun bind(settings: SettingsItem) {
            currentItem = settings // Store the current item for later reference
            menuIcon.setImageResource(settings.settingsIcon)
            menuName.text = settings.settingsMenu
        }

        fun addBalance() {
            val phoneNumber = ChatSDK.auth().getCurrentUserEntityID().split("@")[0]
            val amount = 20.0;
            phoneNumber?.let {
                Brilliant.shared().api.addBalance(it, amount)
                    .observeOn(RX.main())
                    .subscribe({
                        Toast.makeText(context, "Balance added: $amount", Toast.LENGTH_SHORT).show()
                    }, { error ->
                        Toast.makeText(context, "Error on adding balance: ${error.message}", Toast.LENGTH_SHORT).show()
                    })
            }
        }

        fun checkBalance() {
            val phoneNumber = ChatSDK.auth().getCurrentUserEntityID().split("@")[0]
            phoneNumber?.let {
                Brilliant.shared().api.checkBalance(it)
                    .observeOn(RX.main())
                    .subscribe({ response ->
                        Toast.makeText(context, "Your current balance: $response", Toast.LENGTH_SHORT).show()
                    }, { error ->
                        Toast.makeText(context, "Error on checking balance: ${error.message}", Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }
}