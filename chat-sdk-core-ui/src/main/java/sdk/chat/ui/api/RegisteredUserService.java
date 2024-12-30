package sdk.chat.ui.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisteredUserService {
    private static final String ejabberdApiUrl = "http://ej.hobenaki.com:5443/api";//"http://36.255.71.143:5443/api";
    private static final String host = "localhost";
    public static Set<String> listRegisteredUsers() {
        Set<String> result = new HashSet<>();

        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(ejabberdApiUrl + "/registered_users")
                    .post(RequestBody.create(MediaType.parse("application/json"), "{\"host\": \"" + host + "\"}"))
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String responseData = response.body().string();
                System.out.println("responseData" + responseData);
                result.addAll(parseUsernames(responseData));
            } else {
                System.out.println("Error: " + response.code() + " - " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static   Set<String> parseUsernames(String responseData) throws IOException {
        // Parse the JSON array and return a set of usernames


        ObjectMapper objectMapper = new ObjectMapper();
        String[] array = objectMapper.readValue(responseData, String[].class);

        Set<String> hashSet = new HashSet<>();
        for (String element : array) {
            hashSet.add(element);
        }
        return hashSet;
    }
}
