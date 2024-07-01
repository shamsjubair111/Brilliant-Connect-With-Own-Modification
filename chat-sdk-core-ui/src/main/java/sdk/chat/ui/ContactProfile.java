package sdk.chat.ui;

import static sdk.chat.ui.utils.ValidPhoneNumberUtil.validPhoneNumber;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codewithkael.webrtcprojectforrecord.AppToAppAudio;
import com.codewithkael.webrtcprojectforrecord.AppToAppVideo;
import com.codewithkael.webrtcprojectforrecord.OutgoingCall;
import com.google.i18n.phonenumbers.NumberParseException;

import java.util.Arrays;

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

    //    public static String validPhoneNumber(String mobileNumber) {
//        mobileNumber = mobileNumber.replaceAll("[\\s-]+", "");
//        if(mobileNumber.length()<11)
//            return mobileNumber;
//        mobileNumber = mobileNumber.substring(mobileNumber.length() - 11);
//        mobileNumber = "88" + mobileNumber;
//
//        return mobileNumber;
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile);

        userNameTextView = findViewById(R.id.userNameTextView);
        textView9 = findViewById(R.id.textView9);
        backImage = findViewById(R.id.backImage);
        videoCall = findViewById(R.id.videoCall);
        imageView3 = findViewById(R.id.imageView3);
        chatIcon = findViewById(R.id.chatIcon);
        appAudioCall = findViewById(R.id.imageView7);

        userNameTextView.setText(getIntent().getStringExtra("contactName"));
        int imageResId = getIntent().getIntExtra("imageResId", -1);

        textView9.setText(getIntent().getStringExtra("contactNumber"));
        byte[] byteArray = getIntent().getByteArrayExtra("contactImage");

//        if (byteArray.length != 0) {
//            Bitmap photoBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//
//        }

        userPicture = findViewById(R.id.userPicture);
//        userPicture.setImageResource(imageResId);

        Glide.with(this)
                .load(imageResId)
                .apply(RequestOptions.circleCropTransform())
                .into(userPicture);


        try {
            receiverNumber = validPhoneNumber(getIntent().getStringExtra("contactNumber"));
        } catch (NumberParseException e) {
            throw new RuntimeException(e);
        }


        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getIntent().getStringExtra("registered").equals("yes")) {
                    Intent intent = new Intent(getApplicationContext(), AppToAppVideo.class);
                    intent.putExtra("receiverNumber", receiverNumber);
                    intent.putExtra("type", "video");
                    intent.putExtra("contactName", getIntent().getStringExtra("contactName"));
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(ContactProfile.this, "Number Not Registered", Toast.LENGTH_SHORT).show();
                }
            }
        });


        chatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                User user = ChatSDK.db().fetchUserWithEntityID(receiverNumber + "@localhost");

                dm.add(ChatSDK.thread().createThread("", User.convertIfPossible(Arrays.asList(user))).observeOn(RX.main()).subscribe(thread -> {
                    ChatSDK.ui().startChatActivityForID(ContactProfile.this, thread.getEntityID());
                    finish();
                }, ContactProfile.this));
            }
        });

        appAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getStringExtra("registered").equals("yes")) {
                    Intent intent = new Intent(getApplicationContext(), AppToAppAudio.class);
                    intent.putExtra("receiverNumber", receiverNumber);
                    intent.putExtra("type", "audio");
                    intent.putExtra("contactName", getIntent().getStringExtra("contactName"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(ContactProfile.this, "Number Not Registered", Toast.LENGTH_SHORT).show();
                }
            }
        });


        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactProfile.this, OutgoingCall.class);
                intent.putExtra("receiverNumber", receiverNumber);
                intent.putExtra("activityName", "ContactProfile");
                intent.putExtra("contactName", getIntent().getStringExtra("contactName"));
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