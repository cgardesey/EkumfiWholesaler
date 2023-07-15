package com.ekumfi.wholesaler.activity;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.adapter.AgentIndexAdapter;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmAgent;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.greysonparrelli.permiso.PermisoActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class AgentIndexActivity extends PermisoActivity {
    static RecyclerView recyclerview;
    static TextView no_data;
    static AgentIndexAdapter agentIndexAdapter;
    static ArrayList<RealmAgent> cartArrayList = new ArrayList<>();
    static ArrayList<RealmAgent> newCart = new ArrayList<>();
    public static Activity activity;
    FloatingActionButton fab;

    public AgentIndexActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_index);

        activity = this;
        
        recyclerview = findViewById(R.id.recyclerview);
        no_data = findViewById(R.id.no_data);
        fab = findViewById(R.id.fab);

        agentIndexAdapter = new AgentIndexAdapter(new AgentIndexAdapter.AgentIndexAdapterInterface() {

            @Override
            public void onItemClick(ArrayList<RealmAgent> realmAgents, int position, AgentIndexAdapter.ViewHolder holder) {

            }

            @Override
            public void onImageClick(ArrayList<RealmAgent> realmAgents, int position, AgentIndexAdapter.ViewHolder holder) {

            }
            @Override
            public void onMenuClick(ArrayList<RealmAgent> realmAgents, int position, AgentIndexAdapter.ViewHolder holder) {
                final RealmAgent[] realmAgent = {realmAgents.get(position)};
                PopupMenu popup = new PopupMenu(AgentIndexActivity.this, holder.menu);

                popup.inflate(R.menu.agent_menu);

                popup.setOnMenuItemClickListener(item -> {
                    final String agent_id = realmAgent[0].getAgent_id();

                    ProgressDialog dialog = new ProgressDialog(AgentIndexActivity.this);
                    dialog.setMessage("Please wait...");
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);

                    if (item.getItemId() == R.id.edit) {
                        dialog.show();
                        StringRequest stringRequest = new StringRequest(
                                Request.Method.GET,
                                API_URL + "agents/" + agent_id,
                                response -> {
                                    if (response != null) {
                                        try {
                                            dialog.dismiss();
                                            JSONObject jsonObject = new JSONObject(response);
                                            Realm.init(AgentIndexActivity.this);
                                            Realm.getInstance(RealmUtility.getDefaultConfig(AgentIndexActivity.this)).executeTransaction(realm -> {
                                                AgentAccountActivity.realmAgent = realm.createOrUpdateObjectFromJson(RealmAgent.class, jsonObject);

                                                startActivity(new Intent(AgentIndexActivity.this, AgentAccountActivity.class)
                                                        .putExtra("MODE", "EDIT")
                                                );
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                error -> {
                                    dialog.dismiss();
                                    error.printStackTrace();
                                    myVolleyError(AgentIndexActivity.this, error);
                                    Log.d("Cyrilll", error.toString());
                                }
                        ) {
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(AgentIndexActivity.this).getString("com.ekumfi.wholesaler" + HomeActivity.APITOKEN, ""));
                                return headers;
                            }
                        };
                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(stringRequest);
                        return true;
                        /*case R.id.product:
                            dialog.show();
                            StringRequest stringRequestDelete = new StringRequest(
                                    Request.Method.POST,
                                    API_URL + "scoped-agent-products",
                                    response -> {
                                        dialog.dismiss();
                                        if (response != null) {
                                            try {
                                                JSONArray jsonArray = new JSONArray(response);
                                                Realm.init(AgentIndexActivity.this);
                                                Realm.getInstance(RealmUtility.getDefaultConfig(AgentIndexActivity.this)).executeTransaction(realm -> {
//                                                    realm.where(RealmAgentProduct.class).findAll().deleteAllFromRealm();
                                                    realm.createOrUpdateAllFromJson(RealmAgentProduct.class, jsonArray);
                                                });

                                                AgentProductsActivity.realmAgent = realmAgent[0];
                                                startActivity(new Intent(AgentIndexActivity.this, AgentProductsActivity.class));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    error -> {
                                        dialog.dismiss();
                                        error.printStackTrace();
                                        myVolleyError(AgentIndexActivity.this, error);
                                        Log.d("Cyrilll", error.toString());
                                    }
                            ) {
                                @Override
                                public Map getHeaders() throws AuthFailureError {
                                    HashMap headers = new HashMap();
                                    headers.put("accept", "application/json");
                                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(AgentIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                    return headers;
                                }
                                @Override
                                public Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("agent_id", agent_id);
                                    return params;
                                }
                            };
                            stringRequestDelete.setRetryPolicy(new DefaultRetryPolicy(
                                    0,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            InitApplication.getInstance().addToRequestQueue(stringRequestDelete);
                            return true;*/
                        /*case R.id.chat:
                            final String[] agent_name = new String[1];
                            final String[] profile_image_url = new String[1];
                            final String[] availability = new String[1];

                            Realm.init(AgentIndexActivity.this);
                            agent_name[0] = realmAgent[0].getShop_name();
                            profile_image_url[0] = realmAgent[0].getShop_image_url();
                            availability[0] = realmAgent[0].getAvailability();

                            dialog.show();
                            StringRequest chatStringRequest = new StringRequest(
                                    Request.Method.POST,
                                    API_URL + "ekumfi-chat-data",
                                    response -> {
                                        if (response != null) {
                                            dialog.dismiss();
                                            try {
                                                JSONObject jsonObject = new JSONObject(response);
                                                Realm.init(AgentIndexActivity.this);
                                                Realm.getInstance(RealmUtility.getDefaultConfig(AgentIndexActivity.this)).executeTransaction(realm -> {
                                                    try {
                                                        realmAgent[0] = realm.createOrUpdateObjectFromJson(RealmAgent.class, jsonObject.getJSONObject("agent"));
                                                        realm.createOrUpdateAllFromJson(RealmAgent.class, jsonObject.getJSONArray("chats"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    agent_name[0] = realmAgent[0].getShop_name();
                                                    profile_image_url[0] = realmAgent[0].getShop_image_url();
                                                    availability[0] = realmAgent[0].getAvailability();
                                                });

                                                startActivity(new Intent(AgentIndexActivity.this, MessageActivity.class)
                                                        .putExtra("AGENT_ID", agent_id)
                                                        .putExtra("AGENT_NAME", agent_name[0])
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
                                        startActivity(new Intent(AgentIndexActivity.this, MessageActivity.class)
                                                .putExtra("AGENT_ID", agent_id)
                                                .putExtra("AGENT_NAME", agent_name[0])
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
                                    params.put("agent_id", agent_id);
                                    params.put("consumer_id", "");
                                    Realm.init(AgentIndexActivity.this);
                                    Realm.getInstance(RealmUtility.getDefaultConfig(AgentIndexActivity.this)).executeTransaction(realm -> {
                                        RealmResults<RealmChat> results = realm.where(RealmChat.class)
                                                .sort("id", Sort.DESCENDING)
                                                .equalTo("agent_id", agent_id)
                                                .equalTo("consumer_id", "")
                                                .findAll();
                                        ArrayList<RealmChat> myArrayList = new ArrayList<>();
                                        for (RealmChat realmChat : results) {
                                            if (realmChat != null && !(realmChat.getChat_id().startsWith("z"))) {
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
                                @Override
                                public Map getHeaders() throws AuthFailureError {
                                    HashMap headers = new HashMap();
                                    headers.put("accept", "application/json");
                                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(AgentIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                    return headers;
                                }
                            };
                            chatStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    0,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            InitApplication.getInstance().addToRequestQueue(chatStringRequest);
                            return true;*/
                        /*case R.id.orders:
                            dialog.show();
                            StringRequest ordersStringRequest = new StringRequest(
                                    Request.Method.POST,
                                    API_URL + "scoped-agent-carts",
                                    response -> {
                                        if (response != null) {
                                            dialog.dismiss();
                                            try {
                                                JSONArray jsonArray = new JSONArray(response);
                                                Realm.init(AgentIndexActivity.this);
                                                Realm.getInstance(RealmUtility.getDefaultConfig(AgentIndexActivity.this)).executeTransaction(realm -> {
                                                    realm.where(RealmCart.class).findAll().deleteAllFromRealm();
                                                    realm.createOrUpdateAllFromJson(RealmCart.class, jsonArray);
                                                });
                                                startActivity(new Intent(AgentIndexActivity.this, AgentOrdersActivity.class)
                                                        .putExtra("AGENT_ID", agent_id)
                                                );
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    error -> {
                                        error.printStackTrace();
                                        myVolleyError(AgentIndexActivity.this, error);
                                        dialog.dismiss();
                                        Log.d("Cyrilll", error.toString());
                                    }
                            ) {
                                @Override
                                public Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("agent_id", agent_id);
                                    return params;
                                }
                                @Override
                                public Map getHeaders() throws AuthFailureError {
                                    HashMap headers = new HashMap();
                                    headers.put("accept", "application/json");
                                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(AgentIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                    return headers;
                                }
                            };
                            ordersStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                    0,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            InitApplication.getInstance().addToRequestQueue(ordersStringRequest);
                            return true;*/
                    }
                    return false;
                });
                popup.show();
            }
        }, AgentIndexActivity.this, cartArrayList, true);
        recyclerview.setLayoutManager(new LinearLayoutManager(AgentIndexActivity.this));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(agentIndexAdapter);

        fab.setOnClickListener(v -> {
            startActivity(new Intent(AgentIndexActivity.this, GetAgentPhoneNumberActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        populateChatIndex(AgentIndexActivity.this);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                API_URL + "agents",
                response -> {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            Realm.init(AgentIndexActivity.this);
                            Realm.getInstance(RealmUtility.getDefaultConfig(AgentIndexActivity.this)).executeTransaction(realm -> {
                                realm.createOrUpdateAllFromJson(RealmAgent.class, jsonArray);
                            });
                            populateChatIndex(AgentIndexActivity.this);
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

             /*Passing some request headers*/
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(AgentIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
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
            RealmResults<RealmAgent> results;

            results = realm.where(RealmAgent.class)
                    .sort("id", Sort.DESCENDING)
                    .distinct("agent_id")
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
            for (RealmAgent realmAgent : results) {
                newCart.add(realmAgent);
            }
            cartArrayList.clear();
            cartArrayList.addAll(newCart);
            agentIndexAdapter.notifyDataSetChanged();
        });
    }
}
