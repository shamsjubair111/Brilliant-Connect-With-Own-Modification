package sdk.chat.app.xmpp.telco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.hbb20.CountryCodePicker
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.activities.BaseActivity

class BrilliantOTPLoginActivity: BaseActivity() {

    var ccp: CountryCodePicker? = null
    var continueButton: Button? = null
    override fun getLayout(): Int {
        return R.layout.activity_brilliant_opt_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ccp = findViewById(R.id.ccp)
        continueButton = findViewById(R.id.btnContinue)

        ccp?.let { ccp ->
            findViewById<TextInputEditText>(R.id.editText)?.let { et ->
                ccp.registerCarrierNumberEditText(et)
                ccp.setNumberAutoFormattingEnabled(true)
                et.addTextChangedListener {
                    validate(ccp.isValidFullNumber)
                }
            }
            validate(ccp.isValidFullNumber)
        }

        continueButton?.setOnClickListener {
            next()
        }


    }

    fun validate(valid: Boolean) {
        continueButton?.let {
            it.isEnabled = valid
            it.alpha = if(valid) 1.0f else 0.5f
        }
    }

    public fun next() {
        val intent = Intent(this, BrilliantOTPVerificationActivity::class.java)
        ccp?.let {
            intent.putExtra("phone-number", it.fullNumber)
            ChatSDK.ui().startActivity(this, intent)
        }
    }
}