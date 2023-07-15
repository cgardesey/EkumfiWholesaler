package com.ekumfi.wholesaler.materialDialog;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.activity.MessageActivity;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmChat;
import com.ekumfi.wholesaler.realm.RealmEkumfiInfo;
import com.ekumfi.wholesaler.realm.RealmSeller;
import com.ekumfi.wholesaler.util.RealmUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ChooseServiceContactMethodMaterialDialog extends DialogFragment {
    public static ProgressDialog dialog1;
    private static final String MY_LOGIN_ID = "MY_LOGIN_ID";
    LinearLayout chat, call;

    String seller_id;
    String consumer_id;
    String order_id;

    public String getConsumer_id() {
        return consumer_id;
    }

    public void setConsumer_id(String consumer_id) {
        this.consumer_id = consumer_id;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_choose_contact_method, null);
        chat = view.findViewById(R.id.chat);
        call = view.findViewById(R.id.call);

        consumer_id = "";

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();
                final String[] seller_name = new String[1];
                final String[] profile_image_url = new String[1];
                final String[] availability = new String[1];

                Realm.init(getActivity());
                final RealmSeller[] realmSeller = {Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmSeller.class).equalTo("seller_id", seller_id).findFirst()};
                seller_name[0] = realmSeller[0].getShop_name();
                profile_image_url[0] = realmSeller[0].getShop_image_url();
                availability[0] = realmSeller[0].getAvailability();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        API_URL + "ekumfi-chat-data",
                        response -> {
                            if (response != null) {
                                dialog.dismiss();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Realm.init(getActivity());
                                    Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                        try {
                                            realmSeller[0] = realm.createOrUpdateObjectFromJson(RealmSeller.class, jsonObject.getJSONObject("seller"));
                                            realm.createOrUpdateAllFromJson(RealmChat.class, jsonObject.getJSONArray("chats"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        seller_name[0] = realmSeller[0].getShop_name();
                                        profile_image_url[0] = realmSeller[0].getShop_image_url();
                                        availability[0] = realmSeller[0].getAvailability();
                                    });

                                    startActivity(new Intent(getActivity(), MessageActivity.class)
                                            .putExtra("SELLER_ID", seller_id)
                                            .putExtra("SELLER_NAME", seller_name[0])
                                            .putExtra("PROFILE_IMAGE_URL", profile_image_url[0])
                                            .putExtra("AVAILABILITY", availability[0])
                                    );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            startActivity(new Intent(getActivity(), MessageActivity.class)
                                    .putExtra("SELLER_ID", seller_id)
                                    .putExtra("SELLER_NAME", seller_name[0])
                                    .putExtra("PROFILE_IMAGE_URL", profile_image_url[0])
                                    .putExtra("AVAILABILITY", availability[0])
                            );
                            dialog.dismiss();
                            Log.d("Cyrilll", error.toString());
                        }
                ) {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("seller_id", seller_id);
                        params.put("consumer_id", "");
                        Realm.init(getActivity());
                        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                            RealmResults<RealmChat> results = realm.where(RealmChat.class)
                                    .sort("id", Sort.DESCENDING)
                                    .equalTo("seller_id", seller_id)
                                    .equalTo("consumer_id", "")
                                    .findAll();
                            ArrayList<RealmChat> myArrayList = new ArrayList<>();
                            for (RealmChat realmChat : results) {
                                if (!(realmChat.getChat_id().startsWith("z"))) {
                                    myArrayList.add(realmChat);
                                }
                            }
                            if (results.size() == 1) {
                                params.put("id", "0");
                            }
                            else{
                                params.put("id", String.valueOf(myArrayList.get(0).getId()));
                            }
                        });
                        return params;
                    }
                    /** Passing some request headers* */
                    @Override
                    public Map getHeaders() throws AuthFailureError {
                        HashMap headers = new HashMap();
                        headers.put("accept", "application/json");
                        headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                        return headers;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                InitApplication.getInstance().addToRequestQueue(stringRequest);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                CallSellerMaterialDialog callProviderMaterialDialog = new CallSellerMaterialDialog();
                if (callProviderMaterialDialog != null && callProviderMaterialDialog.isAdded()) {

                } else {
                    String primary_contact;
                    Realm.init(getActivity());
                    primary_contact = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmEkumfiInfo.class).findFirst().getPrimary_contact();
                    callProviderMaterialDialog.setPhone_number(primary_contact);
                    callProviderMaterialDialog.setConsumer_id("");
                    callProviderMaterialDialog.setSeller_id(seller_id);
                    callProviderMaterialDialog.show(getFragmentManager(), "");
                }
            }
        });
        // doneBtn.setOnClickListener(doneAction);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });
            }
        }, 5);
        return builder.create();

    }


}