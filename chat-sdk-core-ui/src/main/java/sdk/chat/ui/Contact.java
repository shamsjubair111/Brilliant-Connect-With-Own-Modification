package sdk.chat.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Contact {
    private long id;
    private String name;
    private String number;
    private String photo;

    public Contact(String name, String number, String photo) {
        this.name = name;
        this.number = number;
        this.photo = photo;
    }
    public Contact(long id, String name, String number, String photo) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.photo = photo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public static String toJsonArray(List<Contact> contacts) {
        Gson gson = new Gson();
        return gson.toJson(contacts);
    }

    public static List<Contact> fromJsonArray(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Contact>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public static String toJsonSetFromStringSet(Set<String> contacts) {
        Gson gson = new Gson();
        return gson.toJson(contacts);
    }

    public static Set<String> fromJsonSetToStringSet(String json) {
        Gson gson = new Gson();
        Type setType = new TypeToken<HashSet<String>>(){}.getType();
        return gson.fromJson(json, setType);
    }
}