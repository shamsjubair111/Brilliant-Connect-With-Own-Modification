package sdk.chat.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;



import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class AudioActivity extends AppCompatActivity {

    private static final int CALL_REQUEST_CODE = 100;
    private static final int INCOMING_CALL_REQUEST_CODE = 101;
    private static String TAG = AudioActivity.class.getName();
    // UI references.
    private EditText mWcsUrlView;
    private EditText mSipLoginView;
    private EditText mSipPasswordView;
    private EditText mSipDomainView;
    private EditText mSipPortView;
    private EditText mAuthTokenView;
    private CheckBox mSipRegisterRequiredView;
    private TextView mConnectStatus;
    private Button mConnectButton;
    private Button mConnectTokenButton;
    private EditText mCalleeView;
    private EditText mInviteParametersView;

    private CheckBox googEchoCancellation;
    private CheckBox googAutoGainControl;
    private CheckBox googNoiseSupression;
    private CheckBox googHighpassFilter;
    private CheckBox googEchoCancellation2;
    private CheckBox googAutoGainControl2;
    private CheckBox googNoiseSuppression2;

    private CheckBox mProximitySensor;
    private CheckBox mSpeakerPhone;

    private TextView mCallStatus;
    private Button mCallButton;
    private Button mHoldButton;

    private EditText mDTMF;
    private Button mDTMFButton;

    private ScrollView scrollView;

    /**
     * Associated session with WCS
     */
//    private Session session;

    /**
     * SIP call
     */
//    private Call call;

    /**
     * Processing the call status and displaying UI changes
     */
//    private CallStatusEvent callStatusEvent;

    /**
     * UI alert for incoming call
     */
    private AlertDialog incomingCallAlert;

    private boolean connectWithToken = false;

    private ImageView callCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        TextView policyTextView = (TextView) findViewById(R.id.privacy_policy);
        policyTextView.setMovementMethod(LinkMovementMethod.getInstance());
//        String policyLink ="<a href=https://flashphoner.com/flashphoner-privacy-policy-for-android-tools/>Privacy Policy</a>";
//        policyTextView.setText(Html.fromHtml(policyLink));

        /**
         * Initialization of the API.
         */
//        Flashphoner.init(this);

        /**
         * UI controls
         */
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mWcsUrlView = (EditText) findViewById(R.id.wcs_url);
        mWcsUrlView.setText(sharedPref.getString("wcs_url", getString(R.string.wcs_url)));
        mSipLoginView = (EditText) findViewById(R.id.sip_login);
        mSipLoginView.setText(sharedPref.getString("sip_login", getString(R.string.sip_login)));
        mSipPasswordView = (EditText) findViewById(R.id.sip_password);
        mSipPasswordView.setText(sharedPref.getString("sip_password", getString(R.string.sip_password)));
        mSipDomainView = (EditText) findViewById(R.id.sip_domain);
        mSipDomainView.setText(sharedPref.getString("sip_domain", getString(R.string.sip_domain)));
        mSipPortView = (EditText) findViewById(R.id.sip_port);
        mSipPortView.setText(sharedPref.getString("sip_port", getString(R.string.sip_port)));
        mSipRegisterRequiredView = (CheckBox) findViewById(R.id.register_required);
        mSipRegisterRequiredView.setChecked(sharedPref.getBoolean("sip_register_required", true));
        mAuthTokenView = (EditText) findViewById(R.id.auth_token);
        scrollView = findViewById(R.id.scrollView);
        callCancelButton = findViewById(R.id.callCancelButton);
        scrollView.setBackgroundColor(R.color.brilliant_blue);

        callCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        callStatusEvent = new CallStatusEvent() {
//            /**
//             * WCS received SIP 100 TRYING
//             * @param call
//             */
//            @Override
//            public void onTrying(final Call call) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCallButton.setText(R.string.action_hangup);
//                        mCallButton.setTag(R.string.action_hangup);
//                        mCallButton.setEnabled(true);
//                        mCallStatus.setText(call.getStatus());
//                    }
//                });
//            }
//
//            /**
//             * WCS received SIP BUSY_HERE or BUSY_EVERYWHERE from SIP
//             * @param call
//             */
//            @Override
//            public void onBusy(final Call call) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCallButton.setText(R.string.action_call);
//                        mCallButton.setTag(R.string.action_call);
//                        mCallButton.setEnabled(true);
//                        mCallStatus.setText(call.getStatus());
//                    }
//                });
//                finish();
//            }
//
//            /**
//             * Call is failed
//             * @param call
//             */
//            @Override
//            public void onFailed(Call call) {
//                finish();
//
//            }
//
//            /**
//             * WCS received SIP 180 RINGING from SIP
//             * @param call
//             */
//            @Override
//            public void onRing(final Call call) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCallButton.setText(R.string.action_hangup);
//                        mCallButton.setTag(R.string.action_hangup);
//                        mCallButton.setEnabled(true);
//                        mCallStatus.setText(call.getStatus());
//                    }
//                });
//
//
//            }
//
//            /**
//             * Call is set on hold
//             * @param call
//             */
//            @Override
//            public void onHold(final Call call) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCallStatus.setText(call.getStatus());
//                    }
//                });
//            }
//
//            /**
//             * Call established
//             * @param call
//             */
//            @Override
//            public void onEstablished(final Call call) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCallStatus.setText(call.getStatus());
//                        mHoldButton.setEnabled(true);
//                        mDTMFButton.setEnabled(true);
//                    }
//                });
//
//            }
//
//            /**
//             * Call is terminated
//             * @param call
//             */
//            @Override
//            public void onFinished(final Call call) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCallButton.setText(R.string.action_call);
//                        mCallButton.setTag(R.string.action_call);
//                        mCallButton.setEnabled(true);
//                        mCallStatus.setText(call.getStatus());
//                        mHoldButton.setText(R.string.action_hold);
//                        mHoldButton.setTag(R.string.action_hold);
//                        mHoldButton.setEnabled(false);
//                        mDTMFButton.setEnabled(false);
//                        if (incomingCallAlert != null) {
//                            incomingCallAlert.hide();
//                            incomingCallAlert = null;
//                        }
//                    }
//                });
//                finish();
//
//            }
//        };

        mConnectStatus = (TextView) findViewById(R.id.connect_status);
        mConnectButton = (Button) findViewById(R.id.connect_button);
        /**
         * Connect button pressed
         */



        connectWithToken = false;

//        createSession();
        /**
         * Connection containing SIP details
         */
//        Connection connection = new Connection();
//        connection.setSipLogin("8001");
//        connection.setSipPassword("8001");
//        connection.setSipDomain("118.67.213.162");
//        connection.setSipOutboundProxy("0.0.0.0");
//        connection.setSipPort(5060);
//        connection.setSipRegisterRequired(mSipRegisterRequiredView.isChecked());
//        connection.setKeepAlive(true);
//        session.connect(connection);




        mConnectTokenButton = (Button) findViewById(R.id.connect_token_button);
        mConnectTokenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectTokenButton.getTag() == null || Integer.valueOf(R.string.action_connect_token).equals(mConnectTokenButton.getTag())) {
                    connectWithToken = true;
                    String authToken = mAuthTokenView.getText().toString();
                    if (authToken.isEmpty()) {
                        return;
                    }
                    mConnectButton.setEnabled(false);
                    mConnectTokenButton.setEnabled(false);
//                    createSession();
//                    Connection connection = new Connection();
//                    connection.setAuthToken(authToken);
//                    connection.setKeepAlive(true);
//                    session.connect(connection);
                } else {
                    mConnectButton.setEnabled(false);
                    mConnectTokenButton.setEnabled(false);
//                    session.disconnect();
                }
            }
        });
        mConnectTokenButton.setEnabled(false);

        mCalleeView = (EditText) findViewById(R.id.callee);
        mCalleeView.setText(sharedPref.getString("callee", getString(R.string.default_callee_name)));

        mInviteParametersView = (EditText) findViewById(R.id.invite_parameters);

        googEchoCancellation = (CheckBox) findViewById(R.id.googEchoCancellationCB);
        googAutoGainControl = (CheckBox) findViewById(R.id.googAutoGainControlCB);
        googNoiseSupression = (CheckBox) findViewById(R.id.googNoiseSupressionCB);
        googHighpassFilter = (CheckBox) findViewById(R.id.googHighpassFilterCB);
        googEchoCancellation2 = (CheckBox) findViewById(R.id.googEchoCancellation2CB);
        googAutoGainControl2 = (CheckBox) findViewById(R.id.googAutoGainControl2CB);
        googNoiseSuppression2 = (CheckBox) findViewById(R.id.googNoiseSuppression2CB);

        mProximitySensor = (CheckBox) findViewById(R.id.proximitySensor);
        mProximitySensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Flashphoner.getAudioManager().setUseProximitySensor(isChecked);
            }
        });
        mSpeakerPhone = (CheckBox) findViewById(R.id.speakerPhone);
        mSpeakerPhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Flashphoner.getAudioManager().setUseSpeakerPhone(isChecked);
            }
        });

        mCallStatus = (TextView) findViewById(R.id.call_status);
        mCallButton = (Button) findViewById(R.id.call_button);
        /**
         * Call button pressed
         */

        ActivityCompat.requestPermissions(AudioActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                CALL_REQUEST_CODE);


        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("callee", mCalleeView.getText().toString());
        editor.apply();



        mHoldButton = (Button) findViewById(R.id.hold_button);
        /**
         * Hold or Unhold button pressed
         * Hold the call if the call is ESTABLISHED.
         * Unhold the call if the call is on hold.
         */
        mHoldButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHoldButton.getTag() == null || Integer.valueOf(R.string.action_hold).equals(mHoldButton.getTag())) {
//                    call.hold();
                    mHoldButton.setText(R.string.action_unhold);
                    mHoldButton.setTag(R.string.action_unhold);
                } else {
//                    call.unhold();
                    mHoldButton.setText(R.string.action_hold);
                    mHoldButton.setTag(R.string.action_hold);
                }

            }
        });

        mDTMF = (EditText) findViewById(R.id.dtmf);
        mDTMFButton = (Button) findViewById(R.id.dtmf_button);
        mDTMFButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (call != null) {
////                    call.sendDTMF(mDTMF.getText().toString(), Call.DTMFType.RFC2833);
//                }
            }
        });


        policyTextView.setVisibility(View.INVISIBLE);
        mWcsUrlView.setVisibility(View.GONE);
        mCalleeView.setVisibility(View.GONE);
        mSipRegisterRequiredView.setVisibility(View.GONE);
        mSipPortView.setVisibility(View.GONE);
        mSipDomainView.setVisibility(View.GONE);
        mSipPasswordView.setVisibility(View.GONE);
        mSipLoginView.setVisibility(View.GONE);
        mConnectButton.setVisibility(View.GONE);
        mAuthTokenView.setVisibility(View.GONE);
        mConnectTokenButton.setVisibility(View.GONE);
        mInviteParametersView.setVisibility(View.GONE);
        googAutoGainControl.setVisibility(View.GONE);
        googEchoCancellation.setVisibility(View.GONE);
        googHighpassFilter.setVisibility(View.GONE);
        googNoiseSupression.setVisibility(View.GONE);
        googAutoGainControl2.setVisibility(View.GONE);
        googEchoCancellation2.setVisibility(View.GONE);
        googNoiseSuppression2.setVisibility(View.GONE);
        mProximitySensor.setVisibility(View.GONE);
        mSpeakerPhone.setVisibility(View.GONE);
        mCallButton.setVisibility(View.GONE);
        mHoldButton.setVisibility(View.GONE);
        mDTMF.setVisibility(View.GONE);
        mDTMFButton.setVisibility(View.GONE);


    }



