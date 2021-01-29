package com.skiplab.recyclerviewpagination.Utils;

import android.content.Context;
import android.widget.Toast;

import com.skiplab.recyclerviewpagination.Model.User;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<User> DataCache = new ArrayList<>();

    public static void show(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public static Boolean userExists(String key){
        for (User user: DataCache){
            if (user.getId().equals(key))
                return true;
        }
        return false;
    }
}
