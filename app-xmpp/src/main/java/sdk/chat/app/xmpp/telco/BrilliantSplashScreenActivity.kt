package sdk.chat.app.xmpp.telco

import android.os.Bundle
import sdk.chat.core.session.ChatSDK
import sdk.chat.ui.BaseInterfaceAdapter
import sdk.chat.ui.activities.SplashScreenActivity

class BrilliantSplashScreenActivity: SplashScreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Testing
//        Brilliant.shared().api.register("ben999xxyy", "123").subscribe()
    }

    @Override
    override fun startLoginActivity() {

//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)

        // TODO: Testing
        if (ChatSDK.shared().preferences.getBoolean("terms_accepted", false)) {
            startActivityForResult(ChatSDK.ui().getLoginIntent(this, null), AUTH)
        } else {
            (ChatSDK.ui() as? BaseInterfaceAdapter)?.let {
                startActivityForResult(it.intentForActivity(this, BrilliantWelcomeActivity::class.java, null, 0), AUTH)
            }
        }
    }


}