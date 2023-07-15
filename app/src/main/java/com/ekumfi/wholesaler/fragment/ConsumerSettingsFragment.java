package com.ekumfi.wholesaler.fragment;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;
import static com.ekumfi.wholesaler.receiver.NetworkReceiver.activeActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.activity.HelpActivity;
import com.ekumfi.wholesaler.activity.MapsActivity;
import com.ekumfi.wholesaler.activity.ConsumerOrdersActivity;
import com.ekumfi.wholesaler.activity.ConsumerPaymentActivity;
import com.ekumfi.wholesaler.activity.SelectRoleActivity;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.activity.ConsumerAccountActivity;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmCustomer2;
import com.ekumfi.wholesaler.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import it.beppi.tristatetogglebutton_library.TriStateToggleButton;


/**
 * Created by Nana on 11/26/2017.
 */

public class ConsumerSettingsFragment extends Fragment {

    public static final String ISNIGHTMODE = "ISNIGHTMODE";
    private static final int PICKFILE_REQUEST_CODE = 327;

    CardView conferencecallno, paymentsbtn, profilebtn, displaybtn, availabilitybtn, logout, faqs, webportal, orders;
    LinearLayout detailnightmode, detailavailability;
    ImageView displayright, displayright_availability;
    Switch aSwitch;
    TriStateToggleButton availabilityswitch;
    ProgressDialog dialog;
    TextView availabilitytextview;

    double longitude = 0.0d;
    double latitude = 0.0d;

