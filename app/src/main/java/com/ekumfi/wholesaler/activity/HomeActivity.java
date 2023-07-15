package com.ekumfi.wholesaler.activity;

import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.keyConst.GUID_WS_URL;
import static com.ekumfi.wholesaler.constants.Const.clearAppData;
import static com.ekumfi.wholesaler.constants.Const.isNetworkAvailable;
import static com.ekumfi.wholesaler.fragment.WholesalerProductsFragment.wholesalerProductMaterialDialog;
import static com.ekumfi.wholesaler.other.InitApplication.versionName;
import static com.ekumfi.wholesaler.receiver.NetworkReceiver.activeActivity;
import static com.ekumfi.wholesaler.util.Socket.EVENT_CLOSED;
import static com.ekumfi.wholesaler.util.Socket.EVENT_OPEN;
import static com.ekumfi.wholesaler.util.Socket.EVENT_RECONNECT_ATTEMPT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.fragment.WholesalerProductsFragment;
import com.ekumfi.wholesaler.realm.RealmStockCart;
import com.ekumfi.wholesaler.realm.RealmWholesaler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.greysonparrelli.permiso.PermisoActivity;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.fragment.ChatIndexFragment;
import com.ekumfi.wholesaler.fragment.SettingsFragment;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmAppUserFee;
import com.ekumfi.wholesaler.realm.RealmCourse;
import com.ekumfi.wholesaler.realm.RealmDialcode;
import com.ekumfi.wholesaler.realm.RealmEnrolment;
import com.ekumfi.wholesaler.realm.RealmInstitution;
import com.ekumfi.wholesaler.realm.RealmInstructor;
import com.ekumfi.wholesaler.realm.RealmInstructorCourse;
import com.ekumfi.wholesaler.realm.RealmPayment;
import com.ekumfi.wholesaler.realm.RealmPeriod;
import com.ekumfi.wholesaler.realm.RealmSeller;
import com.ekumfi.wholesaler.realm.RealmStudent;
import com.ekumfi.wholesaler.realm.RealmTimetable;
import com.ekumfi.wholesaler.realm.RealmUser;
import com.ekumfi.wholesaler.receiver.NetworkReceiver;
import com.ekumfi.wholesaler.util.FCMAsyncTask;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.ekumfi.wholesaler.util.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class HomeActivity extends PermisoActivity implements SettingsFragment.Callbacks {

    private String TAG;
    public static String MYUSERID = "MYUSERID";
    public static String APITOKEN = "APITOKEN";
    public static String NUMBER_VERIFIED = "NUMBER_VERIFIED";
    public static String ACCESSTOKEN = "ACCESSTOKEN";
    public static String GUID = "GUID";
    public static String JUSTENROLLED = "JUSTENROLLED";
    public static int RC_ACCOUNT = 435;
    public static final  int FILE_PICKER_REQUEST_CODE = 4389;
    NetworkReceiver networkReceiver;
    static BottomNavigationView navigation;
    FloatingActionButton close;
    public static Context context;
    public static Activity homeactivity;
    private static Socket guidSocket;
    public static RealmSeller realmSeller = new RealmSeller();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        public Fragment fragment;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.banners) {/*fragment = new BannersFragment();
                    loadFragment(fragment);*/
                return true;
            } else if (itemId == R.id.product) {
                fragment = new WholesalerProductsFragment();
                loadFragment(fragment);
                return true;
            } else if (itemId == R.id.chat) {
                fragment = new ChatIndexFragment();
                loadFragment(fragment);
                return true;
            } else if (itemId == R.id.navigation_settings) {
                fragment = new SettingsFragment();
                loadFragment(fragment);
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        homeactivity = this;
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        if (InitApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_home);

        guidSocket = Socket
                .Builder.with(GUID_WS_URL)
                .build();
        guidSocket.connect();

        guidSocket.onEvent(EVENT_OPEN, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket2", "Connected");

                guidSocket.join("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + MYUSERID, ""));

                guidSocket.onEventResponse("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + MYUSERID, ""), new Socket.OnEventResponseListener() {
                    @Override
                    public void onMessage(String event, String data) {

                    }
                });

                guidSocket.setMessageListener(new Socket.OnMessageListener() {
                    @Override
                    public void onMessage(String data) {
                        JSONObject jsonObject = null;
                        JSONObject jsonResponse = null;
                        String message = "";
                        try {
                            jsonObject = new JSONObject(data);
                            switch (jsonObject.getInt("t")) {
                                case 0:
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                                case 5:
                                    break;
                                case 6:
                                    break;
                                case 7:
                                    jsonResponse = jsonObject.getJSONObject("d");
                                    Log.d("mywebsocket2", jsonResponse.toString());
                                    Realm.init(activeActivity);
                                    JSONObject finalJsonResponse = jsonResponse;
                                    if (finalJsonResponse.getJSONObject("data").has("guid")) {
                                        if (!PreferenceManager.getDefaultSharedPreferences(activeActivity).getString("com.ekumfi.wholesaler" + GUID, "").equals("") && !finalJsonResponse.getJSONObject("data").getString("guid").equals(PreferenceManager.getDefaultSharedPreferences(activeActivity).getString("com.ekumfi.wholesaler" + GUID, ""))) {
                                            Log.d("d7410852", "local: " + PreferenceManager.getDefaultSharedPreferences(activeActivity).getString("com.ekumfi.wholesaler" + GUID, "") + "server : " + finalJsonResponse.getJSONObject("data").getString("guid"));
                                            AlertDialog.Builder builder = new AlertDialog.Builder(activeActivity);
                                            builder.setTitle("Duplicate Account Detected");
                                            builder.setMessage("You can only use your account on one device at a time.");
                                            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                                clearAppData(activeActivity);
                                            });
                                            builder
                                                    .setCancelable(false)
                                                    .show();
                                        }
                                    }
                                    break;
                                case 8:
                                    break;
                                case 9:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                try {
                    JSONObject jsonData = new JSONObject()
                            .put(
                                    "guid", PreferenceManager.getDefaultSharedPreferences(homeactivity).getString("com.ekumfi.wholesaler" + GUID, "")
                            );
                    if (isNetworkAvailable(homeactivity)) {

                        if (guidSocket.getState() == Socket.State.OPEN) {
                            if (guidSocket != null) {
                                guidSocket.send("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + MYUSERID, ""), jsonData.toString());
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        guidSocket.onEvent(EVENT_RECONNECT_ATTEMPT, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket2", "reconnecting");
            }
        });
        guidSocket.onEvent(EVENT_CLOSED, new Socket.OnEventListener() {
            @Override
            public void onMessage(String event) {
                Log.d("mywebsocket2", "connection closed");
            }
        });

        new FCMAsyncTask(getApplicationContext()).execute();

        FirebaseMessaging.getInstance().subscribeToTopic(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + MYUSERID, ""))
                .addOnCompleteListener(task -> {
                    String msg = "successfully subscribed";
                    if (!task.isSuccessful()) {
                        msg = "unsuccessfully subscribed";
                    }
                    Log.d("engineer:sub_status:", msg);
                });

        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(HomeActivity.this)).executeTransaction(realm -> {
            RealmResults<RealmStockCart> realmStockCarts = realm.where(RealmStockCart.class)
                    .distinct("seller_id")
                    .findAll();
            for (RealmStockCart realmStockCart : realmStockCarts) {
                FirebaseMessaging.getInstance().subscribeToTopic(realmStockCart.getSeller_id())
                        .addOnCompleteListener(task -> {
                            String msg = "successfully subscribed";
                            if (!task.isSuccessful()) {
                                msg = "unsuccessfully unsubscribed";
                            }
                            Log.d("engineer:sub_status:", msg);
                        });
            }
        });


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d("engineer", token);
                        retriev_current_registration_token(getApplicationContext(), token);
                    }
                });


        loadFragment(new WholesalerProductsFragment());
        //navigation.setSelectedItemId(R.id.navigation_home);

        networkReceiver = new NetworkReceiver();


        navigation = findViewById(R.id.navigation);
        //BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public static void guidCheck(Context context) {

        if (!PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + GUID, "").equals("")) {
            try {
                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        API_URL + "guid-check",
                        response -> {
                            if (response != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Log.d("d7410852", Boolean.toString(jsonObject.getBoolean("guid_changed")) + " : " + PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + GUID, ""));
                                    if (jsonObject.getBoolean("guid_changed")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Duplicate Account Detected");
                                        builder.setMessage("You can only use your account on one device at a time.");
                                        builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                            clearAppData(context);
                                        });
                                        builder
                                                .setCancelable(false)
                                                .show();
                                    }
//                                    new broadcastWithFirebase().execute();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            Log.d("Cyrilll", error.toString());
                            //                                myVolleyError(context, error);
                        }
                ) {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("guid", PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + GUID, ""));
                        return params;
                    }

                    @Override
                    public Map getHeaders() throws AuthFailureError {
                        HashMap headers = new HashMap();
                        headers.put("accept", "application/json");
                        headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                        return headers;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                InitApplication.getInstance().addToRequestQueue(stringRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (guidSocket != null) {
            guidSocket.leave("guid:" + PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + MYUSERID, ""));
            guidSocket.clearListeners();
            guidSocket.close();
            guidSocket.terminate();
            guidSocket = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Realm.init(getApplicationContext());
        RealmWholesaler realmWholesaler = Realm.getInstance(RealmUtility.getDefaultConfig(getApplicationContext())).where(RealmWholesaler.class).findFirst();

        if (realmWholesaler == null) {
            startActivity(
                    new Intent(getApplicationContext(), WholesalerAccountActivity.class)
                            .putExtra("MODE", "ADD")
                            .putExtra("USER_ID", PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + MYUSERID, ""))
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case 1815:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            String product_id = data.getStringExtra("PRODUCT_ID");
                            wholesalerProductMaterialDialog.setProduct_id(product_id);
                            wholesalerProductMaterialDialog.setName(data.getStringExtra("NAME"));

                            wholesalerProductMaterialDialog.name_textview.setText(data.getStringExtra("NAME"));
                            wholesalerProductMaterialDialog.name_textview.setError(null);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // some stuff that will happen if there's no result
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.homeframe, fragment);
        transaction.commit();
    }


    @Override
    public void onChangeNightMOde() {
        if (InitApplication.getInstance().isNightModeEnabled()) {
            InitApplication.getInstance().setIsNightModeEnabled(false);
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);

        } else {
            InitApplication.getInstance().setIsNightModeEnabled(true);
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);
        }

    }

    public static void dialog(Context context, boolean value) {

        if (value) {
            //   tv_check_connection.setVisibility(View.VISIBLE);

        } else {
            Snackbar snackbar = Snackbar
                    .make(navigation, context.getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.ok).toUpperCase(), view -> {

                    });

            snackbar.show();
        }
    }

    public static void versionCheck(Context context) {
        try {
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    API_URL + "student-project-info",
                    response -> {
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getString("version").equals(versionName)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder((AppCompatActivity)context);
                                    builder.setTitle("Critical Update Available!");
                                    builder.setMessage("Update app to continue using this app.");
                                    builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.univirtual.student"));
                                        context.startActivity(i);
                                    })
                                    .setCancelable(false)
                                    .show();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.d("Cyrilll", error.toString());
                        //                                myVolleyError(context, error);
                    }
            ) {
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchAllMyData(Context context) {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL + "all-data",
                    null,
                    responseJson -> {
                        if (responseJson != null) {
                            Realm.init(context);
                            Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
                                try {
                                    persistAll(realm, responseJson);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    },
                    error -> {

                    }
            ){
                /** Passing some request headers* */
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                    return headers;
                }
            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void persistAll(Realm realm, JSONObject responseJson) throws JSONException {
//        realm.createOrUpdateAllFromJson(RealmAssignment.class, responseJson.getJSONArray("assignments"));
//        realm.createOrUpdateAllFromJson(RealmAttendance.class, responseJson.getJSONArray("attendances"));
//        realm.createOrUpdateAllFromJson(RealmAudio.class, responseJson.getJSONArray("audios"));
//        realm.createOrUpdateAllFromJson(RealmChat.class, responseJson.getJSONArray("chats"));
        realm.createOrUpdateAllFromJson(RealmCourse.class, responseJson.getJSONArray("courses"));
        realm.createOrUpdateAllFromJson(RealmEnrolment.class, responseJson.getJSONArray("enrolments"));
        realm.createOrUpdateAllFromJson(RealmInstructor.class, responseJson.getJSONArray("instructors"));
        realm.createOrUpdateAllFromJson(RealmInstructorCourse.class, responseJson.getJSONArray("instructor_courses"));
        realm.createOrUpdateAllFromJson(RealmPayment.class, responseJson.getJSONArray("payments"));
        realm.createOrUpdateAllFromJson(RealmInstitution.class, responseJson.getJSONArray("institutions"));
        realm.createOrUpdateAllFromJson(RealmStudent.class, responseJson.getJSONArray("students"));
//        realm.createOrUpdateAllFromJson(RealmSubmittedAssignment.class, responseJson.getJSONArray("submitted_assignments"));
        realm.createOrUpdateAllFromJson(RealmUser.class, responseJson.getJSONArray("users"));
        realm.createOrUpdateAllFromJson(RealmTimetable.class, responseJson.getJSONArray("timetables"));
        realm.createOrUpdateAllFromJson(RealmPeriod.class, responseJson.getJSONArray("periods"));
//        realm.createOrUpdateAllFromJson(RealmInstructorCourseRating.class, responseJson.getJSONArray("instructor_course_ratings"));
//        realm.createOrUpdateAllFromJson(RealmQuiz.class, responseJson.getJSONArray("quizzes"));
//        realm.createOrUpdateAllFromJson(RealmSubmittedQuiz.class, responseJson.getJSONArray("submitted_quizzes"));
        realm.createOrUpdateAllFromJson(RealmDialcode.class, responseJson.getJSONArray("dialcodes"));
//        realm.createOrUpdateAllFromJson(RealmRecordedVideo.class, responseJson.getJSONArray("recorded_videos"));
//        realm.createOrUpdateAllFromJson(RealmRecordedVideoStream.class, responseJson.getJSONArray("recorded_video_streams"));
//        realm.createOrUpdateAllFromJson(RealmRecordedAudioStream.class, responseJson.getJSONArray("recorded_audio_streams"));
        realm.createOrUpdateAllFromJson(RealmAppUserFee.class, responseJson.getJSONArray("app_user_fees"));
//        realm.createOrUpdateAllFromJson(RealmDrawingCoordinate.class, responseJson.getJSONArray("drawing_coordinates"));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String getDefaultDialerPackage(Context context) {
        TelecomManager manger= null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manger = (TelecomManager) context.getSystemService(TELECOM_SERVICE);
        }
        String name=manger.getDefaultDialerPackage();
        return name;
    }

    public static void retriev_current_registration_token(Context context, String confirmation_token) {
        JSONObject request = new JSONObject();

        try {
            request.put("confirmation_token", confirmation_token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                API_URL + "update-confirmation-token/" + PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + MYUSERID, ""),
                request,
                response -> {
                },
                error -> {
                }
        ) {
            /** Passing some request headers* */

            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                return headers;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        InitApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public static class broadcastWithFirebase extends AsyncTask<Void, Integer, String> {


        // private ProgressDialog progressDialog;
        private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
        private static final String[] SCOPES = {MESSAGING_SCOPE};

        @Override
        protected String doInBackground(Void... params) {
            JSONObject jsonObject = null;
            try {
                JSONObject dataJsonObject = null;
                dataJsonObject = new JSONObject()
                        .put("guid", PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + GUID, ""));

                jsonObject = new JSONObject().put(
                        "message", new JSONObject()
                                .put("topic", PreferenceManager.getDefaultSharedPreferences(homeactivity).getString("com.ekumfi.wholesaler" + MYUSERID, ""))
                                /*.put("notification", new JSONObject()
                                        .put("body", jsonObject.getJSONObject("chat").has("attachmenturl") ? "Attachment" : jsonObject.getJSONObject("chat").getString("text"))
                                        .put("title", COURSEPATH + " Chat")
                                )*/
                                .put("data", dataJsonObject
                                )
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }


            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            String content = jsonObject.toString();
            RequestBody body = RequestBody.create(mediaType, content);
            okhttp3.Request request = new okhttp3.Request.Builder()

                    .url("https://fcm.googleapis.com/v1/projects/instructorapp-c6c95/messages:send")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(homeactivity).getString(ACCESSTOKEN, ""))
                    .build();
            try {
                okhttp3.Response response = client.newCall(request).execute();
                String s = response.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

            // Init and show dialog

        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
