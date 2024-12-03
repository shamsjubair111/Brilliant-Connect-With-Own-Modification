package sdk.chat.app.xmpp;

import android.app.Application;
import android.os.StrictMode;

import org.jivesoftware.smack.util.TLSUtils;
import org.pmw.tinylog.Logger;

import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.receipts.XMPPReadReceiptsModule;
import sdk.chat.app.xmpp.telco.BrilliantContactBookManager;
import sdk.chat.app.xmpp.telco.BrilliantInterfaceAdapter;
import sdk.chat.app.xmpp.telco.BrilliantOTPLoginActivity;
import sdk.chat.app.xmpp.telco.BrilliantSplashScreenActivity;
import sdk.chat.app.xmpp.telco.BrilliantTabBarActivity;
import sdk.chat.app.xmpp.utils.SecureKeyStore;
import sdk.chat.contact.ContactBookModule;
import sdk.chat.core.module.ImageMessageModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.xmpp.R;
import sdk.chat.encryption.xmpp.XMPPEncryptionModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.message.audio.AudioMessageModule;
import sdk.chat.message.file.FileMessageModule;
import sdk.chat.message.location.LocationMessageModule;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.video.VideoMessageModule;
import sdk.chat.ui.extras.ExtrasModule;
import sdk.chat.ui.module.UIModule;
public class MainApplication extends Application {

    public SecureKeyStore store;

    @Override
    public void onCreate() {
        super.onCreate();
        xmpp();
    }


    public void xmpp() {
        try {



            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());

//            store = new SecureKeyStore(this);




            ChatSDK.builder()

                    // Configure the library
                    .setGoogleMaps("AIzaSyCwwtZrlY9Rl8paM0R6iDNBEit_iexQ1aE")
                    .setAnonymousLoginEnabled(false)
                    .setDebugModeEnabled(true)
                    .setThreadDestructionEnabled(false)
                    .setClientPushEnabled(true)
                    .setAllowUserToRejoinGroup(true)
                    .setLogoDrawableResourceID(R.drawable.logo)
                    .setPushNotificationImageDefaultResourceId(R.drawable.logo_trans)

//                    .setDebugUsername("1")
//                    .setDebugPassword("123")
                    .setDebugUsername("4")
                    .setDebugPassword("123")

                    .build()
                    .setInterfaceAdapter(BrilliantInterfaceAdapter.class)

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseUploadModule.shared())
                    .addModule(FirebasePushModule.builder(config ->
                            config.setFirebaseFunctionsRegion("us-central1")
                    ).shared())

                    .addModule(XMPPModule.builder()
//                            .setXMPP("75.119.138.93", "xmpp.app", 5222)
//                          .setXMPP("36.255.71.143", "localhost", 5222)
                          .setXMPP("103.209.42.15", "localhost", 5222)
                            .setSecurityMode("disabled")
                            .setAllowServerConfiguration(false)
                            .setPingInterval(5)
                            .setDebugEnabled(true)
                            .build())

                    .addModule(AudioMessageModule.shared())
                    .addModule(LocationMessageModule.shared())
                    .addModule(ImageMessageModule.shared())
                    .addModule(VideoMessageModule.shared())
                    .addModule(FileMessageModule.shared())
                    .addModule(StickerMessageModule.builder()
                            .build())
                    .addModule(ContactBookModule.builder(config -> config.contactBookManager = new BrilliantContactBookManager())) //.addModule(ContactBookModule.shared())

                    .addModule(XMPPEncryptionModule.shared())
                    .addModule(UIModule.builder()
                            .setRequestPermissionsOnStartup(false)
                            .setMessageSelectionEnabled(true)
                            .setUsernameHint("JID")
                            .setMessageForwardingEnabled(true)
                            .setMessageReplyEnabled(true)
                            .setResetPasswordEnabled(false)
                            .setPublicRoomCreationEnabled(true)
                            .setPublicRoomsEnabled(false)
                            .setGroupsEnabled(true)
                            .setTheme(0)
                            .build())

                    .addModule(XMPPReadReceiptsModule.shared())
                    .addModule(ExtrasModule.builder()
                            .setQrCodesEnabled(true)
                            .setDrawerEnabled(false)
                            .build())

                    .build().activateWithEmail(this, "ben@sdk.chat");
//                    .build().activateWithEmail(this, "apple@telcobright.com");


            FirebasePushModule.config().firebaseFunctionsRegion = "us-central1";


//            ChatSDK.config().setDebugUsername(Device.honor() ? "a3": "a4");
//            ChatSDK.config().setDebugPassword("123");

//            ChatSDK.ui().setThreadDetailsActivity(ThreadDetailsActivity.class);

//            chatsdkAuth(Device.honor() ? "xxx1" : "xxx2", "123", "test@conference.xmpp.app");

            // TODO: Testing
//            ChatSDK.ui().setLoginActivity(BrilliantIntroActivity.class);
            ChatSDK.ui().setLoginActivity(BrilliantOTPLoginActivity.class);
            ChatSDK.ui().setSplashScreenActivity(BrilliantSplashScreenActivity.class);
            ChatSDK.ui().setMainActivity(BrilliantTabBarActivity.class);


//            ChatSDKUI.icons().actionBarIconColor = getColor(R.color.textOrange);

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getLocalizedMessage());
            assert(false);
        }


        XMPPModule.config().setConnectionConfigProvider(builder -> {
            try {
                builder.setCustomX509TrustManager(new TLSUtils.AcceptAllTrustManager());
                builder.setCompressionEnabled(false);
            } catch(Exception e) {
                Logger.debug(e.getLocalizedMessage());
            }
        });



        // Use encrypted shared preferences
//        try {
//            Field field = ChatSDK.class.getDeclaredField("keyStorage");
//            field.setAccessible(true);
//            field.set(ChatSDK.shared(), new SecureKeyStore(this));
//            System.out.println("Ok");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

//    @Override
//    public SharedPreferences getSharedPreferences(String name, int mode) {
//        if (name.equals(ChatSDK.Preferences) && mode == Context.MODE_PRIVATE) {
//            return store.pref();
//        }
//        return super.getSharedPreferences(name, mode);
//    }

}