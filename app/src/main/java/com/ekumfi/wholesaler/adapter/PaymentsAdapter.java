package com.ekumfi.wholesaler.adapter;

import static com.ekumfi.wholesaler.activity.GetAuthActivity.APITOKEN;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.ekumfi.wholesaler.activity.StockCartItemsActivity;
import com.ekumfi.wholesaler.constants.Const;
import com.ekumfi.wholesaler.constants.keyConst;
import com.ekumfi.wholesaler.other.InitApplication;
import com.ekumfi.wholesaler.realm.RealmPayment;
import com.ekumfi.wholesaler.realm.RealmStockCartProduct;
import com.ekumfi.wholesaler.util.RealmUtility;
import com.ekumfi.wholesaler.R;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.ViewHolder> implements Filterable {
    ArrayList<RealmPayment> paymentArrayList;
    private Context mContext;

    public PaymentsAdapter(ArrayList<RealmPayment> paymentArrayList) {
        this.paymentArrayList = paymentArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_payment, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final RealmPayment realmPayment = paymentArrayList.get(position);
        Date date = null;
        try {
            date = Const.dateTimeFormat.parse(realmPayment.getCreated_at());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.day.setText(String.valueOf(new DateTime(date).getDayOfMonth()));
        holder.month.setText(Const.months[date.getMonth()]);
        holder.year.setText(String.valueOf(new DateTime(date).getYear()));
        holder.amt.setText("GHC" + realmPayment.getAmount());
        holder.status.setText(realmPayment.getStatus());
        holder.number.setText(realmPayment.getMsisdn());
        holder.contact.setText(realmPayment.getPrimary_contact());
        holder.shop_name.setText(realmPayment.getShop_name());
        final String order_id = realmPayment.getOrder_id();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.order_id.setText(Html.fromHtml("<font color='#228C22'><u>" + order_id + "</u></font>", Html.FROM_HTML_MODE_COMPACT));
            holder.contact.setText(Html.fromHtml("<font color='#F1C139'><u>" + realmPayment.getPrimary_contact() + "</u></font>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.order_id.setText(Html.fromHtml("<font color='#228C22'><u>" + order_id + "</u></font>"));
            holder.contact.setText(Html.fromHtml("<font color='#F1C139'><u>" + realmPayment.getPrimary_contact() + "</u></font>"));
        }
        
        holder.order_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(mContext);
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                StringRequest stringRequest = new StringRequest(
                        com.android.volley.Request.Method.POST,
                        keyConst.API_URL + "scoped-stock-cart-products",
                        response -> {
                            if (response != null) {
                                dialog.dismiss();
                                try {
                                    final float[] sub_total = {0.00F};
                                    JSONArray jsonArray = new JSONArray(response);
                                    Realm.init(mContext);
                                    Realm.getInstance(RealmUtility.getDefaultConfig(mContext)).executeTransaction(realm -> {
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

                                    mContext.startActivity(
                                            new Intent(mContext, StockCartItemsActivity.class)
                                                    .putExtra("ORDER_ID", order_id)
                                                    .putExtra("LAUNCHED_FROM_CHAT", false)
                                    );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            Const.myVolleyError(mContext, error);
                            dialog.dismiss();
                            Log.d("Cyrilll", error.toString());
                        }
                ) {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("order_id", order_id);
                        return params;
                    }

                    /** Passing some request headers* */
                    @Override
                    public Map getHeaders() throws AuthFailureError {
                        HashMap headers = new HashMap();
                        headers.put("accept", "application/json");
                        headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(mContext).getString("com.ekumfi.wholesaler" + APITOKEN, ""));
                        return headers;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                InitApplication.getInstance().addToRequestQueue(stringRequest);
            }
        });

        /*holder.contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseServiceContactMethodMaterialDialog chooseServiceContactMethodMaterialDialog = new ChooseServiceContactMethodMaterialDialog();
                if (chooseServiceContactMethodMaterialDialog != null && chooseServiceContactMethodMaterialDialog.isAdded()) {

                } else {
                    chooseServiceContactMethodMaterialDialog.setConsumer_id("");
                    chooseServiceContactMethodMaterialDialog.setSeller_id(realmPayment.getSeller_id());
                    chooseServiceContactMethodMaterialDialog.setOrder_id(realmPayment.getOrder_id());
                    chooseServiceContactMethodMaterialDialog.show(((AppCompatActivity)mContext).getSupportFragmentManager(), "chooseContactMethodMaterialDialog");
                    chooseServiceContactMethodMaterialDialog.setCancelable(true);
                }
            }
        });*/

        /*if (realmPayment.getStatus() != null && !realmPayment.getStatus().equals("FAILED")) {
            holder.statusreason.setVisibility(View.VISIBLE);
            holder.statusreason.setText(realmPayment.getTransactionstatusreason());
        }
        else {
            holder.statusreason.setVisibility(View.GONE);
        }*/

        if (position == 0 && !PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("com.ekumfi.wholesaler" + "PAYMENT_ACTIVITY_TIPS_DISMISSED", false)) {
            ViewTreeObserver vto = holder.cardview.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.cardview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        holder.cardview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    /*SimpleTarget firstTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(ConsumerPaymentActivity.refresh)
                            .setRadius(150F)
//                        .setTitle("Tip")
                            .setDescription(mContext.getString(R.string.refresh_payment_tip) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    // make an target
                    SimpleTarget secondTarget = new SimpleTarget.Builder((Activity) mContext).setPoint(holder.cardview)
                            .setRadius(150F)
//                        .setTitle("Account Information")
                            .setDescription(mContext.getString(R.string.click_on_a_row_to_view_additional_payment_details) + mContext.getString(R.string.click_anywhere_to_dismiss))
                            .build();

                    Spotlight.with((Activity) mContext)
//                .setOverlayColor(ContextCompat.getColor(getActivity(), R.color.background))
                            .setDuration(250L)
                            .setAnimation(new DecelerateInterpolator(2f))
                            .setTargets(firstTarget, secondTarget)
                            .setClosedOnTouchedOutside(true)
                            .setOnSpotlightStartedListener(new OnSpotlightStartedListener() {
                                @Override
                                public void onStarted() {
                                    PreferenceManager
                                            .getDefaultSharedPreferences(mContext.getApplicationContext())
                                            .edit()
                                            .putBoolean("com.ekumfi.wholesaler" + "PAYMENT_ACTIVITY_TIPS_DISMISSED", true)
                                            .apply();
                                }
                            })
                            .start();*/

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return paymentArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<RealmPayment> paymentArrayList) {
        this.paymentArrayList = paymentArrayList;
        notifyDataSetChanged();
    }

    public void setFilter(ArrayList<RealmPayment> arrayList) {
        paymentArrayList = new ArrayList<>();
        paymentArrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView month, day, year, amt, status, number, order_id, statusreason, shop_name, contact;
        LinearLayout details;
        CardView cardview;

        public ViewHolder(View view) {
            super(view);
            month = view.findViewById(R.id.month);
            day = view.findViewById(R.id.day);
            year = view.findViewById(R.id.year);
            amt = view.findViewById(R.id.amt);
            status = view.findViewById(R.id.status);
            number = view.findViewById(R.id.number);
            order_id = view.findViewById(R.id.order_id);
            shop_name = view.findViewById(R.id.shop_name);
            contact = view.findViewById(R.id.contact);
            statusreason = view.findViewById(R.id.statusreason);
            details = view.findViewById(R.id.details);
            cardview = view.findViewById(R.id.cardview);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (details.getVisibility() == View.VISIBLE) {
                details.setVisibility(View.GONE);
            } else {
                details.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }

    }
}

