package sdk.chat.core.base;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.AuthenticationHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public abstract class AbstractAuthenticationHandler implements AuthenticationHandler {

    protected String currentUserID = null;
    protected boolean isAuthenticatedThisSession = false;

    private static final String PREFS_NAME = "CurrentUserIDPrefs";
    private static final String PREF_KEY_CURRENT_USER_ID = "CurrentUserID";

    protected Completable authenticating;
    protected Completable loggingOut;

    protected User cachedUser = null;

    public Boolean isAuthenticating () {
        return authenticating != null;
    }

    protected void setAuthStateToIdle() {
        authenticating = null;
        loggingOut = null;
    }

    public Boolean isAuthenticatedThisSession() {
        return isAuthenticated() && isAuthenticatedThisSession;
    }

    /**
     * Currently supporting only string and integers. Long and other values can be added later on.
     */
    public void setCurrentUserEntityID(String currentUserID) {
        this.currentUserID = currentUserID;

        isAuthenticatedThisSession = true;
        ChatSDK.shared().getKeyStorage().put(Keys.CurrentUserID, currentUserID);

        Context context = ChatSDK.shared().context();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_CURRENT_USER_ID, currentUserID);
        editor.apply();
    }

    public void clearCurrentUserEntityID() {
        cachedUser = null;
        currentUserID = null;
        isAuthenticatedThisSession = false;
        ChatSDK.shared().getKeyStorage().remove(Keys.CurrentUserID);
    }

    /**
     * @return the save auth id saved in the preference manager.
     * The preference manager is initialized when the NetworkManager.Init(context) is called.
     */
    public String getCurrentUserEntityID() {
        if (currentUserID == null || !isAuthenticated()) {
            currentUserID = ChatSDK.shared().getKeyStorage().get(Keys.CurrentUserID);

            if (currentUserID == null) {
                Context context = ChatSDK.shared().context();
                SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                currentUserID = sharedPreferences.getString(PREF_KEY_CURRENT_USER_ID, null);
            }
        }
        return currentUserID;
    }

    @Override
    public User currentUser() {
        String entityID = getCurrentUserEntityID();

        if (entityID == null) {
            cachedUser = null;
        }

        if(cachedUser == null || !cachedUser.equalsEntityID(entityID)) {
            if (entityID != null && !entityID.isEmpty()) {
                cachedUser = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
            }
            else {
                cachedUser = null;
            }
        }
        return cachedUser;
    }

    public void cancel() {
        if (isAuthenticating()) {
            authenticating = null;
        }
    }

    public void stop() {
        cachedUser = null;
        currentUserID = null;
        authenticating = null;
        isAuthenticatedThisSession = false;
        loggingOut = null;
    }

    @Override
    public AccountDetails cachedAccountDetails() {
        return null;
    }

}
