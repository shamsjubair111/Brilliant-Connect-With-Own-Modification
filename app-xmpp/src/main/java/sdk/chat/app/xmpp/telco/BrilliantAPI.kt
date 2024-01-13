package sdk.chat.app.xmpp.telco

import io.reactivex.Completable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import sdk.chat.core.session.ChatSDK
import sdk.chat.demo.xmpp.R
import kotlin.random.Random

class BrilliantAPI {

    var otp: String? = null
    var to: String? = null
    var url = "https://appsrv.intercloud.com.bd/test/api/VendorOTP/SendOTP"
    public fun sendOTP(to: String): Completable {
        this.otp = Random.nextInt(1000, 10000).toString()
        this.to = to
        return resendOTP()
    }

    public fun resendOTP(): Completable {
        this.otp?.let {otp ->
            this.to?.let { to ->
                return this.impl_sendOTP(to, otp)
            }
        }
        return Completable.error(Exception(ChatSDK.getString(R.string.otp_or_phone_invalid)))
    }

    public fun verifyOTP(reply: String?): Boolean {
        return reply == this.otp
    }

    public fun clearOTP() {
        otp = null
    }

    fun impl_sendOTP(to: String, otp: String): Completable {
        return Completable.create {

            val client = OkHttpClient()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val json = """
            {
                "from": "TelcoBright",
                "to": "$to",
                "Content": "${String.format(ChatSDK.getString(R.string.your_otp_is__), otp)}",
            }
            """.trimIndent()
            val body = json.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("login", "TelcoBright")
                .addHeader("password", "IO&3(DF&")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    it.onError(IOException("Unexpected code $response"))
                } else {
                    it.onComplete()
                }
            }
        }
    }

}