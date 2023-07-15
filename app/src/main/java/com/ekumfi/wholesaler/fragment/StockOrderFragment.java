package com.ekumfi.wholesaler.fragment;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.activity.StockOrdersActivity.*;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.activity.StockCartItemsActivity;
import com.ekumfi.wholesaler.adapter.StockCartListAdapter;
import com.ekumfi.wholesaler.materialDialog.ChooseServiceContactMethodMaterialDialog;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.pagerAdapter.StockOrdersPagerAdapter;
import com.ekumfi.wholesaler.realm.RealmStockCart;
import com.ekumfi.wholesaler.realm.RealmStockCartProduct;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.greysonparrelli.permiso.Permiso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class StockOrderFragment extends Fragment {
    RecyclerView recyclerview;
    TextView no_data;
    StockCartListAdapter stockCartListAdapter;
    ArrayList<RealmStockCart> cartArrayList = new ArrayList<>(), newCart = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_stock_order, container, false);

        recyclerview = rootView.findViewById(R.id.recyclerview);
        no_data = rootView.findViewById(R.id.no_data);

        stockCartListAdapter = new StockCartListAdapter(new StockCartListAdapter.StockCartAdapterInterface() {
            @Override
            public void onViewClick(ArrayList<RealmStockCart> realmStockCarts, int position, StockCartListAdapter.ViewHolder holder) {
                RealmStockCart realmStockCart = realmStockCarts.get(position);
                String stock_cart_id = realmStockCart.getStock_cart_id();
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                StringRequest stringRequest = new StringRequest(
                        com.android.volley.Request.Method.POST,
                        API_URL + "scoped-stock-cart-products",
                        response -> {
                            if (response != null) {
                                dialog.dismiss();
                                try {
                                    final float[] sub_total = {0.00F};
                                    JSONArray jsonArray = new JSONArray(response);
                                    Realm.init(getActivity());
                                    Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                        realm.where(RealmStockCartProduct.class).findAll().deleteAllFromRealm();
                                        realm.createOrUpdateAllFromJson(RealmStockCartProduct.class, jsonArray);

                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            try {
                                                sub_total[0] += (float)jsonArray.getJSONObject(i).getDouble("price");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    startActivity(
                                            new Intent(getActivity(), StockCartItemsActivity.class)
                                                    .putExtra("IS_INVOICE", realmStockCart.getStatus() != null && realmStockCart.getStatus().equals("SUCCESS"))
                                                    .putExtra("INVOICE_SUB_TOTAL", sub_total[0])
                                                    .putExtra("SHIPPING_FEE", (float)realmStockCart.getShipping_fee())
                                                    .putExtra("STOCK_CART_ID", realmStockCart.getStock_cart_id())
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
                        params.put("stock_cart_id", stock_cart_id);
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
            public void onContactClick(ArrayList<RealmStockCart> realmStockCarts, int position, StockCartListAdapter.ViewHolder holder) {
                RealmStockCart realmStockCart = realmStockCarts.get(position);
                ChooseServiceContactMethodMaterialDialog chooseServiceContactMethodMaterialDialog = new ChooseServiceContactMethodMaterialDialog();
                if (chooseServiceContactMethodMaterialDialog != null && chooseServiceContactMethodMaterialDialog.isAdded()) {

                } else {
                    chooseServiceContactMethodMaterialDialog.setConsumer_id("");
                    chooseServiceContactMethodMaterialDialog.setSeller_id(realmStockCart.getSeller_id());
                    chooseServiceContactMethodMaterialDialog.setOrder_id(realmStockCart.getOrder_id());
                    chooseServiceContactMethodMaterialDialog.show(getChildFragmentManager(), "chooseContactMethodMaterialDialog");
                    chooseServiceContactMethodMaterialDialog.setCancelable(true);
                }
            }

            @Override
            public void onDeliveryClick(ArrayList<RealmStockCart> realmStockCarts, int position, StockCartListAdapter.ViewHolder holder) {
                Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                             @Override
                                                             public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                 if (resultSet.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) && resultSet.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                                                     RealmStockCart realmStockCart = realmStockCarts.get(position);
                                                                     Uri location = Uri.parse("geo:" + realmStockCart.getSeller_latitude() + "," + realmStockCart.getSeller_longitude() + "?z=14"); // z param is zoom level
                                                                     Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
                                                                     startActivity(mapIntent);
                                                                 }
                                                             }

                                                             @Override
                                                             public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                 Permiso.getInstance().showRationaleInDialog(getString(R.string.permissions), getString(R.string.this_permission_is_mandatory_pls_allow_access), null, callback);
                                                             }
                                                         },
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION);
            }

            @Override
            public void onMarkAsDeliveredClick(ArrayList<RealmStockCart> realmStockCarts, int position, StockCartListAdapter.ViewHolder holder) {
                RealmStockCart realmStockCart = realmStockCarts.get(position);
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.PATCH,
                        API_URL + "stock-carts/" + realmStockCart.getStock_cart_id(),
                        response -> {
                            if (response != null) {
                                dialog.dismiss();
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Realm.init(getActivity());
                                    Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                        realm.createOrUpdateObjectFromJson(RealmStockCart.class, jsonObject);
                                    });
                                    mViewPager.setAdapter(new StockOrdersPagerAdapter(getFragmentManager(), statuses));
                                    mTabLayout.setViewPager(mViewPager);
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
                        params.put("delivered", "1");
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
        }, getActivity(), cartArrayList);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(stockCartListAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateStockCart(getActivity());
    }

    void populateStockCart(final Context context) {
        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            RealmResults<RealmStockCart> results = null;
            RealmResults<RealmStockCart> results2 = null;
            RealmResults<RealmStockCart> results3 = null;
            if (getArguments().getString("status").equals("Delivered")) {
                results = realm.where(RealmStockCart.class)
                        .equalTo("delivered", 1)
                        .findAll();
            }
            else if (getArguments().getString("status").equals("Undelivered")) {
                results = realm.where(RealmStockCart.class)
                        .equalTo("delivered", 0)
                        .equalTo("status", "SUCCESS")
                        .notEqualTo("paid", 1)
                        .findAll();

                results2 = realm.where(RealmStockCart.class)
                        .equalTo("delivered", 0)
                        .notEqualTo("status", "SUCCESS")
                        .equalTo("paid", 1)
                        .findAll();

                results3 = realm.where(RealmStockCart.class)
                        .equalTo("delivered", 0)
                        .equalTo("status", "SUCCESS")
                        .equalTo("paid", 1)
                        .findAll();
            }
            else {
                results = realm.where(RealmStockCart.class)
                        .equalTo("delivered", 0)
                        .notEqualTo("status", "SUCCESS")
                        .notEqualTo("paid", 1)
                        .findAll();
            }
            newCart.clear();

            if (results != null) {
                for (RealmStockCart realmStockCart : results) {
                    newCart.add(realmStockCart);
                }
            }
            if (results2 != null) {
                for (RealmStockCart realmStockCart : results2) {
                    newCart.add(realmStockCart);
                }
            }
            if (results3 != null) {
                for (RealmStockCart realmStockCart : results3) {
                    newCart.add(realmStockCart);
                }
            }

            if (newCart.size() > 0) {
                no_data.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            } else {
                no_data.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }

            cartArrayList.clear();
            cartArrayList.addAll(newCart);
            stockCartListAdapter.notifyDataSetChanged();
        });
    }
}
