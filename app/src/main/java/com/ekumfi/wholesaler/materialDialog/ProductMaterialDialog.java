package com.ekumfi.wholesaler.materialDialog;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.ekumfi.wholesaler.activity.PictureActivity;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.other.MyHttpEntity;
import com.ekumfi.wholesaler.realm.RealmProduct;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.makeramen.roundedimageview.RoundedDrawable;
import com.makeramen.roundedimageview.RoundedImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

public class ProductMaterialDialog extends DialogFragment {
    public static String product_id, name, description, unit_quantity, quantity_available, unit_price, image_url;


    EditText unit_price_edittext, quantity_available_edittext;
    public static EditText name_edittext, unit_quantity_edittext;
    Button ok;
    private FloatingActionButton addimage, gal, cam;
    public static LinearLayout controls;
    public static TextView image_not_set;
    public static RoundedImageView product_image;
    public static File product_image_file = null;

    ProductMaterialDialogAdapterInterface productMaterialDialogAdapterInterface;

    public ProductMaterialDialogAdapterInterface getProductMaterialDialogAdapterInterface() {
        return productMaterialDialogAdapterInterface;
    }

    public void setProductMaterialDialogAdapterInterface(ProductMaterialDialogAdapterInterface productMaterialDialogAdapterInterface) {
        this.productMaterialDialogAdapterInterface = productMaterialDialogAdapterInterface;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public static String getImage_url() {
        return image_url;
    }

    public static void setImage_url(String image_url) {
        ProductMaterialDialog.image_url = image_url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static String getUnit_quantity() {
        return unit_quantity;
    }

    public static void setUnit_quantity(String unit_quantity) {
        ProductMaterialDialog.unit_quantity = unit_quantity;
    }

    public String getQuantity_available() {
        return quantity_available;
    }

    public void setQuantity_available(String quantity_available) {
        this.quantity_available = quantity_available;
    }

    public String getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(String unit_price) {
        this.unit_price = unit_price;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_item_product, null);
        name_edittext = view.findViewById(R.id.name);
        quantity_available_edittext = view.findViewById(R.id.quantity_available);
        unit_quantity_edittext = view.findViewById(R.id.unit_quantity);
        unit_price_edittext = view.findViewById(R.id.unit_price);
        gal = view.findViewById(R.id.gal);
        cam = view.findViewById(R.id.cam);
        image_not_set = view.findViewById(R.id.image_not_set);
        controls = view.findViewById(R.id.add);
        product_image = view.findViewById(R.id.shop_image);
        addimage = view.findViewById(R.id.addimage);
        ok = view.findViewById(R.id.ok);

        name_edittext.setText(name);
        unit_quantity_edittext.setText(unit_quantity);
        unit_price_edittext.setText(unit_price);
        quantity_available_edittext.setText(quantity_available);
        if (image_url != null && !image_url.equals("")) {
            Glide.with(getActivity()).load(image_url)
                    .into(product_image);
        }


        addimage.setOnClickListener(v -> {
            if (controls.getVisibility() == View.VISIBLE) {
                controls.setVisibility(View.GONE);

            } else {
                controls.setVisibility(View.VISIBLE);
            }
        });
        gal.setOnClickListener(v -> productMaterialDialogAdapterInterface.onGalClick());
        cam.setOnClickListener(v -> productMaterialDialogAdapterInterface.onCamClick());

        product_image.setOnClickListener(v -> {
            if (product_image.getDrawable() == null) {
                //Toast.makeText(mContext, getString(R.string.image_not_set), Toast.LENGTH_SHORT).show();
            } else {
                PictureActivity.idPicBitmap = ((RoundedDrawable) product_image.getDrawable()).getSourceBitmap();
                Intent intent = new Intent(getActivity(), PictureActivity.class);
//                intent.putExtra(PICTURE_TYPE, TYPE_PROFILE_PIC);
                getActivity().startActivity(intent);
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validate()) {
                    if (product_id != null && !product_id.equals("")) {
                        new editProductAsync(getActivity()).execute();
                    }
                    else {
                        new addProductAsync(getActivity()).execute();
                    }
                }
            }
        });

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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

    }

    public interface ProductMaterialDialogAdapterInterface {
        void onGalClick();
        void onCamClick();
        void onDialogDismissed();
    }

