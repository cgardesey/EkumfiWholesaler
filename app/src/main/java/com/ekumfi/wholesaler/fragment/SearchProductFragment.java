package com.ekumfi.wholesaler.fragment;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.clearAppData;
import static com.ekumfi.wholesaler.constants.Const.isTablet;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.activity.OrderSummaryActivity;
import com.ekumfi.wholesaler.activity.ProximityProductsActivity;
import com.ekumfi.wholesaler.activity.SearchProductsActivity;
import com.ekumfi.wholesaler.activity.StockCartItemsActivity;
import com.ekumfi.wholesaler.materialDialog.ChooseQuantityMaterialDialog;
import com.ekumfi.wholesaler.realm.RealmStockCartProduct;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.adapter.ProductListAdapter;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmBanner;
import com.ekumfi.wholesaler.realm.RealmConsumer;
import com.ekumfi.wholesaler.realm.RealmProduct;
import com.ekumfi.wholesaler.realm.RealmSellerProduct;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.ekumfi.wholesaler.util.carousel.ViewPagerCarouselView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;


public class SearchProductFragment extends Fragment implements ChooseQuantityMaterialDialog.ChooseQuantityMDInterface{
    ArrayList<RealmBanner> realmBannerArrayList = new ArrayList<>();
    ArrayList<RealmProduct> realmProducts = new ArrayList<>();
    RecyclerView recyclerView;
    private ShimmerFrameLayout shimmer_view_container;

    static ViewPagerCarouselView viewPagerCarouselView;
    public static RelativeLayout searchlayout;
    public static LinearLayout error_loading;
    ProductListAdapter productListAdapter;
    Button retrybtn;
    FrameLayout frame;
    Activity activity;
    Double longitude = 0.0d, latitude = 0.0d;
    CoordinatorLayout parent;

    private FusedLocationProviderClient fusedLocationClient;

    public SearchProductFragment() {

    }

    public SearchProductFragment(@NonNull ActivityResultRegistry registry) {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_search_product, container, false);

        activity = getActivity();

