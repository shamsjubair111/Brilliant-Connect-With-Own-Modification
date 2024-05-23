package sdk.chat.app.xmpp.telco

open class Brilliant {

    var api = BrilliantAPI()
    companion object {
        private var instance = Brilliant()
        fun shared(): Brilliant {
            return instance
        }
    }

    val debug = false

    public fun api(): BrilliantAPI {
        return api
    }

//    public fun updatePushToken() {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
//
//            // Log and toast
//            val msg = getString(R.string.msg_token_fmt, token)
//            Log.d(TAG, msg)
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//        })
//    }

}