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
import com.ekumfi.wholesaler.realm.RealmProduct;
import com.ekumfi.wholesaler.R;

import java.util.ArrayList;

/**
 * Created by Nana on 9/11/2017.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    ProductAdapterInterface productAdapterInterface;
    ArrayList<RealmProduct> realmProducts;
    private Context mContext;

    public ProductAdapter(ProductAdapterInterface productAdapterInterface, ArrayList<RealmProduct> realmProducts) {
        this.productAdapterInterface = productAdapterInterface;
        this.realmProducts = realmProducts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycle_product, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RealmProduct realmProduct = realmProducts.get(position);

        holder.name.setText(realmProduct.getName());
        holder.quantity_available.setText(String.valueOf(realmProduct.getQuantity_available()));
        if (false) {
            holder.quantity_available.setTextColor(Color.RED);
        }
        else {
            holder.quantity_available.setTextColor(0xFF888888);
        }

        Glide.with(mContext).load(realmProduct.getImage_url())
                .into(holder.featured_image);

        mContext = holder.more_details.getContext();

        holder.more_details.setOnClickListener(view -> {
            productAdapterInterface.onListItemClick(realmProducts, position, holder);
        });
    }

    @Override
    public int getItemCount() {
        return realmProducts.size();
    }

    public interface ProductAdapterInterface {
        void onListItemClick(ArrayList<RealmProduct> realmProducts, int position, ViewHolder holder);
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
