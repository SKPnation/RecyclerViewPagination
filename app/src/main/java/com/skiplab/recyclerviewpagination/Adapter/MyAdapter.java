package com.skiplab.recyclerviewpagination.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skiplab.recyclerviewpagination.Model.User;
import com.skiplab.recyclerviewpagination.R;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    List<User> userList;
    Context context;

    public MyAdapter(Context context, List<User> userList) {
        this.userList = userList;
        this.context = context;
    }

    public void addAll(List<User> newUsers)
    {
        int initSize = newUsers.size();
        userList.addAll(newUsers);
        notifyItemRangeChanged(initSize,newUsers.size());
    }

    public String getLastItemId(){
        return userList.get(userList.size()-1).getId();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txt_name.setText(userList.get(position).getName());
        holder.txt_email.setText(userList.get(position).getEmail());


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView txt_name, txt_email;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_name = itemView.findViewById(R.id.name);
            txt_email = itemView.findViewById(R.id.email);
        }
    }
}