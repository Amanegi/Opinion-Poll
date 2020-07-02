package com.pravesh.myapplication.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pravesh.myapplication.R;
import com.pravesh.myapplication.entities.Admin;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.MyViewHolder> {
    Context context;
    List<Admin> adminList;

    public AdminAdapter(Context context, List<Admin> adminList) {
        this.context = context;
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_admin, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Admin admin = adminList.get(position);
        holder.txtName.setText(admin.getName());
        holder.txtPhone.setText(admin.getPhone());
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtNameLA);
            txtPhone = itemView.findViewById(R.id.txtPhoneLA);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent("send_position");
                    intent.putExtra("position", getAdapterPosition());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    return true;
                }
            });

        }
    }

}
