package sdk.chat.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ExtendServices extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extend_services);

        webView = findViewById(R.id.webview);
        getTokenAndLoadWebView();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    private void getTokenAndLoadWebView() {
        String tokenUrl = "https://payment.brilliant.com.bd/TelCoTest/Test/GetToken";
        String userName = "BriliantConnect";
        String password = "Brill@4cd3@#";
        String userId = "8801755639764";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userid", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, tokenUrl, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle token response

                        Gson gson = new Gson();
                        String json = String.valueOf(response);
                        JwtHeader jwtHeader = gson.fromJson(json, JwtHeader.class);
                        loadWebViewWithToken(jwtHeader.auth_token);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("UserName", userName);
                headers.put("Password", password);
                return headers;
            }
        };

        // Add the request to the RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void loadWebViewWithToken(String token) {
        String url = "https://payment.brilliant.com.bd/TelCoTest/Test/Show?userid=8801755639764&token=" + token;
        webView.setVisibility(View.VISIBLE); // Make WebView visible
        webView.loadUrl(url);
    }


}

class JwtHeader {
    String auth_token;
    String message;
}



