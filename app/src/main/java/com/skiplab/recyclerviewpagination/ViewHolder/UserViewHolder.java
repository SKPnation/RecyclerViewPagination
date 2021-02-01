package com.skiplab.recyclerviewpagination.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skiplab.recyclerviewpagination.R;

public class UserViewHolder extends RecyclerView.ViewHolder{

    public TextView txt_name, txt_email;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_name = itemView.findViewById(R.id.name);
        txt_email = itemView.findViewById(R.id.email);

    }
}