    public static Callbacks mCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_consumer_settings, container, false);
        paymentsbtn = rootView.findViewById(R.id.paymentsbtn);
        profilebtn = rootView.findViewById(R.id.profilebtn);
        displaybtn = rootView.findViewById(R.id.displaybtn);
        availabilitybtn = rootView.findViewById(R.id.availabilitybtn);
        detailnightmode = rootView.findViewById(R.id.detailnightmode);
        detailavailability = rootView.findViewById(R.id.detailavailability);
        displayright = rootView.findViewById(R.id.displayright);
        displayright_availability = rootView.findViewById(R.id.displayright_availability);
        faqs = rootView.findViewById(R.id.faqs);
        orders = rootView.findViewById(R.id.orders);
        aSwitch = rootView.findViewById(R.id.day_night_switch);
        availabilityswitch = rootView.findViewById(R.id.availabilityswitch);
        logout = rootView.findViewById(R.id.logout);
        webportal = rootView.findViewById(R.id.webportal);
        availabilitytextview = rootView.findViewById(R.id.availabilitytextview);

        webportal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("http://41.189.178.40:55554");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), getString(R.string.page_not_found), Toast.LENGTH_LONG).show();
                }
            }
        });

        profilebtn.setOnClickListener(view -> {
            clickview(view);

            startActivity(
                    new Intent(getActivity(), ConsumerAccountActivity.class)
                            .putExtra("MODE", "EDIT")
            );
        });

        faqs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        orders.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), ConsumerOrdersActivity.class));
        });

        logout.setOnClickListener(view -> {
            PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putString("com.ekumfi.wholesaler" + "ROLE", "")
                    .apply();
            startActivity(new Intent(getContext(), SelectRoleActivity.class));
            getActivity().finish();
        });

        paymentsbtn.setOnClickListener(view -> {
            clickview(view);
            startActivity(new Intent(getActivity(), ConsumerPaymentActivity.class));
        });

        displaybtn.setOnClickListener(view -> {
            clickview(view);
            aSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ISNIGHTMODE, false));
            if (detailnightmode.getVisibility() == View.VISIBLE) {
                detailnightmode.setVisibility(View.GONE);
                displayright.setImageResource(R.drawable.right);
            } else {
                detailnightmode.setVisibility(View.VISIBLE);
                displayright.setImageResource(R.drawable.arrowdown);
            }
        });

        aSwitch.setOnClickListener(v -> {
            PreferenceManager
                    .getDefaultSharedPreferences(getContext())
                    .edit()
                    .putBoolean("com.ekumfi.wholesaler" + ISNIGHTMODE, !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ISNIGHTMODE, false))
                    .apply();

            mCallbacks.onChangeNightMOde();
        });

        /*availabilitybtn.setOnClickListener(view -> {
            clickview(view);
            Realm.init(getContext());
            String availability = Realm.getInstance(RealmUtility.getDefaultConfig(getContext())).where(RealmConsumer.class).equalTo("consumer_id", PreferenceManager.getDefaultSharedPreferences(getContext()).getString("com.ekumfi.wholesaler" + "CONSUMERID", "")).findFirst().getAvailability();

            availabilitytextview.setText(availability);
            switch (availability) {
                case "Closed":
                    availabilityswitch.setToggleStatus(0, true);
                    break;
                case "Busy":
                    availabilityswitch.setToggleStatus(1, true);
                    break;
                case "Available":
                    availabilityswitch.setToggleStatus(2, true);
                    break;
            }



            if (detailavailability.getVisibility() == View.VISIBLE) {
                detailavailability.setVisibility(View.GONE);
                displayright_availability.setImageResource(R.drawable.collapse);
            } else {
                detailavailability.setVisibility(View.VISIBLE);
                displayright_availability.setImageResource(R.drawable.expand);
            }
        });

        availabilityswitch.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                Realm.init(getContext());
                RealmConsumer realmConsumer = Realm.getInstance(RealmUtility.getDefaultConfig(getContext())).where(RealmConsumer.class).equalTo("consumer_id", PreferenceManager.getDefaultSharedPreferences(getContext()).getString("com.ekumfi.wholesaler" + "CONSUMERID", "")).findFirst();
                String consumer_id = realmConsumer.getConsumer_id();

                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        API_URL + "consumers/" + consumer_id,
                        response -> {
                            dialog.dismiss();
                            if (response != null) {
                                JSONObject jsonObjectResponse = null;
                                try {
                                    jsonObjectResponse = new JSONObject(response);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Realm.init(getActivity());
                                JSONObject finalJsonObjectResponse = jsonObjectResponse;
                                Realm.getInstance(RealmUtility.getDefaultConfig(activeActivity)).executeTransaction(realm -> {
                                    realm.createOrUpdateObjectFromJson(RealmConsumer.class, finalJsonObjectResponse);

                                    switch (toggleIntValue) {
                                        case 0:
                                            availabilitytextview.setText("Closed");
                                            break;
                                        case 1:
                                            availabilitytextview.setText("Busy");
                                            break;
                                        case 2:
                                            availabilitytextview.setText("Available");
                                            break;
                                    }
                                    Toast.makeText(activeActivity, "Availability status successfully updated!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        },
                        error -> {
                            dialog.dismiss();
                            error.printStackTrace();
                            Log.d("Cyrilll", error.toString());
                            myVolleyError(activeActivity, error);
                        }
                ){
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params  = new HashMap<>();
                        switch (toggleIntValue) {
                            case 0:
                                params.put("availability", "Closed");
                                break;
                            case 1:
                                params.put("availability", "Busy");
                                break;
                            case 2:
                                params.put("availability", "Available");
                                break;
                        }
                        return params;
                    }
                    *//** Passing some request headers* *//*
                    @Override
                    public Map getHeaders() throws AuthFailureError {
                        HashMap headers = new HashMap();
                        headers.put("accept", "application/json");
                        headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(activeActivity).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                        return headers;
                    }
                };;

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                InitApplication.getInstance().addToRequestQueue(stringRequest);
            }
        });*/

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks
//        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MapsActivity.RC_CONFIRM_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            longitude = data.getDoubleExtra("LONGITUDE", 0.0d);
                            latitude = data.getDoubleExtra("LATITUDE", 0.0d);
                            Log.d("sdffds0990xc", String.valueOf(longitude) + "  " + String.valueOf(latitude));
                            final Map<String, String>[] params = new Map[]{new HashMap<>()};
                            final String[] customer_id = new String[1];
                            Realm.init(getActivity());
                            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                RealmCustomer2 realmCustomer = realm.where(RealmCustomer2.class).findFirst();
                                customer_id[0] = realmCustomer.getCustomer_id();
                                realmCustomer.setLongitude(longitude);
                                realmCustomer.setLatitude(latitude);

                                params[0] = new HashMap<>();
                                params[0].put("name", realmCustomer.getName() == null ? "" : realmCustomer.getName());
                                params[0].put("gender", realmCustomer.getGender() == null ? "" : realmCustomer.getGender());
                                params[0].put("primary_contact", realmCustomer.getPrimary_contact() == null ? "" : realmCustomer.getPrimary_contact());
                                params[0].put("auxiliary_contact", realmCustomer.getAuxiliary_contact() == null ? "" : realmCustomer.getAuxiliary_contact());
                                params[0].put("location", realmCustomer.getStreet_address() == null ? "" : realmCustomer.getStreet_address());
                                params[0].put("longitude", String.valueOf(longitude));
                                params[0].put("latitude", String.valueOf(latitude));
                            });
                            ProgressDialog dialog = new ProgressDialog(getActivity());
                            dialog.setMessage("Updating location...");
                            dialog.setMessage("Please wait...");
                            dialog.setCancelable(false);
                            dialog.setIndeterminate(true);
                            dialog.show();



                            StringRequest stringRequest = new StringRequest(
                                    Request.Method.POST,
                                    API_URL + "customers/" + customer_id[0],
                                    response -> {
                                        dialog.dismiss();
                                        if (response != null) {
                                            JSONObject jsonObjectResponse = null;
                                            try {
                                                jsonObjectResponse = new JSONObject(response);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            Realm.init(getActivity());
                                            JSONObject finalJsonObjectResponse = jsonObjectResponse;
                                            Realm.getInstance(RealmUtility.getDefaultConfig(activeActivity)).executeTransaction(realm -> {
                                                realm.createOrUpdateObjectFromJson(RealmCustomer2.class, finalJsonObjectResponse);
                                                Toast.makeText(activeActivity, "Location successfully set!", Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    },
                                    error -> {
                                        dialog.dismiss();
                                        error.printStackTrace();
                                        Log.d("Cyrilll", error.toString());
                                        myVolleyError(activeActivity, error);
                                    }
                            ){
                                @Override
                                public Map<String, String> getParams() throws AuthFailureError {
                                    return params[0];
                                }
                                /** Passing some request headers* */
                                @Override
                                public Map getHeaders() throws AuthFailureError {
                                    HashMap headers = new HashMap();
                                    headers.put("accept", "application/json");
                                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(activeActivity).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                    return headers;
                                }
                            };;

                            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    0,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            InitApplication.getInstance().addToRequestQueue(stringRequest);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // some stuff that will happen if there's no result
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + resultCode);
                }
                break;
            default:
                break;

        }
    }

    private void clickview(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(v.getContext(), R.anim.click);
        v.startAnimation(animation1);

    }

    public interface Callbacks {
        //Callback for when button clicked.
        void onChangeNightMOde();
    }
}
