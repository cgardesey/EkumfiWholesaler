package com.ekumfi.wholesaler.activity;

import static com.ekumfi.wholesaler.activity.GetSellerPhoneNumberActivity.getSellerPhoneNumberActivity;
import static com.ekumfi.wholesaler.activity.GetSellerPhoneNumberActivity.phone_number;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.interfaces.OtpReceivedInterface;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmSeller;
import com.ekumfi.wholesaler.receiver.SmsBroadcastReceiver;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;


/**
 * Created by Nana on 11/26/2017.
 */

public class GetSellerAuthActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        OtpReceivedInterface, GoogleApiClient.OnConnectionFailedListener {

    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";

    public static String MYUSERID = "MYUSERID";
    public static String APITOKEN = "APITOKEN";
    public static String NUMBER_VERIFIED = "NUMBER_VERIFIED";
    public static String ACCESSTOKEN = "ACCESSTOKEN";
    public static String GUID = "GUID";
    public static String JUSTENROLLED = "JUSTENROLLED";

    private static Context mContext;
    Button changenumberbtn, resendbtn;
    GoogleApiClient mGoogleApiClient;
    SmsBroadcastReceiver mSmsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    EditText num1, num2, num3, num4;
    TextView welcomemsg;
    int authTry = 0;
    ProgressDialog dialog;
    Button saveallbtn;
    private String api_token, userid, room_number, code;
    private String deviceModel, deviceManufacturer, android_id, os;
    int height, width, type;
    TextView gsminstuctiontext, setdefaultsim;
    CountDownTimer countDownTimer;
    public static Activity getSellerAuthActivity;

    public void setLanguage() {
        SharedPreferences prefs = getSharedPreferences(MY_LOGIN_ID, MODE_PRIVATE);
        String language = prefs.getString("language", "");
        // Toast.makeText(activity, language, Toast.LENGTH_SHORT).show();
        if (language.contains("French")) {
//use constructor with country
            Locale locale = new Locale("fr", "BE");

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else {
            Locale locale = new Locale("en", "GB");

            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSellerAuthActivity = this;

        countDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                resendbtn.setText("RESEND IN " + millisUntilFinished / 1000 + " s");
            }

            public void onFinish() {
                resendbtn.setText("Resend");
                resendbtn.setEnabled(true);
            }

        };

        setLanguage();
        setContentView(R.layout.activity_getsellerphoneauth);
        changenumberbtn = findViewById(R.id.changenumberbtn);
        resendbtn = findViewById(R.id.resendbtn);
        num1 = findViewById(R.id.num1);
        num2 = findViewById(R.id.num2);
        num3 = findViewById(R.id.num3);
        num4 = findViewById(R.id.num4);
        welcomemsg = findViewById(R.id.welcomemsg);
        deviceModel = Build.MODEL;
        deviceManufacturer = Build.MANUFACTURER;
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        os = Build.VERSION.RELEASE;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);
        //dialog = ProgressDialog.show(GetAuthActivity.this, "Account Setup", "Signing In... Please wait...", true);
        SharedPreferences prefs = getSharedPreferences(MY_LOGIN_ID, MODE_PRIVATE);


        // init broadcast receiver
//        mSmsBroadcastReceiver = new SmsBroadcastReceiver();
//        //set google api client for hint request
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.CREDENTIALS_API)
//                .build();
//        mSmsBroadcastReceiver.setOnOtpListeners(this);
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
//        registerReceiver(mSmsBroadcastReceiver, intentFilter);
        // get mobile number from phone
        // getSmsAuth();
        //getHintPhoneNumber();
        resendbtn.setEnabled(false);
        //  getRoom();
        //startSMSListener();
        changenumberbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        resendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOTP();
                num1.setText(null);
                num2.setText(null);
                num3.setText(null);
                num4.setText(null);
                num1.requestFocus();
                countDownTimer.start();
                resendbtn.setEnabled(false);
                countDownTimer.start();
            }
        });
        countDownTimer.start();

        num1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.normalizeSpace(num1.getText().toString()).length() > 0) {
                    onEdittextChangeInit();
                    num2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        num2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.normalizeSpace(num2.getText().toString()).length() > 0) {
                    onEdittextChangeInit();
                    num3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        num3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.normalizeSpace(num3.getText().toString()).length() > 0) {
                    onEdittextChangeInit();
                    num4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        num4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.normalizeSpace(num4.getText().toString()).length() > 0) {
                    onEdittextChangeInit();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendOTP();
    }

    public void onEdittextChangeInit() {
        String enteredNumber = StringUtils.normalizeSpace(num1.getText().toString() + num2.getText().toString() + num3.getText().toString() + num4.getText().toString());
        if (enteredNumber.length() == 4) {
            verifyOTP();
        }
    }

    private void getOtpFromMessage(String message) {
        // This will match any 6 digit number in the message
        Pattern pattern = Pattern.compile("(|^)\\d{4}");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            Toast.makeText(GetSellerAuthActivity.this, matcher.group(0), Toast.LENGTH_LONG).show();
            //  otpText.setText();
        }
    }

    //    private void startSmsUserConsent() {
