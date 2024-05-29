package sdk.chat.ui.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Set;

import sdk.chat.ui.Contact;

public class GsonUtils {

    private static Gson gson = new Gson();

    public static String toJson(Set<Contact> contactSet) {
        return gson.toJson(contactSet);
    }

    public static Set<Contact> fromJson(String json) {
        Type type = new TypeToken<Set<Contact>>() {}.getType();
        return gson.fromJson(json, type);
    }
}