        String role = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + "ROLE", "");
        if (role.equals("CONSUMER")) {
            Realm.init(getActivity());
            RealmConsumer realmConsumer = Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).where(RealmConsumer.class).equalTo("consumer_id", androidx.preference.PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + "CONSUMERID", "")).findFirst();
            longitude = realmConsumer.getLongitude();
            latitude = realmConsumer.getLatitude();
            if (realmConsumer == null) {
                clearAppData(getActivity());
            }
        }


        viewPagerCarouselView = rootView.findViewById(R.id.carousel_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        parent = rootView.findViewById(R.id.parent);
        frame = rootView.findViewById(R.id.frame);
        searchlayout = rootView.findViewById(R.id.searchlayout);

        searchlayout.setOnClickListener(view -> startActivity(new Intent(getContext(), SearchProductsActivity.class)));
        recyclerView = rootView.findViewById(R.id.recyclerView);
        if (isTablet(getContext())) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        shimmer_view_container.startShimmerAnimation();
        error_loading = rootView.findViewById(R.id.error_loading);
        productListAdapter = new ProductListAdapter((realmProducts, position, holder) -> {
            RealmProduct realmProduct = realmProducts.get(position);
            if (role.equals("CONSUMER")) {
                if (longitude == 0.0d && latitude == 0.0d) {
                    if (checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        // Got last known location. In some rare situations this can be null.
                                        if (location != null) {
                                            launchProximityProducts(realmProduct.getProduct_id(), realmProduct.getName(), location.getLongitude(), location.getLatitude());
                                        }
                                        else {
                                            Toast.makeText(getActivity(), "Error retrieving location.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    else {
                        ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                                        .RequestMultiplePermissions(), result -> {
                                    Boolean fineLocationGranted = null;
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                        fineLocationGranted = result.getOrDefault(
                                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                                    }
                                    Boolean coarseLocationGranted = null;
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                        coarseLocationGranted = result.getOrDefault(
                                                Manifest.permission.ACCESS_COARSE_LOCATION, false);
                                    }
                                    if (fineLocationGranted != null && fineLocationGranted) {
                                        // Precise location access granted.
                                        fusedLocationClient.getLastLocation()
                                                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                                    @Override
                                                    public void onSuccess(Location location) {
                                                        // Got last known location. In some rare situations this can be null.
                                                        if (location != null) {
                                                            launchProximityProducts(realmProduct.getProduct_id(), realmProduct.getName(), location.getLongitude(), location.getLatitude());
                                                        }
                                                        else {
                                                            Toast.makeText(getActivity(), "Error retrieving location.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                                        /*new GoogleMap.OnMyLocationChangeListener() {
                                                            @Override
                                                            public void onMyLocationChange(Location location) {
                                                                Toast.makeText(, "", Toast.LENGTH_SHORT).show();
                                                            }
                                                        };*/
                                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                        // Only approximate location access granted.
                                        fusedLocationClient.getLastLocation()
                                                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                                    @Override
                                                    public void onSuccess(Location location) {
                                                        // Got last known location. In some rare situations this can be null.
                                                        if (location != null) {
                                                            launchProximityProducts(realmProduct.getProduct_id(), realmProduct.getName(), location.getLongitude(), location.getLatitude());
                                                        }
                                                        else {
                                                            Toast.makeText(getActivity(), "Error retrieving location.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        // No location access granted.
                                    }
                                }
                        );

                        // ...

                        // Before you perform the actual permission request, check whether your app
                        // already has the permissions, and whether your app needs to show a permission
                        // rationale dialog. For more details, see Request permissions.
                        locationPermissionRequest.launch(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        });
                    }
                }
                else {
                    launchProximityProducts(realmProduct.getProduct_id(), realmProduct.getName(), longitude, latitude);
                }
            }
            else {
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.GET,
                        API_URL + "products/" + realmProduct.getProduct_id(),
                        response -> {
                            if (response != null) {
                                dialog.dismiss();
                                try {
                                    final float[] sub_total = {0.00F};
                                    JSONObject jsonObject = new JSONObject(response);
                                    Realm.init(getActivity());
                                    final RealmProduct[] product = new RealmProduct[1];
                                    Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                        product[0] = realm.createOrUpdateObjectFromJson(RealmProduct.class, jsonObject);

                                    });
                                    if (product[0].getQuantity_available() == 0) {
                                        Toast.makeText(getActivity(), "This item is out of stock.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        ChooseQuantityMaterialDialog chooseQuantityMaterialDialog = new ChooseQuantityMaterialDialog();
                                        if (chooseQuantityMaterialDialog != null && chooseQuantityMaterialDialog.isAdded()) {

                                        } else {
                                            chooseQuantityMaterialDialog.setSeller_id(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + "SELLER_ID", ""));
                                            chooseQuantityMaterialDialog.setProduct_id(product[0].getProduct_id());
                                            chooseQuantityMaterialDialog.setQuantity_available(product[0].getQuantity_available());
                                            chooseQuantityMaterialDialog.setUnit_quantity(product[0].getUnit_quantity());
                                            chooseQuantityMaterialDialog.setUnit_price(product[0].getUnit_price());
                                            chooseQuantityMaterialDialog.setImage_url(product[0].getImage_url());
                                            chooseQuantityMaterialDialog.setCancelable(false);
                                            chooseQuantityMaterialDialog.show(getChildFragmentManager(), "chooseQuantityMaterialDialog");
                                            chooseQuantityMaterialDialog.setCancelable(true);
                                        }
                                    }

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

                    /* Passing some request headers* */
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

        }, getActivity(), realmProducts, "");
        recyclerView.setAdapter(productListAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getCustomerHomeData();
    }

    private void launchProximityProducts(String product_id, String product_name, double longitude, double lattitude) {
        try {
            ProgressDialog mProgress = new ProgressDialog(getContext());
            mProgress.setMessage("Please wait...");
            mProgress.setCancelable(false);
            mProgress.setIndeterminate(true);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "proximity-products",
                    response -> {
                        mProgress.dismiss();
                        if (response != null) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                Realm.init(getContext());
                                Realm.getInstance(RealmUtility.getDefaultConfig(getContext())).executeTransaction(realm -> {
                                    RealmResults<RealmSellerProduct> realmSellerProducts = realm.where(RealmSellerProduct.class).findAll();
                                    realmSellerProducts.deleteAllFromRealm();

                                    realm.createOrUpdateAllFromJson(RealmSellerProduct.class, jsonArray);
                                });
                                if (jsonArray.length() == 0) {
                                    Toast.makeText(getContext(), "No matching sellers available!", Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(getContext(), ProximityProductsActivity.class)
                                            .putExtra("TITLE", product_name)
                                            .putExtra("LONGITUDE", longitude)
                                            .putExtra("LATITUDE", lattitude)
                                    );
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        mProgress.dismiss();
                        myVolleyError(getContext(), error);
                    }
            ) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<>();
                    params.put("product_id", product_id);
                    return params;
                }
                @Override
                public Map getHeaders() throws AuthFailureError {
                    HashMap headers = new HashMap();
                    headers.put("accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getContext()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);

        } catch (Exception e) {
            Log.e("My error", e.toString());
        }
    }

    public void getCustomerHomeData() {
        Realm.init(getContext());
        try {
            shimmer_view_container.startShimmerAnimation();
            shimmer_view_container.setVisibility(View.VISIBLE);
            frame.setVisibility(View.GONE);
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    API_URL + "consumer-home-data",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Realm.init(getActivity());
                                    Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                        try {
                                            realm.where(RealmBanner.class).findAll().deleteAllFromRealm();
                                            realm.where(RealmProduct.class).findAll().deleteAllFromRealm();

                                            realm.createOrUpdateAllFromJson(RealmBanner.class, jsonObject.getJSONArray("banners"));
                                            realm.createOrUpdateAllFromJson(RealmProduct.class, jsonObject.getJSONArray("products"));
                                            shimmer_view_container.stopShimmerAnimation();
                                            shimmer_view_container.setVisibility(View.GONE);

                                            RealmResults<RealmBanner> realmBanners = realm.where(RealmBanner.class).findAll();
                                            for (RealmBanner realmBanner : realmBanners) {
                                                realmBannerArrayList.add(realmBanner);
                                            }
                                            if (realmBannerArrayList.size() > 0) {
                                                viewPagerCarouselView.setData(getFragmentManager(), realmBannerArrayList, 3500);
                                                frame.setVisibility(View.VISIBLE);
                                            }

                                            RealmResults<RealmProduct> products = realm.where(RealmProduct.class).findAll();
                                            realmProducts.clear();
                                            for (RealmProduct realmProduct : products) {
                                                realmProducts.add(realmProduct);
                                            }
                                            if (realmProducts.size() > 0) {
                                                productListAdapter.notifyDataSetChanged();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            shimmer_view_container.stopShimmerAnimation();
                            shimmer_view_container.setVisibility(View.GONE);


                            Realm.init(getContext());
                            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                RealmResults<RealmBanner> realmBannerRealmResults = realm.where(RealmBanner.class).findAll();
                                if (realmBannerRealmResults.size() > 0) {
                                    realmBannerArrayList.clear();
                                    for (RealmBanner banner : realmBannerRealmResults) {
                                        realmBannerArrayList.add(banner);
                                    }
                                    viewPagerCarouselView.setData(getFragmentManager(), realmBannerArrayList, 3500);
                                    frame.setVisibility(View.VISIBLE);
                                    //  error_loading.setVisibility(View.GONE);

                                } else {
                                    ///  error_loading.setVisibility(View.VISIBLE);
                                }
                                RealmResults<RealmProduct> products = realm.where(RealmProduct.class).findAll();
                                realmProducts.clear();
                                for (RealmProduct realmProduct : products) {
                                    realmProducts.add(realmProduct);
                                }
                                if (realmProducts.size() > 0) {
                                    productListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
            )
            {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("consumer_id", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + "CONSUMERID", ""));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewClick(String message, JSONObject jsonObject) {

    }

    @Override
    public void onStockCartViewClick(String message, JSONObject jsonObject) {

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
            ProgressDialog dialog = new ProgressDialog(getContext());
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
                                            sub_total[0] += (float) jsonArray.getJSONObject(i).getDouble("price");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                startActivity(
                                        new Intent(getActivity(), StockCartItemsActivity.class)
                                                .putExtra("IS_INVOICE", jsonObject.getString("status") != null && jsonObject.getString("status").equals("SUCCESS"))
                                                .putExtra("INVOICE_SUB_TOTAL", sub_total[0])
                                                .putExtra("SHIPPING_FEE", (float) jsonObject.getDouble("shipping_fee"))
                                                .putExtra("STOCK_CART_ID", jsonObject.getString("stock_cart_id"))
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
                    try {
                        params.put("stock_cart_id", jsonObject.getString("stock_cart_id"));
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
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);
        });

        final TextView textViewTwo = snackView.findViewById(R.id.second_text_view);

        textViewTwo.setText("Order");
        textViewTwo.setOnClickListener(v -> {
            ProgressDialog dialog = new ProgressDialog(getContext());
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
                                            sub_total[0] += (float) jsonArray.getJSONObject(i).getDouble("price");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                            /*startActivity(
                                                    new Intent(getActivity(), StockCartItemsActivity.class)
                                                            .putExtra("IS_INVOICE", jsonObject.getString("status") != null && jsonObject.getString("status").equals("SUCCESS"))
                                                            .putExtra("INVOICE_SUB_TOTAL", sub_total[0])
                                                            .putExtra("SHIPPING_FEE", (float) jsonObject.getDouble("shipping_fee"))
                                                            .putExtra("STOCK_CART_ID", jsonObject.getString("stock_cart_id"))
                                            );*/


                                startActivity(new Intent(getActivity(), OrderSummaryActivity.class)
                                        .putExtra("ITEM_COUNT", jsonArray.length())
                                        .putExtra("SUB_TOTAL", sub_total[0])
                                        .putExtra("SHIPPING_FEE", 20.00F)
                                        .putExtra("STOCK_CART_ID", jsonObject.getString("stock_cart_id")));
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
                    try {
                        params.put("stock_cart_id", jsonObject.getString("stock_cart_id"));
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
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(stringRequest);
        });

        // Add our courseListMaterialDialog view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);

        // Show the Snackbar
        snackbar.show();
    }
}
