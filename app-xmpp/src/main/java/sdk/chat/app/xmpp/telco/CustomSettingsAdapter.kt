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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sdk.chat.core.dao.Keys
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.ExtendServices
import sdk.guru.common.RX

class SettingsAdapter(private val context: Context, private val settingsData: List<SettingsItem>) :
    RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.settings_item, parent, false)
        return SettingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val settings = settingsData[position]
        holder.bind(settings)
    }

    override fun getItemCount(): Int {
        return settingsData.size
    }

    override fun onViewRecycled(holder: SettingsViewHolder) {
        super.onViewRecycled(holder)
        holder.clearDisposables()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        compositeDisposable.clear()
    }

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
        private val menuName: TextView = itemView.findViewById(R.id.menuName)
        private lateinit var currentItem: SettingsItem
        private val viewHolderDisposables = CompositeDisposable()

        init {
            itemView.setOnClickListener(this)
        }

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

                else -> {
                    Toast.makeText(context, "Selected: ${currentItem.settingsMenu}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun bind(settings: SettingsItem) {
            currentItem = settings
            menuIcon.setImageResource(settings.settingsIcon)
            menuName.text = settings.settingsMenu
        }

        fun addBalance() {
            val userEntityId = ChatSDK.auth().currentUserEntityID
            val phoneNumber = userEntityId?.split("@")?.get(0)
            val amount = 20.0

            viewHolderDisposables.clear()
            if (phoneNumber != null) {
                val disposable: Disposable = Brilliant.shared().api.addBalance(phoneNumber, amount)
                    .observeOn(RX.main())
                    .subscribe({
                        Toast.makeText(context, "Balance added: $amount", Toast.LENGTH_SHORT).show()
                    }, { error ->
                        Toast.makeText(context, "Error adding balance: ${error.message}", Toast.LENGTH_SHORT).show()
                    })
                viewHolderDisposables.add(disposable)
            } else {
                Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        fun checkBalance() {
            val userEntityId = ChatSDK.auth().currentUserEntityID
            val phoneNumber = userEntityId?.split("@")?.get(0)

            viewHolderDisposables.clear()
            if (phoneNumber != null) {
                val disposable: Disposable = Brilliant.shared().api.checkBalance(phoneNumber)
                    .observeOn(RX.main())
                    .subscribe({ response ->
                        Toast.makeText(context, "Your current balance: $response", Toast.LENGTH_SHORT).show()
                    }, { error ->
                        Toast.makeText(context, "Error checking balance: ${error.message}", Toast.LENGTH_SHORT).show()
                    })
                viewHolderDisposables.add(disposable)
            } else {
                Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        fun clearDisposables() {
            viewHolderDisposables.clear()
        }
    }
}
