package com.ekumfi.wholesaler.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.adapter.ConsumerCartListAdapter;
import com.ekumfi.wholesaler.realm.RealmCartold;
import com.ekumfi.wholesaler.util.RealmUtility;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class CartListFragmentold extends Fragment {
    RecyclerView recyclerview;
    TextView no_data;
    ConsumerCartListAdapter cartListAdapter;
    ArrayList<RealmCartold> cartArrayList = new ArrayList<>(), newCart = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_cart_listold, container, false);
        recyclerview = rootView.findViewById(R.id.recyclerview);
        no_data = rootView.findViewById(R.id.no_data);

        /*cartListAdapter = new CartListAdapter(new CartListAdapter.CartAdapterInterface() {
            @Override
            public void onViewClick(ArrayList<RealmCartold> realmCarts, int position, CartListAdapter.ViewHolder holder) {
                RealmCartold realmCart = realmCarts.get(position);
                String cart_id = realmCart.getCart_id();
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                Realm.init(getActivity());
                String customer_id = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmCustomer2.class).findFirst().getCustomer_id();


                StringRequest stringRequest = new StringRequest(
                        com.android.volley.Request.Method.POST,
                        API_URL + "scoped-cart-products",
                        response -> {
                            if (response != null) {
                                dialog.dismiss();
                                try {
                                    final float[] sub_total = {0.00F};
                                    JSONArray jsonArray = new JSONArray(response);
                                    Realm.init(getActivity());
                                    Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                        realm.where(RealmCartProduct.class).findAll().deleteAllFromRealm();
                                        realm.createOrUpdateAllFromJson(RealmCartProduct.class, jsonArray);

                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            try {
                                                sub_total[0] += (float)jsonArray.getJSONObject(i).getDouble("price");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    startActivity(
                                            new Intent(getActivity(), CartItemsActivity.class)
                                                    .putExtra("IS_INVOICE", !realmCart.getStatus().equals("Unpaid"))
                                                    .putExtra("INVOICE_SUB_TOTAL", sub_total[0])
                                                    .putExtra("SHIPPING_FEE", (float)realmCart.getShipping_fee())
                                    );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            myVolleyError(getActivity(), error);
                            dialog.dismiss();
                            Log.d("Cyrilll", error.toString());
                        }
                ) {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("cart_id", cart_id);
                        return params;
                    }
                    *//** Passing some request headers* *//*
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
            public void onContactClick(ArrayList<RealmCartold> realmCarts, int position, CartListAdapter.ViewHolder holder) {
                RealmCartold realmCart = realmCarts.get(position);
                ChooseServiceContactMethodMaterialDialog chooseServiceContactMethodMaterialDialog = new ChooseServiceContactMethodMaterialDialog();
                if (chooseServiceContactMethodMaterialDialog != null && chooseServiceContactMethodMaterialDialog.isAdded()) {

                } else {
                    chooseServiceContactMethodMaterialDialog.setSeller_id(realmCart.getProvider_id());
                    chooseServiceContactMethodMaterialDialog.setOrder_id(realmCart.getOrder_id());
                    chooseServiceContactMethodMaterialDialog.show(getChildFragmentManager(), "chooseContactMethodMaterialDialog");
                    chooseServiceContactMethodMaterialDialog.setCancelable(true);
                }
            }

            @Override
            public void onOrderClick(ArrayList<RealmCartold> realmCarts, int position, CartListAdapter.ViewHolder holder) {
                RealmCartold realmCart = realmCarts.get(position);

                String cart_id = realmCart.getCart_id();
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                StringRequest stringRequest = new StringRequest(
                        com.android.volley.Request.Method.POST,
                        API_URL + "cart-total",
                        response -> {
                            if (response != null) {
                                dialog.dismiss();
                                try {
                                    JSONArray jsonArray = new JSONArray(response);
                                    startActivity(new Intent(getActivity(), OrderSummaryActivity.class)
                                            .putExtra("ITEM_COUNT", realmCart.getItem_count())
                                            .putExtra("SUB_TOTAL", (float)jsonArray.getJSONObject(0).getDouble("total_amount"))
                                            .putExtra("SHIPPING_FEE", 20.00F));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            myVolleyError(getActivity(), error);
                            dialog.dismiss();
                            Log.d("Cyrilll", error.toString());
                        }
                ) {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("cart_id", cart_id);
                        return params;
                    }
                    *//** Passing some request headers* *//*
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
        }, getActivity(), cartArrayList);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(cartListAdapter);*/

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateCart(getActivity());
    }

    void populateCart(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmCartold> results;
            results = realm.where(RealmCartold.class)
                    .equalTo("status", "Unpaid")
                    .findAll();
            newCart.clear();
            if (results.size() > 0) {
                no_data.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            }
            else {
                no_data.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
        for (RealmCartold realmCart : results) {
                newCart.add(realmCart);
            }
            cartArrayList.clear();
            cartArrayList.addAll(newCart);
            cartListAdapter.notifyDataSetChanged();
        });
    }
}
