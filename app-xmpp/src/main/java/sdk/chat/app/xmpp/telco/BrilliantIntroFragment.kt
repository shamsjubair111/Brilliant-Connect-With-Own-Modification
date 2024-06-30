package sdk.chat.app.xmpp.telco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.fragments.BaseFragment

class BrilliantIntroFragment : BaseFragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"
        private const val ARG_DESC = "desc"
        private const val ARG_IMAGE = "image"

        fun newInstance(
            @StringRes title: Int,
            @StringRes subtitle: Int,
            @StringRes desc: Int,
            @DrawableRes image: Int
        ): BrilliantIntroFragment {
            val fragment = BrilliantIntroFragment()
            val args = Bundle().apply {
                putInt(ARG_TITLE, title)
                putInt(ARG_SUBTITLE, subtitle)
                putInt(ARG_DESC, desc)
                putInt(ARG_IMAGE, image)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_brilliant_intro, container, false)

        arguments?.let {
            val title = it.getInt(ARG_TITLE)
            val subtitle = it.getInt(ARG_SUBTITLE)
            val desc = it.getInt(ARG_DESC)
            val imageRes = it.getInt(ARG_IMAGE)

            view.findViewById<ImageView>(R.id.imageView)?.setImageResource(imageRes)
            view.findViewById<TextView>(R.id.tvTitle)?.setText(title)
            view.findViewById<TextView>(R.id.tvSubtitle)?.setText(subtitle)
            view.findViewById<TextView>(R.id.tvDesc)?.setText(desc)
        }

        return view
    }

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_intro
    }

    override fun initViews() {
        // Initialize any specific views here
    }

    override fun clearData() {
        // Clear any data here if needed
    }

    override fun reloadData() {
        // Reload any data here if needed
    }
}