package sdk.chat.app.xmpp.telco

import android.os.Bundle
import android.widget.Button
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.activities.BaseActivity

class BrilliantWelcomeActivity: BaseActivity() {

//    private var _binding: BrilliantWelcomeActivityBinding? = null
//    private val binding get() = _binding!!
    override fun getLayout(): Int {
        return R.layout.activity_brilliant_welcome
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.btnAgree)?.let {btnAgree->
            btnAgree.setOnClickListener {
                btnAgree.isEnabled = false
                acceptTerms()
            }
        }

    }


    fun acceptTerms() {
        ChatSDK.shared().preferences.edit().putBoolean("terms_accepted", true).commit()
        ChatSDK.ui().startActivity(this, ChatSDK.ui().loginActivity)
    }
}