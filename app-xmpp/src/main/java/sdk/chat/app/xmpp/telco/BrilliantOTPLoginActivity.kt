package sdk.chat.app.xmpp.telco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
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

        if (ccp != null && continueButton != null) {
            findViewById<TextInputEditText>(R.id.editText)?.let { et ->
                ccp?.registerCarrierNumberEditText(et)
                ccp?.setNumberAutoFormattingEnabled(true)
                et.addTextChangedListener {
                    validate(ccp?.isValidFullNumber ?: false)
                }
            }
            validate(ccp?.isValidFullNumber ?: false)
        } else {
            Log.e("OTPLogin", "CCP or ContinueButton is null")
            finish() // Optionally handle the error case
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
            ccp?.let {
                val phoneNumber = it.fullNumberWithPlus
                if (!isFinishing && phoneNumber.isNotEmpty()) {
                    val intent = Intent(this@BrilliantOTPLoginActivity, BrilliantOTPVerificationActivity::class.java)
                    intent.putExtra("phone-number", phoneNumber)
                    Log.d("OTPLogin", "Phone number: $phoneNumber")
                    withContext(Dispatchers.Main) {
                        ChatSDK.ui().startActivity(this@BrilliantOTPLoginActivity, intent)
                    }
                } else {
                    Log.e("OTPLogin", "Invalid phone number or activity is finishing")
                    Toast.makeText(this@BrilliantOTPLoginActivity, "Invalid phone number", Toast.LENGTH_SHORT).show()
                    continueButton?.isEnabled = true
                }
            } ?: run {
                Log.e("OTPLogin", "CountryCodePicker is null")
                continueButton?.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
