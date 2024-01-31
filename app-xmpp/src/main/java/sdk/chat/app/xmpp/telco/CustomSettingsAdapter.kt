package sdk.chat.app.xmpp.telco

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sdk.chat.demo.xmpp.R

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

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
        private val menuName: TextView = itemView.findViewById(R.id.menuName)

        fun bind(tr: SettingsItem) {
            //println("settings" + settings.settingsIcon)
            menuIcon.setImageResource(tr.settingsIcon)
            menuName.text = tr.settingsMenu
        }
    }
}