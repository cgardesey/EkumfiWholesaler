package com.ekumfi.wholesaler.adapter;

/**
 * Created by Nana on 11/10/2017.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ekumfi.wholesaler.R;
import com.ekumfi.wholesaler.realm.RealmAgent;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Belal on 6/6/2017.
 */

public class AgentIndexAdapter extends RecyclerView.Adapter<AgentIndexAdapter.ViewHolder> {

    private static final String YOUR_DIALOG_TAG = "";
    AgentIndexAdapterInterface chatIndexAdapterInterface;
    Activity mActivity;
    boolean showMenu;
    private ArrayList<RealmAgent> realmAgents;
    public static final SimpleDateFormat sfd_time = new SimpleDateFormat("h:mm a");

    public AgentIndexAdapter(AgentIndexAdapterInterface chatIndexAdapterInterface, Activity mActivity, ArrayList<RealmAgent> realmAgents, boolean showMenu) {
        this.chatIndexAdapterInterface = chatIndexAdapterInterface;
        this.mActivity = mActivity;
        this.realmAgents = realmAgents;
        this.showMenu = showMenu;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_agent_index, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        RealmAgent realmAgent = realmAgents.get(position);

        holder.name.setText(StringUtils.normalizeSpace((realmAgent.getTitle() + " " + realmAgent.getFirst_name() + " " + realmAgent.getOther_names() + " " + realmAgent.getLast_name()).replace("null", "")));
        holder.location.setText(realmAgent.getStreet_address());

        if (realmAgent.getProfile_image_url() != null && !realmAgent.getProfile_image_url().equals("")) {
            Glide.with(mActivity)
                    .load(realmAgent.getProfile_image_url()) // image url
                    .apply(new RequestOptions().centerCrop())
                    .into(holder.profilepic);
        } else {
            holder.profilepic.setImageDrawable(null);
        }


        holder.profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatIndexAdapterInterface.onImageClick(realmAgents, position, holder);
            }
        });

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatIndexAdapterInterface.onItemClick(realmAgents, position, holder);
            }
        });

        if (showMenu) {
            holder.menu.setVisibility(View.VISIBLE);
        }
        else {
            holder.menu.setVisibility(View.GONE);
        }

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatIndexAdapterInterface.onMenuClick(realmAgents, position, holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return realmAgents.size();
    }

    public interface AgentIndexAdapterInterface {
        void onItemClick(ArrayList<RealmAgent> realmAgents, int position, ViewHolder holder);
        void onImageClick(ArrayList<RealmAgent> realmAgents, int position, ViewHolder holder);
        void onMenuClick(ArrayList<RealmAgent> realmAgents, int position, ViewHolder holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name, location;
        public ImageView profilepic, menu;
        public LinearLayout item;

        public ViewHolder(View itemView) {
            super(itemView);
            profilepic = itemView.findViewById(R.id.profilepic);
            name = itemView.findViewById(R.id.name);
            location = itemView.findViewById(R.id.location);
            item = itemView.findViewById(R.id.item);
            menu = itemView.findViewById(R.id.menu);
        }
    }
}
