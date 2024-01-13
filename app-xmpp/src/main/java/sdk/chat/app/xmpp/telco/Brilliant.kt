package sdk.chat.app.xmpp.telco

open class Brilliant {

    var api = BrilliantAPI()
    companion object {
        private var instance = Brilliant()
        fun shared(): Brilliant {
            return instance
        }
    }

    public fun api(): BrilliantAPI {
        return api
    }

}