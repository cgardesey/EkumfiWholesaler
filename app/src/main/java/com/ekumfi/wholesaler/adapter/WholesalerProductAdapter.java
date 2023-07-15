package com.ekumfi.wholesaler.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.realm.RealmWholesalerProduct;

import java.util.ArrayList;

/**
 * Created by Nana on 9/11/2017.
 */
public class WholesalerProductAdapter extends RecyclerView.Adapter<WholesalerProductAdapter.ViewHolder> {

    WholesalerProductAdapterInterface productAdapterInterface;
    ArrayList<RealmWholesalerProduct> realmWholesalerProducts;
    private Context mContext;

    public WholesalerProductAdapter(WholesalerProductAdapterInterface productAdapterInterface, ArrayList<RealmWholesalerProduct> realmWholesalerProducts) {
        this.productAdapterInterface = productAdapterInterface;
        this.realmWholesalerProducts = realmWholesalerProducts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_wholesaler_product, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RealmWholesalerProduct realmWholesalerProduct = realmWholesalerProducts.get(position);

        holder.name.setText(realmWholesalerProduct.getProduct_name());
        holder.quantity_available.setText(String.valueOf(realmWholesalerProduct.getQuantity_available()));
        if (false) {
            holder.quantity_available.setTextColor(Color.RED);
        }
        else {
            holder.quantity_available.setTextColor(0xFF888888);
        }

        Glide.with(mContext).load(realmWholesalerProduct.getProduct_image_url())
                .into(holder.featured_image);

        mContext = holder.more_details.getContext();

        holder.more_details.setOnClickListener(view -> {
            productAdapterInterface.onListItemClick(realmWholesalerProducts, position, holder);
        });
    }

    @Override
    public int getItemCount() {
        return realmWholesalerProducts.size();
    }

    public interface WholesalerProductAdapterInterface {
        void onListItemClick(ArrayList<RealmWholesalerProduct> realmWholesalerProducts, int position, ViewHolder holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity_available;
        public ImageView more_details, featured_image;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            quantity_available = view.findViewById(R.id.quantity_available);
            more_details = view.findViewById(R.id.more_details);
            featured_image = view.findViewById(R.id.featured_image);
        }
    }
}
