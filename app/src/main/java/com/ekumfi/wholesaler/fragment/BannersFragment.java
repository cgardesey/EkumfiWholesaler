package com.ekumfi.wholesaler.fragment;

import static com.ekumfi.wholesaler.activity.HomeActivity.APITOKEN;
import static com.ekumfi.wholesaler.constants.keyConst.API_URL;
import static com.ekumfi.wholesaler.constants.Const.myVolleyError;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.noelchew.multipickerwrapper.library.MultiPickerWrapper;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.adapter.BannersAdapter;
import com.ekumfi.wholesaler.constants.Const;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.other.MyHttpEntity;
import com.ekumfi.wholesaler.realm.RealmBanner;
import com.ekumfi.wholesaler.util.PixelUtil;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.noelchew.multipickerwrapper.library.ui.MultiPickerWrapperSupportFragment;
import com.yalantis.ucrop.UCrop;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;


public class BannersFragment extends MultiPickerWrapperSupportFragment {
    private static final String TAG = "BannersFragment";
    protected static Typeface mTfLight;
    private static final int REQUEST_MEDIA = 1002;
    Button retrybtn;
    TextView noimages;
    RecyclerView recyclerview;
    BannersAdapter imagesAdapter;
    GridLayoutManager gridLayoutManager;
    TextView titletextview, activitytitle;
    ArrayList<Object> objects = new ArrayList<>(), newObjects = new ArrayList<>();
    public static Activity recordedConferenceCallActivity;
    FloatingActionButton addImage;
    public static File banner_image_file = null;
    private ProgressDialog mProgress;

