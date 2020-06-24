package sdk.chat.firebase.location;

import android.Manifest;
import android.content.Context;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.Tab;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.ui.icons.Icons;
import sdk.guru.common.BaseConfig;


/**
 * Created by pepe on 08.03.18.
 */

public class FirebaseNearbyUsersModule extends AbstractModule {

    public static final FirebaseNearbyUsersModule instance = new FirebaseNearbyUsersModule();

    public static FirebaseNearbyUsersModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<FirebaseNearbyUsersModule> builder() {
        return instance.config;
    }

    public static FirebaseNearbyUsersModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public Config<FirebaseNearbyUsersModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {

        int index = config().tabIndex;

        //index = Math.min(index, ChatSDK.ui().tabs().size());
        index = Math.max(index, 0);

        ChatSDK.ui().setTab(nearbyUsersTab(), index);
        LocationHandler.shared().initialize(context);

        ChatSDK.hook().addHook(Hook.sync(data -> {

            // When we get the first location update, then start listening for new users
            ChatSDK.events().disposeOnLogout(LocationHandler.shared().once().subscribe(location -> {
                GeoFireManager.shared().startListeningForItems(location.getLatitude(), location.getLongitude(), config().maxDistance);
            }));

            // Add the current user
            GeoItemManager.shared().addTrackedItem(currentUser(), false);

        }), HookEvent.DidAuthenticate);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            GeoFireManager.shared().stopListeningForItems();
            GeoItemManager.shared().remove(currentUser());
        }), HookEvent.WillLogout);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            if (currentUser() != null) {
                GeoItemManager.shared().remove(currentUser());
            }
        }), HookEvent.UserWillDisconnect);

        AppBackgroundMonitor.shared().addListener(new AppBackgroundMonitor.Listener() {
            @Override
            public void didStart() {
                if (ChatSDK.auth().isAuthenticatedThisSession()) {
                    GeoItemManager.shared().addTrackedItem(currentUser(), true);
                }
             }

            @Override
            public void didStop() {
//                if (ChatSDK.auth().isAuthenticatedThisSession()) {
//                    GeoItemManager.shared().removeFromGeoFire(new GeoItem(ChatSDK.currentUser().getEntityID(), GeoItem.USER));
//                }
            }
        });

    }

    protected GeoItem currentUser() {
        return new GeoItem(ChatSDK.currentUser().getEntityID(), GeoItem.USER);
    }

    @Override
    public String getName() {
        return "FirebaseNearbyUsersModule";
    }

    public static class Config<T> extends BaseConfig<T> {

        /**
         * Maximum distance to pick up nearby users
         */
        public int maxDistance = 50000;

        /**
         * How much distance must be moved to update the server with our new location
         */
        public int minRefreshDistance = 50;

        /**
         * Minimum refresh time in seconds
         */
        public long minRefreshTime = 1;

        /**
         * Which tab index should the nearby users tab be added at
         */
        public int tabIndex = 2;

        /**
         * If this custom property is set it should contain an Double which is the number
         * of meters within which the exact distance isn't displayed. For example, if this
         * is set to 1000, if a user is within 1000m their exact distance won't be displayed
         * and <1000 will be displayed instead.
         */
        public double minimumDisplayResolution = 100;

        /**
         * Low data mode will minimise the data used
         */
        public boolean lowDataMode = false;

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Set the max distance
         * @param maxDistance in meters
         * @return builder
         */
        public Config<T> setMaxDistance (int maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        /**
         * Set the minimum refresh distance
         * @param minimumDistance in meters
         * @return builder
         */
        public Config<T> setMinRefreshDistance (int minimumDistance) {
            this.minRefreshDistance = minimumDistance;
            return this;
        }

        /**
         * Set the minimum refresh distance
         * @param minRefreshTime in seconds
         * @return builder
         */
        public Config<T> setMinRefreshTime (int minRefreshTime) {
            this.minRefreshTime = minRefreshTime;
            return this;
        }

        /**
         /**
         * If this custom property is set it should contain an Double which is the number
         * of meters within which the exact distance isn't displayed. For example, if this
         * is set to 1000, if a user is within 1000m their exact distance won't be displayed
         * and <1000 will be displayed instead.
         * @param minimumDisplayResolution in meters
         * @return builder
         */
        public Config<T> setMinimumDisplayResolution (double minimumDisplayResolution) {
            this.minimumDisplayResolution = minimumDisplayResolution;
            return this;
        }

        /**
         * Which tab index should the nearby users tab be added at
         * @param index
         * @return builder
         */
        public Config<T> setTabIndex (int index) {
            this.tabIndex = index;
            return this;
        }

    }

    public static Tab nearbyUsersTab() {
        return new Tab(R.string.nearby_users, Icons.get(Icons.choose().location, Icons.shared().tabIconColor), nearbyUsersFragment());
    }

    public static Fragment nearbyUsersFragment() {
        return NearbyUsersFragment.newInstance();
    }

    public static Config config() {
        return shared().config;
    }

    public List<String> requiredPermissions() {
        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        return permissions;
    }
}
