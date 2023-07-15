package com.ekumfi.wholesaler.activity;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.adapter.SellerProductAdapter;
import com.ekumfi.wholesaler.materialDialog.SellerProductMaterialDialog;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmSellerProduct;
import com.ekumfi.wholesaler.realm.RealmSeller;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.greysonparrelli.permiso.PermisoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by 2CLearning on 12/13/2017.
 */

public class SellerProductsActivity extends PermisoActivity {
    private static final String TAG = "ProductsFragment";
    Button add, remove;
    public static RecyclerView recyclerView;
    Context mContext;
    LinearLayout clickToAdd;
    CardView cardView;
    public static ArrayList<RealmSellerProduct> productArrayList;
    public static SellerProductAdapter productAdapter;
    public static RecyclerView.LayoutManager layoutManager;
    public static SellerProductMaterialDialog sellerProductMaterialDialog = new SellerProductMaterialDialog();
    public static RealmSeller realmSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_products);

        productArrayList = new ArrayList<>();
        mContext= this;
        cardView = findViewById(R.id.cardView);
        clickToAdd = findViewById(R.id.clickToAdd);
        recyclerView = findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(getApplicationContext());

        clickToAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sellerProductMaterialDialog != null && sellerProductMaterialDialog.isAdded()) {

                } else {
                    sellerProductMaterialDialog.setName("");
                    sellerProductMaterialDialog.setUnit_price("");
                    sellerProductMaterialDialog.setQuantity_available("");
                    sellerProductMaterialDialog.setCancelable(false);
                    sellerProductMaterialDialog.show(getSupportFragmentManager(), "addSellerProductMaterialDialog");
                    sellerProductMaterialDialog.setCancelable(true);
                }
            }
        });

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void init() {
        Realm.init(getApplicationContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(getApplicationContext())).executeTransaction(realm -> {

            RealmResults<RealmSellerProduct> realmSellerProducts = realm.where(RealmSellerProduct.class).equalTo("seller_id", realmSeller.getSeller_id()).findAll();
            productArrayList.clear();
            for (RealmSellerProduct realmSellerProduct : realmSellerProducts) {
                productArrayList.add(realmSellerProduct);
            }
        });

        productAdapter = new SellerProductAdapter((realmSellerProducts, position, holder) -> {
            RealmSellerProduct realmSellerProduct = realmSellerProducts.get(position);

            PopupMenu popup = new PopupMenu(mContext, holder.more_details);

            popup.inflate(R.menu.product_menu);

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.edit) {
                    SellerProductMaterialDialog sellerProductMaterialDialog = new SellerProductMaterialDialog();
                    if (sellerProductMaterialDialog != null && sellerProductMaterialDialog.isAdded()) {

                    } else {
                        sellerProductMaterialDialog.setSeller_id(realmSellerProduct.getSeller_id());
                        sellerProductMaterialDialog.setProduct_id(realmSellerProduct.getProduct_id());
                        sellerProductMaterialDialog.setName(realmSellerProduct.getProduct_name());
                        sellerProductMaterialDialog.setUnit_price(String.format("%.2f", realmSellerProduct.getUnit_price()));
                        sellerProductMaterialDialog.setQuantity_available(String.valueOf(realmSellerProduct.getQuantity_available()));

                        sellerProductMaterialDialog.setCancelable(false);
                        sellerProductMaterialDialog.show(getSupportFragmentManager(), "editSellerProductMaterialDialog");
                        sellerProductMaterialDialog.setCancelable(true);
                    }
                    return true;
                } else if (itemId == R.id.remove) {
                    String product_id = realmSellerProduct.getProduct_id();
                    StringRequest stringRequestDelete = new StringRequest(
                            Request.Method.DELETE,
                            API_URL + "seller-products/" + realmSellerProduct.getSeller_product_id(),
                            response -> {
                                if (response != null) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.getBoolean("status")) {
                                            Realm.init(getApplicationContext());
                                            Realm.getInstance(RealmUtility.getDefaultConfig(getApplicationContext())).executeTransaction(realm -> {
                                                realmSellerProducts.get(position).deleteFromRealm();
                                            });
                                            realmSellerProducts.remove(position);
                                            productAdapter.notifyDataSetChanged();
                                            Toast.makeText(mContext, "Successfully deleted.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(mContext, "Error deleting.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            error -> {
                                error.printStackTrace();
                                myVolleyError(mContext, error);
                                Log.d("Cyrilll", error.toString());
                            }
                    ) {
                        @Override
                        public Map getHeaders() throws AuthFailureError {
                            HashMap headers = new HashMap();
                            headers.put("accept", "application/json");
                            headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(mContext).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                            return headers;
                        }

                        @Override
                        public Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("product_id", product_id);
                            return params;
                        }
                    };
                    stringRequestDelete.setRetryPolicy(new DefaultRetryPolicy(
                            0,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    InitApplication.getInstance().addToRequestQueue(stringRequestDelete);
                    return true;
                }
                return false;
            });
            popup.show();
        }, productArrayList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(productAdapter);
    }

    public boolean validate (){
        boolean validated = true;
        return validated;
    }
}