package sdk.chat.app.xmpp.telco

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.rd.PageIndicatorView
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.activities.BaseActivity
import sdk.chat.ui.activities.LoginActivity
import sdk.guru.common.RX

class BrilliantIntroActivity: BaseActivity() {

    lateinit var viewPager: ViewPager2
    lateinit var pageIndicatorView: PageIndicatorView
    lateinit var adapter: BrilliantIntroPagerAdapter
    override fun getLayout(): Int {
        return R.layout.activity_brilliant_intro
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewPager = findViewById(R.id.viewPager)
        pageIndicatorView = findViewById(R.id.pageIndicatorView)

        io.reactivex.plugins.RxJavaPlugins.setErrorHandler(this)

        adapter = BrilliantIntroPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pageIndicatorView.setSelected(position)
            }
        })

        pageIndicatorView.selectedColor = getColor(R.color.textPrimary)
        pageIndicatorView.unselectedColor = getColor(R.color.gray_very_light_accent)
        pageIndicatorView.radius = 4

        pageIndicatorView.count = adapter.fragments.count()

        findViewById<ImageView>(R.id.ivBack)?.let {
            it.setOnClickListener {
                viewPager.currentItem = 0.coerceAtLeast(viewPager.currentItem - 1)
            }
        }

        findViewById<ImageView>(R.id.ivForward)?.let {
            it.setOnClickListener {
                viewPager.currentItem = (adapter.fragments.count() - 1).coerceAtMost(viewPager.currentItem + 1)
                if (viewPager.currentItem == adapter.fragments.count() - 1) {
                    val intent = Intent(this, LoginActivity::class.java)
                    ChatSDK.ui().startActivity(this, intent)
                }
            }
        }
        viewPager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        if (ChatSDK.auth() != null) {
            if (ChatSDK.auth().isAuthenticatedThisSession) {
                ChatSDK.ui().startMainActivity(this)
                return
            } else if (ChatSDK.auth().cachedCredentialsAvailable()) {
                dm.add(ChatSDK.auth().authenticate()
                    .observeOn(RX.main())
                    .subscribe {
                        ChatSDK.ui().startMainActivity(this)
                    })
            }
        }
    }

    fun showPushAlert() {
        // Inflate the custom layout
        val customView = LayoutInflater.from(this).inflate(R.layout.dialog_alert, null)

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .create()

        customView.findViewById<TextView>(R.id.dialog_text)?.let {

        }
        customView.findViewById<Button>(R.id.button_positive)?.let {

        }
        customView.findViewById<Button>(R.id.button_negative)?.let {

        }

        // Show the dialog
        dialog.show()
    }

}