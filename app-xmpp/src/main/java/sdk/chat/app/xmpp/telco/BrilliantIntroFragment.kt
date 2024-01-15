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

class BrilliantIntroFragment: BaseFragment {

    var title: Int
    var subtitle: Int
    var desc: Int
    var image: Int

    constructor(@StringRes title: Int, @StringRes subtitle: Int, @StringRes desc: Int, @DrawableRes image: Int): super() {
        this.title = title
        this.subtitle = subtitle
        this.desc = desc
        this.image = image
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        view?.findViewById<ImageView>(R.id.imageView)?.let {
            it.setImageResource(image)
        }
        view?.findViewById<TextView>(R.id.tvTitle)?.let {
            it.text = getString(title)
        }
        view?.findViewById<TextView>(R.id.tvSubtitle)?.let {
            it.text = getString(subtitle)
        }
        view?.findViewById<TextView>(R.id.tvDesc)?.let {
            it.text = getString(desc)
        }


        return view
    }

    override fun getLayout(): Int {
        return R.layout.fragment_brilliant_intro
    }

    override fun initViews() {
    }

    override fun clearData() {
    }

    override fun reloadData() {
    }

}