package com.ekumfi.wholesaler.activity;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.keyConst.APP_HASH;
import static com.ekumfi.wholesaler.constants.Const.isNetworkAvailable;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ekumfi.wholesaler.realm.RealmAgent;
import com.ekumfi.wholesaler.realm.RealmBanner;
import com.ekumfi.wholesaler.realm.RealmChat;
import com.ekumfi.wholesaler.realm.RealmEkumfiInfo;
import com.ekumfi.wholesaler.realm.RealmProduct;
import com.ekumfi.wholesaler.realm.RealmSeller;
import com.ekumfi.wholesaler.realm.RealmStockCart;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;


public class SigninActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LogMeIn";
    static int modelsfetched = 0;
    ImageView passwordIcon;
    TextView register, forgotpassword;
    boolean passwordShow = false;
    Context context;
    private EditText emailField, passwordField;
    private Button login;
    private ProgressDialog mProgress;
    private static final int RC_SIGN_IN = 110;
    private GoogleApiClient mGoogleApiClient;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        context = getApplicationContext();

        FragmentManager fm = getSupportFragmentManager();

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Signing in...");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        login = findViewById(R.id.login);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        passwordIcon = findViewById(R.id.passwordIcon);


        // Add code to print out the key hash
        String appHash;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                appHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash:", appHash);
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {


        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignin();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        RequestData();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        mProgress.dismiss();
                        Log.d("sdf42ty", "onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        mProgress.dismiss();

                        if (exception instanceof FacebookAuthorizationException) {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();

                                if (isNetworkAvailable(context)) {
                                    mProgress.show();
                                    LoginManager.getInstance().logInWithReadPermissions(SigninActivity.this, Arrays.asList("public_profile", "email"));
                                } else {
                                    Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                                }
                            }
                            return;
                        }



                        AlertDialog.Builder builder = new AlertDialog.Builder(SigninActivity.this);
                        builder.setTitle("Error.");
                        builder.setMessage(exception.getLocalizedMessage());
                        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            dialog.dismiss();
                        }).show();
                        Log.d("sdf42ty", "facebook onError: " + exception.getLocalizedMessage());
                    }
                });

        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordShow = !passwordShow;
                if (passwordShow) {
                    passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hide_password);
                    passwordIcon.setImageBitmap(bitmap);
                } else {
                    passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.see_password);
                    passwordIcon.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void RequestData() {
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                final JSONObject json = response.getJSONObject();
                try {
                    if (json != null) {
                        if (!json.isNull("email")) {
                            Signin(json.getString("email"), true, json.getString("id"));
                        } else {
                            mProgress.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(SigninActivity.this);
                            builder.setTitle("Error Retrieving email");
                            builder.setMessage(Html.fromHtml("Unable to retrieve the email address associated with your facebook account.<br><br> <u>Possible Fixes:</u><br><br> 1. Link your facebook account to an email address. <br>2. Allow access to you email address in facebook settings."));
                            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getLocalizedMessage());
                    mProgress.dismiss();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void attemptSignin() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        boolean canLogin = true;

        if (TextUtils.isEmpty(email)) {
            emailField.setError(getString(R.string.error_field_required));
            canLogin = false;
        } else if (!isEmailValid(email)) {
            emailField.setError("Invalid email!");
            canLogin = false;
        } else {
            emailField.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError(getString(R.string.error_field_required));
            canLogin = false;
        } else {
            passwordField.setError(null);
        }

        if (canLogin) {
            mProgress.show();
            Signin(email, false, password);
        }
    }

    public void Signin(String email, boolean external_login, String password) {
        try {
            JSONObject request = new JSONObject();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;
            request.put("apphash", APP_HASH);
            request.put("osversion", System.getProperty("os.version"));
            request.put("sdkversion", Build.VERSION.SDK_INT);
            request.put("device", Build.DEVICE);
            request.put("devicemodel", Build.MODEL);
            request.put("deviceproduct", Build.PRODUCT);
            request.put("manufacturer", Build.MANUFACTURER);
            request.put("androidid", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            request.put("versionrelease", Build.VERSION.RELEASE);
            request.put("deviceheight", String.valueOf(heightPixels));
            request.put("devicewidth", String.valueOf(widthPixels));
            request.put("email", email);
            if (external_login) {
                request.put("external_login", true);
            }
            request.put("password", password);
            String guid = UUID.randomUUID().toString();
            request.put("guid", guid);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL + "admin-login",
                    request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            Toast.makeText(context, "Testing toast", Toast.LENGTH_SHORT).show();
                            //mProgress.dismiss();
                            if (response != null) {

                                try {
                                    if (response.has("user_not_found")) {
                                        mProgress.dismiss();
                                        Toast.makeText(context, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                                    } else if (response.has("incorrect_password")) {
                                        mProgress.dismiss();
                                        Toast.makeText(context, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                                    } else {

                                        Realm.init(getApplicationContext());
                                        Realm.getInstance(RealmUtility.getDefaultConfig(SigninActivity.this)).executeTransaction(realm -> {
                                            try {
                                                realm.createOrUpdateAllFromJson(RealmEkumfiInfo.class, response.getJSONArray("ekumfi_infos"));
                                                realm.createOrUpdateAllFromJson(RealmSeller.class, response.getJSONArray("sellers"));
                                                realm.createOrUpdateAllFromJson(RealmAgent.class, response.getJSONArray("agents"));
                                                realm.createOrUpdateAllFromJson(RealmBanner.class, response.getJSONArray("banners"));
                                                realm.createOrUpdateAllFromJson(RealmProduct.class, response.getJSONArray("products"));
                                                realm.createOrUpdateAllFromJson(RealmStockCart.class, response.getJSONArray("stock_carts"));
                                                realm.createOrUpdateAllFromJson(RealmChat.class, response.getJSONArray("ekumfi_chats"));

                                                PreferenceManager
                                                        .getDefaultSharedPreferences(getApplicationContext())
                                                        .edit()
                                                        .putString("com.ekumfi.wholesaler" + APITOKEN, response.getString("api_token"))
                                                        .apply();

                                                mProgress.dismiss();
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                finish();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }
                                } catch (Throwable t) {
//                                    Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
                                    Log.d("My App", "Could not parse malformed JSON: " + t.toString());
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mProgress.dismiss();
                            myVolleyError(context, error);
                        }
                    }
            );
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            mProgress.dismiss();
            e.printStackTrace();
        }
    }


    public static boolean isEmailValid(String email) {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        return matcher.matches();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("bbbbb", "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct == null) {
                mProgress.dismiss();
                Log.d("sdf42ty", "Google onError: " + result);
                Toast.makeText(context, "Signin error!", Toast.LENGTH_SHORT).show();
            } else {
                Signin(acct.getEmail(), true, acct.getId());
            }
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    public class SendEmailVerificationLinkAsync extends AsyncTask<String, Void, String> {

        private Context context;

        public SendEmailVerificationLinkAsync(Context context) {
            this.context = context;
        }

        protected void onPreExecute() {
            mProgress.setMessage("Please wait...");
            mProgress.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            mProgress.dismiss();
            try {
                String email = arg0[0];

                String link = API_URL + "resend-verification-link";
                String data = URLEncoder.encode("email", "UTF-8") + "=" +
                        URLEncoder.encode(email, "UTF-8");


                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                return sb.toString();
            } catch (Exception e) {
                return "" + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                if (result.equals("0")) {
                    Toast.makeText(context, "Email verification link successfully sent.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}