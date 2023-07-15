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
import com.ekumfi.wholesaler.adapter.SellerIndexAdapter;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmCart;
import com.ekumfi.wholesaler.realm.RealmChat;
import com.ekumfi.wholesaler.realm.RealmSeller;
import com.ekumfi.wholesaler.realm.RealmSellerProduct;
import com.ekumfi.wholesaler.realm.RealmWholesaler;
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

public class SellerIndexActivity extends PermisoActivity {
    static RecyclerView recyclerview;
    static TextView no_data;
    static SellerIndexAdapter sellerIndexAdapter;
    static ArrayList<RealmSeller> cartArrayList = new ArrayList<>();
    static ArrayList<RealmSeller> newCart = new ArrayList<>();
    public static Activity activity;
    FloatingActionButton fab;

    public SellerIndexActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_index);

        activity = this;
        
        recyclerview = findViewById(R.id.recyclerview);
        no_data = findViewById(R.id.no_data);
        fab = findViewById(R.id.fab);

        sellerIndexAdapter = new SellerIndexAdapter(new SellerIndexAdapter.SellerIndexAdapterInterface() {

            @Override
            public void onItemClick(ArrayList<RealmSeller> realmSellers, int position, SellerIndexAdapter.ViewHolder holder) {

            }

            @Override
            public void onImageClick(ArrayList<RealmSeller> realmSellers, int position, SellerIndexAdapter.ViewHolder holder) {

            }
            @Override
            public void onMenuClick(ArrayList<RealmSeller> realmSellers, int position, SellerIndexAdapter.ViewHolder holder) {
                final RealmSeller[] realmSeller = {realmSellers.get(position)};
                PopupMenu popup = new PopupMenu(SellerIndexActivity.this, holder.menu);

                popup.inflate(R.menu.seller_menu);

                popup.setOnMenuItemClickListener(item -> {
                    final String seller_id = realmSeller[0].getSeller_id();

                    ProgressDialog dialog = new ProgressDialog(SellerIndexActivity.this);
                    dialog.setMessage("Please wait...");
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);

                    int itemId = item.getItemId();
                    if (itemId == R.id.edit) {
                        dialog.show();
                        StringRequest stringRequest = new StringRequest(
                                Request.Method.GET,
                                API_URL + "sellers/" + seller_id,
                                response -> {
                                    if (response != null) {
                                        try {
                                            dialog.dismiss();
                                            JSONObject jsonObject = new JSONObject(response);
                                            Realm.init(SellerIndexActivity.this);
                                            Realm.getInstance(RealmUtility.getDefaultConfig(SellerIndexActivity.this)).executeTransaction(realm -> {
                                                SellerAccountActivity.realmSeller = realm.createOrUpdateObjectFromJson(RealmSeller.class, jsonObject);

                                                startActivity(new Intent(SellerIndexActivity.this, SellerAccountActivity.class)
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
                                    myVolleyError(SellerIndexActivity.this, error);
                                    Log.d("Cyrilll", error.toString());
                                }
                        ) {
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(SellerIndexActivity.this).getString("com.ekumfi.wholesaler" + HomeActivity.APITOKEN, ""));
                                return headers;
                            }
                        };
                        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(stringRequest);
                        return true;
                    } else if (itemId == R.id.product) {
                        dialog.show();
                        StringRequest stringRequestDelete = new StringRequest(
                                Request.Method.POST,
                                API_URL + "scoped-seller-products",
                                response -> {
                                    dialog.dismiss();
                                    if (response != null) {
                                        try {
                                            JSONArray jsonArray = new JSONArray(response);
                                            Realm.init(SellerIndexActivity.this);
                                            Realm.getInstance(RealmUtility.getDefaultConfig(SellerIndexActivity.this)).executeTransaction(realm -> {
//                                                    realm.where(RealmSellerProduct.class).findAll().deleteAllFromRealm();
                                                realm.createOrUpdateAllFromJson(RealmSellerProduct.class, jsonArray);
                                            });

                                            SellerProductsActivity.realmSeller = realmSeller[0];
                                            startActivity(new Intent(SellerIndexActivity.this, SellerProductsActivity.class));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                error -> {
                                    dialog.dismiss();
                                    error.printStackTrace();
                                    myVolleyError(SellerIndexActivity.this, error);
                                    Log.d("Cyrilll", error.toString());
                                }
                        ) {
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(SellerIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                return headers;
                            }

                            @Override
                            public Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("seller_id", seller_id);
                                return params;
                            }
                        };
                        stringRequestDelete.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(stringRequestDelete);
                        return true;
                    } else if (itemId == R.id.chat) {
                        final String[] seller_name = new String[1];
                        final String[] profile_image_url = new String[1];
                        final String[] availability = new String[1];

                        Realm.init(SellerIndexActivity.this);
                        seller_name[0] = realmSeller[0].getShop_name();
                        profile_image_url[0] = realmSeller[0].getShop_image_url();
                        availability[0] = realmSeller[0].getAvailability();

                        dialog.show();
                        StringRequest chatStringRequest = new StringRequest(
                                Request.Method.POST,
                                API_URL + "ekumfi-chat-data",
                                response -> {
                                    if (response != null) {
                                        dialog.dismiss();
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            Realm.init(SellerIndexActivity.this);
                                            Realm.getInstance(RealmUtility.getDefaultConfig(SellerIndexActivity.this)).executeTransaction(realm -> {
                                                try {
                                                    realmSeller[0] = realm.createOrUpdateObjectFromJson(RealmSeller.class, jsonObject.getJSONObject("seller"));
                                                    realm.createOrUpdateAllFromJson(RealmSeller.class, jsonObject.getJSONArray("chats"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                seller_name[0] = realmSeller[0].getShop_name();
                                                profile_image_url[0] = realmSeller[0].getShop_image_url();
                                                availability[0] = realmSeller[0].getAvailability();
                                            });

                                            startActivity(new Intent(SellerIndexActivity.this, MessageActivity.class)
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
                                    startActivity(new Intent(SellerIndexActivity.this, MessageActivity.class)
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
                                Realm.init(SellerIndexActivity.this);
                                Realm.getInstance(RealmUtility.getDefaultConfig(SellerIndexActivity.this)).executeTransaction(realm -> {
                                    RealmResults<RealmChat> results = realm.where(RealmChat.class)
                                            .sort("id", Sort.DESCENDING)
                                            .equalTo("seller_id", seller_id)
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
                                    } else {
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
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(SellerIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                return headers;
                            }
                        };
                        chatStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(chatStringRequest);
                        return true;
                    } else if (itemId == R.id.orders) {
                        dialog.show();
                        StringRequest ordersStringRequest = new StringRequest(
                                Request.Method.POST,
                                API_URL + "scoped-seller-carts",
                                response -> {
                                    if (response != null) {
                                        dialog.dismiss();
                                        try {
                                            JSONArray jsonArray = new JSONArray(response);
                                            Realm.init(SellerIndexActivity.this);
                                            Realm.getInstance(RealmUtility.getDefaultConfig(SellerIndexActivity.this)).executeTransaction(realm -> {
                                                realm.where(RealmCart.class).findAll().deleteAllFromRealm();
                                                realm.createOrUpdateAllFromJson(RealmCart.class, jsonArray);
                                            });
                                            startActivity(new Intent(SellerIndexActivity.this, SellerOrdersActivity.class)
                                                    .putExtra("SELLER_ID", seller_id)
                                            );
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                error -> {
                                    error.printStackTrace();
                                    myVolleyError(SellerIndexActivity.this, error);
                                    dialog.dismiss();
                                    Log.d("Cyrilll", error.toString());
                                }
                        ) {
                            @Override
                            public Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("seller_id", seller_id);
                                return params;
                            }

                            /** Passing some request headers* */
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(SellerIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                return headers;
                            }
                        };
                        ordersStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(ordersStringRequest);
                        return true;
                    }
                    return false;
                });
                popup.show();
            }
        }, SellerIndexActivity.this, cartArrayList, true);
        recyclerview.setLayoutManager(new LinearLayoutManager(SellerIndexActivity.this));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(sellerIndexAdapter);

        fab.setOnClickListener(v -> {
            startActivity(new Intent(SellerIndexActivity.this, GetSellerPhoneNumberActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        populateChatIndex(SellerIndexActivity.this);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                API_URL + "sellers",
                response -> {
                    if (response != null) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            Realm.init(SellerIndexActivity.this);
                            Realm.getInstance(RealmUtility.getDefaultConfig(SellerIndexActivity.this)).executeTransaction(realm -> {
                                realm.createOrUpdateAllFromJson(RealmSeller.class, jsonArray);
                            });
                            populateChatIndex(SellerIndexActivity.this);
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
                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(SellerIndexActivity.this).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
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
            RealmResults<RealmSeller> results;

            results = realm.where(RealmSeller.class)
                    .sort("id", Sort.DESCENDING)
                    .distinct("seller_id")
                    .equalTo("wholesaler_id", realm.where(RealmWholesaler.class).findFirst().getWholesaler_id())
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
            for (RealmSeller realmSeller : results) {
                newCart.add(realmSeller);
            }
            cartArrayList.clear();
            cartArrayList.addAll(newCart);
            sellerIndexAdapter.notifyDataSetChanged();
        });
    }
}
