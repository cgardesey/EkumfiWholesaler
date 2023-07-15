package com.ekumfi.wholesaler.fragment;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.activity.MessageActivity;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.activity.ChooseSellerIndexActivity;
import com.ekumfi.wholesaler.adapter.ChatIndexAdapter;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmChat;
import com.ekumfi.wholesaler.realm.RealmSeller;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ChatIndexFragment extends Fragment {
    static RecyclerView recyclerview;
    static TextView no_data;
    static ChatIndexAdapter chatIndexAdapter;
    static ArrayList<RealmChat> cartArrayList = new ArrayList<>();
    static ArrayList<RealmChat> newCart = new ArrayList<>();
    public static Activity chatIndexFragmentContext;
    FloatingActionButton fab;

    public ChatIndexFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatIndexFragmentContext = getActivity();
        final View rootView = inflater.inflate(R.layout.fragment_chat_index, container, false);
        recyclerview = rootView.findViewById(R.id.recyclerview);
        no_data = rootView.findViewById(R.id.no_data);
        fab = rootView.findViewById(R.id.fab);

        chatIndexAdapter = new ChatIndexAdapter(new ChatIndexAdapter.ChatIndexAdapterInterface() {

            @Override
            public void onItemClick(ArrayList<RealmChat> realmChats, int position, ChatIndexAdapter.ViewHolder holder) {
                RealmChat realmChat = realmChats.get(position);
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();
                final String[] seller_name = new String[1];
                final String[] profile_image_url = new String[1];
                final String[] availability = new String[1];
                String seller_id = realmChat.getSeller_id();

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
                            if (results.size() < 3) {
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

            @Override
            public void onImageClick(ArrayList<RealmChat> realmChats, int position, ChatIndexAdapter.ViewHolder holder) {

            }
        }, getActivity(), cartArrayList);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(chatIndexAdapter);


        fab.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChooseSellerIndexActivity.class));
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateChatIndex(getActivity());

        StringRequest stringRequest = new StringRequest(
                com.android.volley.Request.Method.POST,
                API_URL + "scoped-latest-ekumfi-chats",
                response -> {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            Realm.init(getActivity());
                            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                realm.createOrUpdateAllFromJson(RealmChat.class, jsonArray);
                            });
                            populateChatIndex(getActivity());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.d("Cyrilll", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("seller_id", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + "SELLER_ID", ""));
                return params;
            }

             /*Passing some request headers*/
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

    public static void populateChatIndex(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmChat> results;

            results = realm.where(RealmChat.class)
                    .sort("id", Sort.DESCENDING)
                    .distinct("seller_id")
                    .findAll();

            if (results.size() < 1) {
                no_data.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
            else {
                no_data.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            }
            newCart.clear();
            for (RealmChat realmChat : results) {
                newCart.add(realmChat);
            }
            cartArrayList.clear();
            cartArrayList.addAll(newCart);
            chatIndexAdapter.notifyDataSetChanged();
        });
    }
}