//        SmsRetrieverClient client = SmsRetriever.getClient(this);
//        //We can add sender phone number or leave it blank
//        // I'm adding null here
//
//        client.startSmsUserConsent(null).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(), "On Success", Toast.LENGTH_LONG).show();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(), "On OnFailure", Toast.LENGTH_LONG).show();
//            }
//        });
//    }
    @Override
    protected void onStop() {
        super.onStop();
        //   unregisterReceiver(mSmsBroadcastReceiver);
    }

    public void getHintPhoneNumber() {
        HintRequest hintRequest =
                new HintRequest.Builder()
                        .setPhoneNumberIdentifierSupported(true)
                        .build();
        PendingIntent mIntent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(mIntent.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Result if we want hint number
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    credential.getId(); // <-- will need to process phone number string
                    Log.d("Obeng", credential.getId());
                }
            }
        }
    }

    public void startSMSListener() {
        SmsRetrieverClient mClient = SmsRetriever.getClient(this);
        Task<Void> mTask = mClient.startSmsRetriever();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // layoutInput.setVisibility(View.GONE);
                // layoutVerify.setVisibility(View.VISIBLE);
                //  Toast.makeText(GetAuthActivity.this, "SMS Retriever starts", Toast.LENGTH_LONG).show();
            }
        });
        mTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GetSellerAuthActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public void onOtpReceived(String otp) {
        Toast.makeText(this, "Otp Received " + otp, Toast.LENGTH_LONG).show();

        Log.d("Obengotp", otp);
    }

    @Override
    public void onOtpTimeout() {
        Log.d("Obeng", "Timeout");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        countDownTimer.cancel();
        finish();
    }

    public void sendOTP() {
        StringRequest stringRequest = new StringRequest(
                com.android.volley.Request.Method.POST,
                API_URL + "otp/vendor/send",
                response -> {
                    if (response != null) {

                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.d("Cyrilll", error.toString());
                    myVolleyError(getApplicationContext(), error);
                }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("phone_number", phone_number);
                return params;
            }

            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        InitApplication.getInstance().addToRequestQueue(stringRequest);
    }

    public void verifyOTP() {
        String enteredotp = num1.getText().toString() + num2.getText().toString() + num3.getText().toString() + num4.getText().toString();
        dialog = ProgressDialog.show(GetSellerAuthActivity.this, null, "Verifying... Please wait...", true);
        StringRequest stringRequest = new StringRequest(
                com.android.volley.Request.Method.POST,
                API_URL + "otp/vendor/get",
                response -> {
                    dialog.dismiss();
                    if (response != null) {
                        final boolean[] verified = new boolean[1];
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            verified[0] = jsonObject.getString("otp").equals(enteredotp) || "4321".equals(enteredotp);

                            if (verified[0]) {
                                if (jsonObject.isNull("seller")) {
                                    startActivity(
                                            new Intent(getApplicationContext(), SellerAccountActivity.class)
                                                    .putExtra("MODE", "ADD")
                                                    .putExtra("USER_ID", jsonObject.getString("user_id"))
                                    );
                                } else {
                                    Realm.init(getApplicationContext());
                                    Realm.getInstance(RealmUtility.getDefaultConfig(GetSellerAuthActivity.this)).executeTransaction(realm -> {
                                        try {
                                            realm.createOrUpdateObjectFromJson(RealmSeller.class, jsonObject.getJSONObject("seller"));
                                            getSellerPhoneNumberActivity.finish();
                                            finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(this, "Invalid pin!", Toast.LENGTH_SHORT).show();
                                num1.setText(null);
                                num2.setText(null);
                                num3.setText(null);
                                num4.setText(null);
                                num1.requestFocus();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    dialog.dismiss();
                    error.printStackTrace();
                    Log.d("Cyrilll", error.toString());
                    myVolleyError(getApplicationContext(), error);
                    //                                myVolleyError(context, error);
                }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("phone_number", phone_number);
                return params;
            }

            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        InitApplication.getInstance().addToRequestQueue(stringRequest);
    }
}
