package sdk.chat.ui;

import static sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithkael.webrtcprojectforrecord.AppToAppAudio;
import com.codewithkael.webrtcprojectforrecord.AppToAppVideo;
import com.codewithkael.webrtcprojectforrecord.OutgoingCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import io.reactivex.CompletableObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class ContactProfile extends AppCompatActivity implements Consumer<Throwable>, CompletableObserver {

    DisposableMap dm = new DisposableMap();
    private TextView userNameTextView;
    private TextView textView9;
    private ImageView backImage;
    private ImageView userPicture;
    private ImageView videoCall;
    private ImageView imageView3;
    private ImageView chatIcon;
    private ImageView appAudioCall;
    private String receiverNumber;
    private String receiverName;

    private LinearLayout directCall;
    private LinearLayout apptoappVideoCall;
    private LinearLayout apptoappMessage;
    private LinearLayout apptonumberSms;
    private LinearLayout apptoappAudioCall;


    public ArrayList<Integer> imageList = new ArrayList<>(Arrays.asList(
            R.drawable.ragnar,
            R.drawable.sazid_vai,
            R.drawable.suchi_apu,
            R.drawable.maruf_vai,
            R.drawable.angela_merkel,
            R.drawable.joe_biden,
            R.drawable.donald_trump,
            R.drawable.messi,
            R.drawable.ronaldo

    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        userNameTextView = findViewById(R.id.userNameTextView);
        textView9 = findViewById(R.id.textView9);
        backImage = findViewById(R.id.backImage);
        directCall = findViewById(R.id.directCall);
        apptoappAudioCall = findViewById(R.id.apptoappAudioCall);
        apptoappVideoCall = findViewById(R.id.apptoappVideoCall);
        apptoappMessage = findViewById(R.id.apptoappMessage);
        apptonumberSms = findViewById(R.id.apptonumberSms);

        receiverName = getIntent().getStringExtra("contactName");
        userNameTextView.setText(receiverName);
        int imageResId = getIntent().getIntExtra("imageResId", -1);


        textView9.setText(getIntent().getStringExtra("contactNumber"));


        userPicture = findViewById(R.id.userPicture);
        String contactImage = getIntent().getStringExtra("contactImage");
        if(!Objects.equals(contactImage, "")){
            Uri contactImageUri = Uri.parse(contactImage);
            userPicture.setImageURI(contactImageUri);
        }

//        Glide.with(this)
//                .load(imageList.get(imageResId % imageList.size()))
//                .apply(RequestOptions.circleCropTransform())
//                .into(userPicture);


        receiverNumber = validPhoneNumber(getIntent().getStringExtra("contactNumber"));


        apptoappVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getIntent().getStringExtra("registered").equals("yes")) {
                    Intent intent = new Intent(getApplicationContext(), AppToAppVideo.class);
                    intent.putExtra("receiverNumber", receiverNumber);
                    intent.putExtra("type", "video");
                    intent.putExtra("contactName", receiverName);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(ContactProfile.this, "Number Not Registered", Toast.LENGTH_SHORT).show();
                }
            }
        });


        apptoappMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                User user = ChatSDK.db().fetchUserWithEntityID(receiverNumber + "@localhost");

                dm.add(ChatSDK.thread().createThread("", User.convertIfPossible(Arrays.asList(user))).observeOn(RX.main()).subscribe(thread -> {
                    ChatSDK.ui().startChatActivityForID(ContactProfile.this, thread.getEntityID());
                    finish();
                }, ContactProfile.this));
            }
        });

        apptoappAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getStringExtra("registered").equals("yes")) {
                    Intent intent = new Intent(getApplicationContext(), AppToAppAudio.class);
                    intent.putExtra("receiverNumber", receiverNumber);
                    intent.putExtra("type", "audio");
                    intent.putExtra("contactName", receiverName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(ContactProfile.this, "Number Not Registered", Toast.LENGTH_SHORT).show();
                }
            }
        });


        directCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactProfile.this, OutgoingCall.class);
                intent.putExtra("receiverNumber", receiverNumber);
                intent.putExtra("activityName", "ContactProfile");
                intent.putExtra("contactName", receiverName);
                startActivity(intent);
            }
        });


        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void accept(Throwable t) {
        onError(t);
    }

    public void onSubscribe(@NonNull Disposable d) {
        dm.add(d);
    }

    /**
     * Called once the deferred computation completes normally.
     */
    public void onComplete() {

    }

    /**
     * Called once if the deferred computation 'throws' an exception.
     *
     * @param e the exception, not null.
     */
    public void onError(@NonNull Throwable e) {
        e.printStackTrace();
        System.out.println("SHOW TOAST" + e.getLocalizedMessage());

        ToastHelper.show(this, e.getLocalizedMessage());
    }
}