    public boolean validate() {
        boolean validated = true;
        if (TextUtils.isEmpty(name_edittext.getText())) {
            name_edittext.setError(getString(R.string.error_field_required));
            validated = false;
        }
        if (TextUtils.isEmpty(unit_quantity_edittext.getText())) {
            unit_quantity_edittext.setError(getString(R.string.error_field_required));
            validated = false;
        }
        if (TextUtils.isEmpty(unit_price_edittext.getText())) {
            unit_price_edittext.setError(getString(R.string.error_field_required));
            validated = false;
        }
        if (TextUtils.isEmpty(quantity_available_edittext.getText())) {
            quantity_available_edittext.setError(getString(R.string.error_field_required));
            validated = false;
        }
        if (product_image.getDrawable() == null) {
            image_not_set.setVisibility(View.VISIBLE);
            validated = false;
        }
        return validated;
    }

    private class addProductAsync extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        private Context context;
        private ProgressDialog mProgress = new ProgressDialog(getActivity());

        private addProductAsync(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = API_URL + "products";
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                if (product_image_file != null) {
                    multipartEntityBuilder.addPart("product_image_file", new FileBody(product_image_file));
                }
                multipartEntityBuilder.addTextBody("name", name_edittext.getText().toString().trim());
                multipartEntityBuilder.addTextBody("description", "");
                multipartEntityBuilder.addTextBody("unit_quantity", unit_quantity_edittext.getText().toString().trim());
                multipartEntityBuilder.addTextBody("unit_price", unit_price_edittext.getText().toString().trim());
                multipartEntityBuilder.addTextBody("quantity_available", quantity_available_edittext.getText().toString().trim());

                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        progress -> publishProgress((int) progress);

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));
                httpPost.setHeader("accept", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));


                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();

                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    // Server response
                    responseString = EntityUtils.toString(httpEntity);
                }
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                responseString = e.getMessage();
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
            } catch (IOException e) {
                responseString = e.getMessage();
                Log.e("gardes", e.toString());
//                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPreExecute() {
            mProgress.setMessage(getString(R.string.please_wait));
            mProgress.setCancelable(false);
            mProgress.setIndeterminate(true);
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                if (result.contains("connect")){
                    Toast.makeText(getActivity(), context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Realm.init(getActivity());
                        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                            realm.createOrUpdateObjectFromJson(RealmProduct.class, jsonObject);
                        });
                        productMaterialDialogAdapterInterface.onDialogDismissed();
                        dismiss();
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
            else {
                Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update process
            /*progressbar.setProgress(progress[0]);
            statustext.setText(progress[0].toString() + "%  complete");*/
        }
    }

    private class editProductAsync extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        private Context context;
        private ProgressDialog mProgress = new ProgressDialog(getActivity());

        private editProductAsync(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = API_URL + "products/" + product_id;
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                if (product_image_file != null) {
                    multipartEntityBuilder.addPart("product_image_file", new FileBody(product_image_file));
                }
                multipartEntityBuilder.addTextBody("name", name_edittext.getText().toString().trim());
                multipartEntityBuilder.addTextBody("description", "");
                multipartEntityBuilder.addTextBody("unit_quantity", unit_quantity_edittext.getText().toString().trim());
                multipartEntityBuilder.addTextBody("unit_price", unit_price_edittext.getText().toString().trim());
                multipartEntityBuilder.addTextBody("quantity_available", quantity_available_edittext.getText().toString().trim());

                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        progress -> publishProgress((int) progress);

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));
                httpPost.setHeader("accept", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));


                httpResponse = httpClient.execute(httpPost);
                httpEntity = httpResponse.getEntity();

                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 200 || statusCode == 201) {
                    // Server response
                    responseString = EntityUtils.toString(httpEntity);
                }
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                responseString = e.getMessage();
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
            } catch (IOException e) {
                responseString = e.getMessage();
                Log.e("gardes", e.toString());
//                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPreExecute() {
            mProgress.setMessage(getString(R.string.please_wait));
            mProgress.setCancelable(false);
            mProgress.setIndeterminate(true);
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                if (result.contains("connect")){
                    Toast.makeText(getActivity(), context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Realm.init(getActivity());
                        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                            realm.createOrUpdateObjectFromJson(RealmProduct.class, jsonObject);
                        });
                        productMaterialDialogAdapterInterface.onDialogDismissed();
                        dismiss();
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
            else {
                Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update process
            /*progressbar.setProgress(progress[0]);
            statustext.setText(progress[0].toString() + "%  complete");*/
        }
    }
}