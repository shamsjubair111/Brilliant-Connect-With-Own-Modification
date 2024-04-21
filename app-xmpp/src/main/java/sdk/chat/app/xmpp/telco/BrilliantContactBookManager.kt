package sdk.chat.app.xmpp.telco

import io.reactivex.Maybe
import sdk.chat.contact.ContactBookManager
import sdk.chat.contact.ContactBookUser
import sdk.chat.core.dao.Keys
import sdk.chat.core.dao.User
import sdk.chat.core.session.ChatSDK
import sdk.guru.common.RX

class BrilliantContactBookManager: ContactBookManager() {

//    override fun searchServer(contactBookUser: ContactBookUser): Maybe<SearchResult?>? {
//        return Maybe.defer {
//            val maybeList: MutableList<Maybe<User>> =
//                ArrayList()
//
//            for (index in contactBookUser.searchIndexes) {
//                if(index.key == Keys.Phone) {
//                    maybeList.add(ChatSDK.search().userForIndex(index.value, "user"))
//                }
//            }
//            Maybe.concat(maybeList)
//                .map { user: User? ->
//                    SearchResult(
//                        user,
//                        contactBookUser
//                    )
//                }.firstElement()
//        }.subscribeOn(RX.computation())
//    }


}
