package sdk.chat.app.xmpp.telco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.*
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import sdk.chat.ui.activities.BaseActivity

class BrilliantOTPLoginActivity: BaseActivity() {

    private var ccp: CountryCodePicker? = null
    private var continueButton: Button? = null
    private val scope = CoroutineScope(Dispatchers.Main)

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
            continueButton?.isEnabled = false
            next()
        }
    }

    private fun validate(valid: Boolean) {
        continueButton?.let {
            if (Brilliant.shared().debug) {
                it.isEnabled = true
                it.alpha = 1.0f
            } else {
                it.isEnabled = valid
                it.alpha = if (valid) 1.0f else 0.5f
            }
        }
    }

    private fun next() {
        scope.launch {
            val intent = Intent(this@BrilliantOTPLoginActivity, BrilliantOTPVerificationActivity::class.java)
            ccp?.let {
                intent.putExtra("phone-number", it.fullNumberWithPlus)
                withContext(Dispatchers.Main) {
                    ChatSDK.ui().startActivity(this@BrilliantOTPLoginActivity, intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
