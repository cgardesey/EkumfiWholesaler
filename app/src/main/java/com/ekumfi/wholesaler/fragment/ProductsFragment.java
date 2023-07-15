package com.ekumfi.wholesaler.fragment;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;
import static com.ekumfi.wholesaler.materialDialog.ProductMaterialDialog.controls;
import static com.ekumfi.wholesaler.materialDialog.ProductMaterialDialog.product_image;
import static com.ekumfi.wholesaler.materialDialog.ProductMaterialDialog.image_not_set;
import static com.ekumfi.wholesaler.materialDialog.ProductMaterialDialog.product_image_file;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.adapter.ProductAdapter;
import com.ekumfi.wholesaler.constants.Const;
import com.ekumfi.wholesaler.materialDialog.ProductMaterialDialog;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmProduct;
import com.ekumfi.wholesaler.util.PixelUtil;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.noelchew.multipickerwrapper.library.MultiPickerWrapper;
import com.noelchew.multipickerwrapper.library.ui.MultiPickerWrapperSupportFragment;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by 2CLearning on 12/13/2017.
 */

public class ProductsFragment extends MultiPickerWrapperSupportFragment {
    private static final String TAG = "ProductsFragment";
    Button add, remove;
    public static RecyclerView recyclerView;
    Context mContext;
    LinearLayout clickToAdd;
    CardView cardView;
    public static ArrayList<RealmProduct> productArrayList;
    public static ProductAdapter productAdapter;
    public static RecyclerView.LayoutManager layoutManager;
    public static ProductMaterialDialog productMaterialDialog = new ProductMaterialDialog();
    MultiPickerWrapper.PickerUtilListener multiPickerWrapperListener = new MultiPickerWrapper.PickerUtilListener() {
        @Override
        public void onPermissionDenied() {
            // do something here
        }

        @Override
        public void onImagesChosen(List<ChosenImage> list) {
            image_not_set.setVisibility(View.GONE);
            controls.setVisibility(View.GONE);
            String imagePath = list.get(0).getOriginalPath();
            product_image.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            product_image_file = new File(list.get(0).getOriginalPath());
        }

        @Override
        public void onVideosChosen(List<ChosenVideo> list) {
            Const.showToast(getContext(), mContext.getString(R.string.unsupported_file_format));
        }

        @Override
        public void onError(String s) {
            Toast.makeText(getContext(), getString(R.string.error_choosing_image), Toast.LENGTH_SHORT).show();
        }
    };
    ProductMaterialDialog.ProductMaterialDialogAdapterInterface productMaterialDialogAdapterInterface;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_products, container, false);


        productArrayList = new ArrayList<>();
        mContext= getContext();
        cardView = rootView.findViewById(R.id.cardView);
        clickToAdd = rootView.findViewById(R.id.clickToAdd);
        add = rootView.findViewById(R.id.add);
        remove = rootView.findViewById(R.id.remove);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(getActivity());

        init();

        clickToAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productMaterialDialog != null && productMaterialDialog.isAdded()) {

                } else {
                    productMaterialDialog.setName("");
                    productMaterialDialog.setImage_url("");
                    productMaterialDialog.setUnit_quantity("");
                    productMaterialDialog.setUnit_price("");
                    productMaterialDialog.setQuantity_available("");
                    productMaterialDialog.setProductMaterialDialogAdapterInterface(productMaterialDialogAdapterInterface);
                    productMaterialDialog.show(getFragmentManager(), "addProductMaterialDialog");
                    productMaterialDialog.setCancelable(true);
                }
            }
        });

        productMaterialDialogAdapterInterface = new ProductMaterialDialog.ProductMaterialDialogAdapterInterface() {
            @Override
            public void onGalClick() {
                multiPickerWrapper.getPermissionAndPickSingleImageAndCrop(imgOptions(), 1, 1);
            }

            @Override
            public void onCamClick() {
                multiPickerWrapper.getPermissionAndTakePictureAndCrop(imgOptions(), 1, 1);
            }

            @Override
            public void onDialogDismissed() {
                init();
            }
        };

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    protected MultiPickerWrapper.PickerUtilListener getMultiPickerWrapperListener() {
        return multiPickerWrapperListener;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void init() {
        Realm.init(getContext());
        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {

            RealmResults<RealmProduct> realmProducts = realm.where(RealmProduct.class).findAll();
            productArrayList.clear();
            for (RealmProduct realmProduct : realmProducts) {
                productArrayList.add(realmProduct);
            }
        });

        productAdapter = new ProductAdapter(new ProductAdapter.ProductAdapterInterface() {
            @Override
            public void onListItemClick(ArrayList<RealmProduct> realmProducts, int position, ProductAdapter.ViewHolder holder) {
                RealmProduct realmProduct = realmProducts.get(position);

                PopupMenu popup = new PopupMenu(mContext, holder.more_details);

                popup.inflate(R.menu.product_menu);

                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.edit) {
                        ProductMaterialDialog productMaterialDialog = new ProductMaterialDialog();
                        if (productMaterialDialog != null && productMaterialDialog.isAdded()) {

                        } else {
                            productMaterialDialog.setProduct_id(realmProduct.getProduct_id());
                            productMaterialDialog.setName(realmProduct.getName());
                            productMaterialDialog.setImage_url(realmProduct.getImage_url());
                            productMaterialDialog.setUnit_price(String.format("%.2f", realmProduct.getUnit_price()));
                            productMaterialDialog.setUnit_quantity(String.valueOf(realmProduct.getUnit_quantity()));
                            productMaterialDialog.setQuantity_available(String.valueOf(realmProduct.getQuantity_available()));
                            productMaterialDialog.setProductMaterialDialogAdapterInterface(productMaterialDialogAdapterInterface);
                            productMaterialDialog.show(getFragmentManager(), "editProductMaterialDialog");
                            productMaterialDialog.setCancelable(true);
                        }
                        return true;
                    } else if (itemId == R.id.remove) {
                        String product_id = realmProduct.getProduct_id();
                        StringRequest stringRequestDelete = new StringRequest(
                                Request.Method.DELETE,
                                API_URL + "products/" + realmProduct.getProduct_id(),
                                response -> {
                                    if (response != null) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            if (jsonObject.getBoolean("status")) {
                                                Realm.init(getActivity());
                                                Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                                    realmProducts.get(position).deleteFromRealm();
                                                });
                                                realmProducts.remove(position);
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
            }
        }, productArrayList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //  myrecyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(productAdapter);
    }

    public boolean validate (){
        boolean validated = true;
        return validated;
    }

    private UCrop.Options imgOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        options.setCropFrameColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        options.setCropFrameStrokeWidth(PixelUtil.dpToPx(getContext(), 4));
        options.setCropGridColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        options.setCropGridStrokeWidth(PixelUtil.dpToPx(getContext(), 2));
        options.setActiveWidgetColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        options.setToolbarTitle(getString(R.string.crop_image));

        // set rounded cropping guide
        options.setCircleDimmedLayer(true);
        return options;
    }
}