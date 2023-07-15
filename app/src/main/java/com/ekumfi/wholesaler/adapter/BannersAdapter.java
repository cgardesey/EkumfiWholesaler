package com.ekumfi.wholesaler.adapter;

import static com.ekumfi.wholesaler.activity.PictureActivity.idPicBitmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.activity.PictureActivity;
import com.ekumfi.wholesaler.realm.RealmBanner;

import java.util.ArrayList;

public class BannersAdapter extends RecyclerView.Adapter<BannersAdapter.ViewHolder> implements Filterable {

    ArrayList<Object> objects;
    private Activity activity;
    private String type;
    BannersAdapterInterface imagesAdapterInterface;

    public BannersAdapter(BannersAdapterInterface imagesAdapterInterface, Activity activity, ArrayList<Object> objects, String type) {
        this.imagesAdapterInterface = imagesAdapterInterface;
        this.activity = activity;
        this.objects = objects;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_banner, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Object object = objects.get(position);
        RealmBanner realmBanner = (RealmBanner) object;
        Glide.with(activity).load(realmBanner.getUrl()).apply(new RequestOptions().centerCrop()).into(holder.image);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagesAdapterInterface.onListItemClick(objects, position, holder);
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                idPicBitmap = ((BitmapDrawable) holder.image.getDrawable()).getBitmap();
                Intent intent = new Intent(activity, PictureActivity.class);
                activity.startActivity(intent);
            }
        });

        holder.more_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagesAdapterInterface.onListItemClick(objects, position, holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void reload(ArrayList<Object> resourceArrayList) {
        this.objects = resourceArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image, more_details;
        public FrameLayout featured_layout;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.image);
            more_details = view.findViewById(R.id.more_details);
            featured_layout = view.findViewById(R.id.featured_layout);
        }
    }

    public interface BannersAdapterInterface {
        void onListItemClick(ArrayList<Object> objects, int position, ViewHolder holder);
    }
}

