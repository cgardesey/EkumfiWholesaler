package com.ekumfi.wholesaler.activity;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;
import static com.ekumfi.wholesaler.receiver.NetworkReceiver.activeActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.realm.RealmCartProduct;
import com.ekumfi.wholesaler.receiver.NetworkReceiver;
import com.google.android.material.snackbar.Snackbar;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.adapter.ProximityProductAdapter;
import com.ekumfi.wholesaler.materialDialog.ChooseQuantityMaterialDialog;
import com.ekumfi.wholesaler.materialDialog.ChooseServiceContactMethodMaterialDialog;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmCart;
import com.ekumfi.wholesaler.realm.RealmSellerProduct;
import com.ekumfi.wholesaler.util.PixelUtil;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;


public class ProximityProductsActivity extends PermisoActivity implements ChooseQuantityMaterialDialog.ChooseQuantityMDInterface {

    NetworkReceiver networkReceiver;
    RecyclerView recyclerview;
    ProximityProductAdapter proximityProductAdapter;
    private ImageView cartIcon, backbtn;
    ArrayList<RealmSellerProduct> realmProducts = new ArrayList<>(), newRealmProducts = new ArrayList<>();
    public static Activity productActivity;
    TextView title;
    RelativeLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_proximity_products);

        recyclerview = findViewById(R.id.recyclerview);
        productActivity = this;
        backbtn = findViewById(R.id.backbtn1);
        title = findViewById(R.id.title);
        cartIcon = findViewById(R.id.cartIcon);
        parent = findViewById(R.id.parent);

        title.setText(getIntent().getStringExtra("TITLE"));

        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order(ProximityProductsActivity.this, null);
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        proximityProductAdapter = new ProximityProductAdapter(new ProximityProductAdapter.ContactMethodAdapterInterface() {
            @Override
            public void onListItemClick(ArrayList<RealmSellerProduct> realmSellerProducts, int position, ProximityProductAdapter.ViewHolder holder) {
                RealmSellerProduct realmSellerProduct = realmSellerProducts.get(position);
                ChooseServiceContactMethodMaterialDialog chooseServiceContactMethodMaterialDialog = new ChooseServiceContactMethodMaterialDialog();
                if (chooseServiceContactMethodMaterialDialog != null && chooseServiceContactMethodMaterialDialog.isAdded()) {

                } else {
                    chooseServiceContactMethodMaterialDialog.setConsumer_id(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("com.ekumfi.wholesaler" + "CONSUMERID", ""));
                    chooseServiceContactMethodMaterialDialog.setSeller_id(realmSellerProduct.getSeller_id());
                    chooseServiceContactMethodMaterialDialog.setSeller_id(realmSellerProduct.getSeller_id());
                    chooseServiceContactMethodMaterialDialog.show(getSupportFragmentManager(), "chooseContactMethodMaterialDialog");
                    chooseServiceContactMethodMaterialDialog.setCancelable(true);
                }
            }
        },
                new ProximityProductAdapter.AddToCartAdapterInterface() {
                    @Override
                    public void onListItemClick(ArrayList<RealmSellerProduct> names, int position, ProximityProductAdapter.ViewHolder holder) {
                        RealmSellerProduct realmSellerProduct = realmProducts.get(position);
                        if (realmSellerProduct.getQuantity_available() <= realmSellerProduct.getUnit_quantity()) {
                            Toast.makeText(productActivity, "This item is out of stock.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            ChooseQuantityMaterialDialog chooseQuantityMaterialDialog = new ChooseQuantityMaterialDialog();
                            if (chooseQuantityMaterialDialog != null && chooseQuantityMaterialDialog.isAdded()) {

                            } else {
                                chooseQuantityMaterialDialog.setSeller_id(realmSellerProduct.getSeller_id());
                                chooseQuantityMaterialDialog.setSeller_product_id(realmSellerProduct.getSeller_product_id());
                                chooseQuantityMaterialDialog.setQuantity_available(realmSellerProduct.getQuantity_available());
                                chooseQuantityMaterialDialog.setUnit_quantity(realmSellerProduct.getUnit_quantity());
                                chooseQuantityMaterialDialog.setUnit_price(realmSellerProduct.getUnit_price());
                                chooseQuantityMaterialDialog.setImage_url(realmSellerProduct.getProduct_image_url());
                                chooseQuantityMaterialDialog.setCancelable(false);
                                chooseQuantityMaterialDialog.show(getSupportFragmentManager(), "chooseQuantityMaterialDialog");
                                chooseQuantityMaterialDialog.setCancelable(true);
                            }
                        }
                    }
                }, productActivity, realmProducts);

        recyclerview.setLayoutManager(new LinearLayoutManager(this));
//        recyclerview.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        recyclerview.setAdapter(proximityProductAdapter);

        populateProducts(getApplicationContext());
        proximityProductAdapter.notifyDataSetChanged();

        networkReceiver = new NetworkReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permiso.getInstance().setActivity(this);
        activeActivity = this;
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    public static void order(Context context, JSONObject jsonObject) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                API_URL + "scoped-cart-products",
                response -> {
                    if (response != null) {
                        dialog.dismiss();
                        try {
                            final float[] sub_total = {0.00F};
                            JSONArray jsonArray = new JSONArray(response);
                            Realm.init(context);
                            Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
                                realm.where(RealmCartProduct.class).findAll().deleteAllFromRealm();
                                realm.createOrUpdateAllFromJson(RealmCartProduct.class, jsonArray);

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        sub_total[0] += (float) jsonArray.getJSONObject(i).getDouble("price");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            context.startActivity(new Intent(context, OrderSummaryActivity.class)
                                    .putExtra("ITEM_COUNT", jsonArray.length())
                                    .putExtra("SUB_TOTAL", sub_total[0])
                                    .putExtra("SHIPPING_FEE", 20.00F)
                                    .putExtra("CART_ID", jsonObject.getString("cart_id")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                    myVolleyError(context, error);
                    dialog.dismiss();
                    Log.d("Cyrilll", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("cart_id", jsonObject.getString("cart_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }

            /** Passing some request headers* */
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
    }

    public static void viewCart(Context context, JSONObject jsonObject) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                API_URL + "scoped-consumer-carts",
                response -> {
                    if (response != null) {
                        dialog.dismiss();
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                Realm.init(context);
                                Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
                                    realm.where(RealmCart.class).findAll().deleteAllFromRealm();
                                    realm.createOrUpdateAllFromJson(RealmCart.class, jsonArray);
                                });

                                context.startActivity(new Intent(context, CartListActivity.class));
                            } else {
                                Toast.makeText(context, "No cart items available.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                    myVolleyError(context, error);
                    dialog.dismiss();
                    Log.d("Cyrilll", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("consumer_id", PreferenceManager.getDefaultSharedPreferences(context).getString("com.ekumfi.wholesaler" + "CONSUMERID", ""));
                return params;
            }
            /** Passing some request headers* */
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
    }

    private UCrop.Options imgOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        options.setToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        options.setCropFrameColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        options.setCropFrameStrokeWidth(PixelUtil.dpToPx(getApplicationContext(), 4));
        options.setCropGridColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        options.setCropGridStrokeWidth(PixelUtil.dpToPx(getApplicationContext(), 2));
        options.setActiveWidgetColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        options.setToolbarTitle(getString(R.string.crop_image));

        // set rounded cropping guide
        options.setCircleDimmedLayer(true);
        return options;
    }

    void populateProducts(final Context context) {
        newRealmProducts.clear();

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {
            String stringExtra;
            String fieldName;
            RealmResults<RealmSellerProduct> realmProducts = realm.where(RealmSellerProduct.class)
                    .findAll();


            for (RealmSellerProduct realmProduct : realmProducts) {
                newRealmProducts.add(realmProduct);
            }

            this.realmProducts.clear();
            this.realmProducts.addAll(newRealmProducts);

            proximityProductAdapter.notifyDataSetChanged();
            recyclerview.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onViewClick(String message, JSONObject jsonObject) {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_LONG);

        // Get the Snackbar layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        // Inflate our courseListMaterialDialog viewBitmap bitmap = ((RoundedDrawable)profilePic.getDrawable()).getSourceBitmap();
        View snackView = getLayoutInflater().inflate(R.layout.snackbar, null);

        TextView messageTextView = snackView.findViewById(R.id.message);
        messageTextView.setText(message);
        TextView textViewOne = snackView.findViewById(R.id.first_text_view);
        textViewOne.setText("View cart");
        textViewOne.setOnClickListener(v -> {
            viewCart(ProximityProductsActivity.this, jsonObject);
        });

        final TextView textViewTwo = snackView.findViewById(R.id.second_text_view);

        textViewTwo.setText("Order");
        textViewTwo.setOnClickListener(v -> {
            order(ProximityProductsActivity.this, jsonObject);
        });

        // Add our courseListMaterialDialog view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);

        // Show the Snackbar
        snackbar.show();
    }

    @Override
    public void onStockCartViewClick(String message, JSONObject jsonObject) {

    }
}