    MultiPickerWrapper.PickerUtilListener multiPickerWrapperListener = new MultiPickerWrapper.PickerUtilListener() {
        @Override
        public void onPermissionDenied() {
            // do something here
        }

        @Override
        public void onImagesChosen(List<ChosenImage> list) {
            banner_image_file = new File(list.get(0).getOriginalPath());
            new uploadBannersAsync(getActivity()).execute();
        }

        @Override
        public void onVideosChosen(List<ChosenVideo> list) {
            Const.showToast(getActivity(), getString(R.string.unsupported_file_format));
        }

        @Override
        public void onError(String s) {
            Toast.makeText(getActivity(), getString(R.string.error_choosing_image), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected MultiPickerWrapper.PickerUtilListener getMultiPickerWrapperListener() {
        return multiPickerWrapperListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_banners, container, false);

        mProgress = new ProgressDialog(getActivity());
        mProgress.setTitle("Uploading image...");
        mProgress.setMessage(getString(R.string.please_wait));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        recyclerview = rootView.findViewById(R.id.recyclerview);
        recordedConferenceCallActivity = getActivity();
        noimages = rootView.findViewById(R.id.noimages);
        titletextview = rootView.findViewById(R.id.title);
        activitytitle = rootView.findViewById(R.id.activitytitle);
        activitytitle.setText("Banners");

        addImage = rootView.findViewById(R.id.addImage);
        addImage.setOnClickListener(v -> {
            multiPickerWrapper.getPermissionAndPickSingleImageAndCrop(imgOptions(), 1, 1);
        });

        imagesAdapter = new BannersAdapter((objects, position, holder) -> {
            Object object = objects.get(position);
            PopupMenu popup = new PopupMenu(getActivity(), holder.more_details);

            popup.inflate(R.menu.image_menu);

            popup.setOnMenuItemClickListener(item -> {

                RealmBanner realmBanner = (RealmBanner) object;
                final String banner_id = realmBanner.getBanner_id();
                int itemId = item.getItemId();
                if (itemId == R.id.delete) {
                    StringRequest stringRequest = new StringRequest(
                            Request.Method.DELETE,
                            API_URL + "banners/" + banner_id,
                            response -> {
                                if (response != null) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.getBoolean("status")) {
                                            Realm.init(getActivity());
                                            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                                realm.where(RealmBanner.class).equalTo("banner_id", banner_id).findFirst().deleteFromRealm();
                                            });
                                            objects.remove(position);
                                            imagesAdapter.notifyDataSetChanged();

                                            if (objects.size() > 0) {
                                                noimages.setVisibility(View.GONE);
                                                recyclerview.setVisibility(View.VISIBLE);
                                            } else {
                                                noimages.setVisibility(View.VISIBLE);
                                                recyclerview.setVisibility(View.GONE);
                                            }
                                            Toast.makeText(getActivity(), "Successfully deleted.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Error deleting.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            error -> {
                                error.printStackTrace();
                                myVolleyError(getActivity(), error);
                                Log.d("Cyrilll", error.toString());
                            }
                    ) {
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
                    return true;
                } else if (itemId == R.id.featured_layout) {
                    try {
                        mProgress = new ProgressDialog(getActivity());
                        mProgress.setMessage(getActivity().getString(R.string.pls_wait));
                        mProgress.setCancelable(false);
                        mProgress.setIndeterminate(true);
                        mProgress.show();
                        StringRequest stringRequest1 = new StringRequest(
                                Request.Method.PATCH,
                                API_URL + "banners/" + banner_id,
                                response -> {
                                    mProgress.dismiss();
                                    if (response != null) {
                                        Toast.makeText(getActivity(), "Featured image successfully set.", Toast.LENGTH_LONG).show();
                                        try {
                                            JSONArray jsonArray = new JSONArray(response);
                                            Realm.init(getActivity());
                                            Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                                                realm.createOrUpdateAllFromJson(RealmBanner.class, jsonArray);
                                            });
                                            populateImages(getActivity());
                                            imagesAdapter.notifyDataSetChanged();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                error -> {
                                    mProgress.dismiss();
                                    error.printStackTrace();
                                    myVolleyError(getActivity(), error);
                                    Log.d("Cyrilll", error.toString());
                                }
                        ) {
                            @Override
                            public Map getHeaders() throws AuthFailureError {
                                HashMap headers = new HashMap();
                                headers.put("accept", "application/json");
                                headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                                return headers;
                            }
                        };
                        stringRequest1.setRetryPolicy(new DefaultRetryPolicy(
                                0,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        InitApplication.getInstance().addToRequestQueue(stringRequest1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        }, getActivity(), objects, "all");
        if (isTablet(getActivity())) {
            gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        } else {
            gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        }
        recyclerview.setLayoutManager(gridLayoutManager);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(imagesAdapter);

        populateImages(getActivity());
        imagesAdapter.notifyDataSetChanged();

        String role = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("com.ekumfi.wholesaler" + "ROLE", "");
        if (role.equals("CUSTOMER")) {
            addImage.setVisibility(View.GONE);
        }
        else {
            addImage.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private UCrop.Options imgOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        options.setCropFrameColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        options.setCropFrameStrokeWidth(PixelUtil.dpToPx(getActivity(), 4));
        options.setCropGridColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        options.setCropGridStrokeWidth(PixelUtil.dpToPx(getActivity(), 2));
        options.setActiveWidgetColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        options.setToolbarTitle(getString(R.string.crop_image));

        // set rounded cropping guide
        options.setCircleDimmedLayer(true);
        return options;
    }

    void populateImages(final Context context) {
        newObjects.clear();

        Realm.init(context);
        Realm.getInstance(RealmUtility.getDefaultConfig(context)).executeTransaction(realm -> {

            RealmResults<RealmBanner> realmBanners = realm.where(RealmBanner.class).findAll();

            for (RealmBanner realmBanner : realmBanners) {
                newObjects.add(realmBanner);
            }

            objects.clear();
            objects.addAll(newObjects);

            imagesAdapter.notifyDataSetChanged();
            if (objects.size() > 0) {
                noimages.setVisibility(View.GONE);
                recyclerview.setVisibility(View.VISIBLE);
            } else {
                noimages.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);
            }
        });
    }

    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    private class uploadBannersAsync extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        private Context context;

        private uploadBannersAsync(Context context) {
            context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String URL = API_URL + "banners";
            try {
                HttpPost httpPost = new HttpPost(URL);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                if (banner_image_file != null) {
                    multipartEntityBuilder.addPart("banner_image_file", new FileBody(banner_image_file));
                }
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
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                if (result.contains("connect")) {
                    Toast.makeText(getActivity(), context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Realm.init(getActivity());
                        Realm.getInstance(RealmUtility.getDefaultConfig(getActivity())).executeTransaction(realm -> {
                            realm.createOrUpdateObjectFromJson(RealmBanner.class, jsonObject);
                        });
                        populateImages(getActivity());
                        imagesAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            } else {
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