//    private void createSession() {
//        SessionOptions sessionOptions = new SessionOptions("wss://tb.intercloud.com.bd:8443");
//        session = Flashphoner.createSession(sessionOptions);
//        session.on(new SessionEvent() {
//            @Override
//            public void onAppData(Data data) {
//
//            }
//
//
//
//            /**
//             * Connection established
//             * @param connection Current connection state
//             */
//            @Override
//            public void onConnected(final Connection connection) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mConnectButton.setText(R.string.action_disconnect);
//                        mConnectButton.setTag(R.string.action_disconnect);
//                        mConnectButton.setEnabled(true);
//                        mConnectTokenButton.setText(R.string.action_disconnect);
//                        mConnectTokenButton.setTag(R.string.action_disconnect);
//                        mConnectTokenButton.setEnabled(true);
//                        if (!mSipRegisterRequiredView.isChecked() || connectWithToken) {
////                            mConnectStatus.setText(connection.getStatus());
//                            mConnectStatus.setText("Calling " + getIntent().getStringExtra("callee"));
//
//                            mCallButton.setEnabled(true);
//                        } else {
//                            mConnectStatus.setText(connection.getStatus() + ". Registering...");
//                        }
//                        String token = connection.getAuthToken();
//                        if (token != null && !token.isEmpty()) {
//                            mAuthTokenView.setText(token);
//                            mConnectTokenButton.setEnabled(true);
////                            try {
//
////                            }
////                            catch (Exception e)
////                            {
//
//
////                            }
//
//
//                        }
//                        else{
//                            Toast.makeText(AudioActivity.this, "There might be some issue with your network, please try again after a few seconds", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//
//            /**
//             * Phone registered
//             * @param connection Current connection state
//             */
//            @Override
//            public void onRegistered(final Connection connection) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mConnectStatus.setText("Calling " + getIntent().getStringExtra("callee"));
////                        mConnectStatus.setText(connection.getStatus());
//                        mCallButton.setEnabled(true);
//                    }
//                });
//            }
//
//            /**
//             * Phone disconnected
//             * @param connection Current connection state
//             */
//            @Override
//            public void onDisconnection(final Connection connection) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
////                        finish();
//                        mConnectButton.setText(R.string.action_connect);
//                        mConnectButton.setTag(R.string.action_connect);
//                        mConnectButton.setEnabled(true);
//                        mConnectTokenButton.setText(R.string.action_connect_token);
//                        mConnectTokenButton.setTag(R.string.action_connect_token);
//                        mConnectTokenButton.setEnabled(true);
//                        mConnectStatus.setText(connection.getStatus());
//                        mCallButton.setText(R.string.action_call);
//                        mCallButton.setTag(R.string.action_call);
//                        mCallButton.setEnabled(false);
//                        mCallStatus.setText("");
//                        mHoldButton.setText(R.string.action_hold);
//                        mHoldButton.setTag(R.string.action_hold);
//                        mHoldButton.setEnabled(false);
//                        mDTMFButton.setEnabled(false);
//
//                    }
//                });
//            }
//        });
//
//        /**
//         * Add handler for incoming call
//         */
//        session.on(new IncomingCallEvent() {
//            @Override
//            public void onCall(final Call call) {
//                call.on(callStatusEvent);
//                /**
//                 * Display UI alert for the new incoming call
//                 */
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(AudioActivity.this);
//
//                        builder.setTitle("Incoming call");
//
//                        builder.setMessage("Incoming call from '" + call.getCaller() + "'");
//                        builder.setPositiveButton("Answer", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                AudioActivity.this.call = call;
//                                ActivityCompat.requestPermissions(AudioActivity.this,
//                                        new String[]{Manifest.permission.RECORD_AUDIO},
//                                        INCOMING_CALL_REQUEST_CODE);
//                            }
//                        });
//                        builder.setNegativeButton("Hangup", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                call.hangup();
//                                incomingCallAlert = null;
//                            }
//                        });
//                        incomingCallAlert = builder.show();
//                    }
//                });
//            }
//        });
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CALL_REQUEST_CODE: {
                if (grantResults.length == 0 ||
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user");
                    finish();
                } else {
                    mCallButton.setEnabled(false);
                    /**
                     * Get call options from the callee text field
                     */
//                    CallOptions callOptions = new CallOptions(getIntent().getStringExtra("callee"));
//                    AudioConstraints audioConstraints = callOptions.getConstraints().getAudioConstraints();
//                    MediaConstraints mediaConstraints = audioConstraints.getMediaConstraints();

//                    mediaConstraints.optional.add(
//                            new MediaConstraints.KeyValuePair("googEchoCancellation", Boolean.toString(googEchoCancellation.isChecked())));
//                    mediaConstraints.optional.add(
//                            new MediaConstraints.KeyValuePair("googAutoGainControl", Boolean.toString(googAutoGainControl.isChecked())));
//                    mediaConstraints.optional.add(
//                            new MediaConstraints.KeyValuePair("googNoiseSupression", Boolean.toString(googNoiseSupression.isChecked())));
//                    mediaConstraints.optional.add(
//                            new MediaConstraints.KeyValuePair("googHighpassFilter", Boolean.toString(googHighpassFilter.isChecked())));
//                    mediaConstraints.optional.add(
//                            new MediaConstraints.KeyValuePair("googEchoCancellation2", Boolean.toString(googEchoCancellation2.isChecked())));
//                    mediaConstraints.optional.add(
//                            new MediaConstraints.KeyValuePair("googAutoGainControl2", Boolean.toString(googAutoGainControl2.isChecked())));
//                    mediaConstraints.optional.add(
//                            new MediaConstraints.KeyValuePair("googNoiseSuppression2", Boolean.toString(googNoiseSuppression2.isChecked())));

//                    try {
//                        Map<String, String> inviteParameters = new Gson().fromJson(mInviteParametersView.getText().toString(),
//                                new TypeToken<Map<String, String>>() {
//                                }.getType());
//                        callOptions.setInviteParameters(inviteParameters);
//
//
//                            call = session.createCall(callOptions);
//                        if(call != null)
//                        {
//                            call.on(callStatusEvent);
//                            call.call();
//
//                        }
//                        else {
//                            Toast.makeText(AudioActivity.this, "problem with permission", Toast.LENGTH_SHORT).show();
////                            finish();
//                        }
//                    } catch (Throwable t) {
//                        Log.e(TAG, "Invite Parameters have wrong format of json object");
//                    }

                    /**
                     * Make the outgoing call
                     */

                    Log.i(TAG, "Permission has been granted by user");
                    break;
                }
            }
            case INCOMING_CALL_REQUEST_CODE: {
                if (grantResults.length == 0 ||
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    call.hangup();
                    incomingCallAlert = null;
                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    mCallButton.setText(R.string.action_hangup);
                    mCallButton.setTag(R.string.action_hangup);
                    mCallButton.setEnabled(true);
//                    mCallStatus.setText(call.getStatus());
//                    call.answer();
                    incomingCallAlert = null;
                    Log.i(TAG, "Permission has been granted by user");
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (session != null) {
//            session.disconnect();
//        }
    }

}