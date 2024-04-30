package com.codewithkael.webrtcprojectforrecord;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.codewithkael.webrtcprojectforrecord.databinding.ActivityMainBinding;
import com.permissionx.guolindev.PermissionX;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PermissionX.init(MainActivity.this)
                .permissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                ).request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        startActivity(new Intent(MainActivity.this, OutgoingCall.class)
                                .putExtra("username", "sip:2001@192.168.0.105")
                                .putExtra("receiver", "sip:2000@192.168.0.105"));
                    } else {
                        Toast.makeText(MainActivity.this, "You should accept all permissions", Toast.LENGTH_LONG).show();
                    }
                });
//        binding.enterBtn.setOnClickListener(v -> {
//
//        });
    }
}
