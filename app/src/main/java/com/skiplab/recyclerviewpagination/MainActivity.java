package com.skiplab.recyclerviewpagination;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.skiplab.recyclerviewpagination.Adapter.MyAdapter;
import com.skiplab.recyclerviewpagination.Model.User;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    Context mContext = MainActivity.this;

    List<User> userList = new ArrayList<>();
    ArrayList<String> mUserIds = new ArrayList<>();
    DatabaseReference usersDb;
    CollectionReference usersCollection;

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;    //for linear layout
    MyAdapter adapter;

    ProgressBar progressBar;
    ImageView btnAdd;

    int ITEM_LOAD_COUNT= 10;
    int currentitems,tottalitems,scrolledoutitems;
    String last_key="",last_node="";
    boolean isMaxData=false,isScrolling=false;

    long firebaseDbMaxId=0;
    long firestoreDocMaxId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);
        btnAdd = findViewById(R.id.addBtn);

        getLastKeyFromFirebase();

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        usersCollection = FirebaseFirestore.getInstance().collection("Users");

        usersCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshots.size() >= 0)
                    firestoreDocMaxId = documentSnapshots.size();
            }
        });

        usersDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    firebaseDbMaxId = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new MyAdapter(this, userList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        getUsers();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                {
                    isScrolling=true;
                }

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                currentitems=layoutManager.getChildCount();
                tottalitems=layoutManager.getItemCount();
                scrolledoutitems=layoutManager.findFirstVisibleItemPosition();

                if( isScrolling && currentitems + scrolledoutitems == tottalitems)
                {
                    //  Toast.makeText(getContext(), "fetch data", Toast.LENGTH_SHORT).show();
                    isScrolling=false;
                    //fetch data
                    progressBar.setVisibility(View.VISIBLE);
                    getUsers();

                }

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Add new user");

                LinearLayout linearLayout = new LinearLayout(mContext);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(10,20,10,10);

                final EditText emailEt = new EditText(mContext);
                final EditText nameEt = new EditText(mContext);

                emailEt.setHint("user's email");
                nameEt.setHint("user's name");

                linearLayout.addView(emailEt,0);
                linearLayout.addView(nameEt,1);

                builder.setView(linearLayout);

                builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        usersDb
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                                            //String[] mIds = new String[mUserIds.size()];
                                            long data = Long.parseLong(ds.getValue(User.class).getId());
                                            long[] a = new long[]{data};
                                            long max = firebaseDbMaxId+1;
                                            for (int i=0; i<a.length; i++){
                                                if (a[i] > max)
                                                {
                                                    max = a[i];

                                                    Toast.makeText(mContext, max+"", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            mUserIds.clear();

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
//                        User user1 = new User();
//                        user1.setId(String.valueOf(firebaseDbMaxId+1));
//                        user1.setName(nameEt.getText().toString());
//                        user1.setEmail(emailEt.getText().toString());
//
//                        //Set to Firebase database
//                        usersDb.child(String.valueOf(firebaseDbMaxId+1)).setValue(user1)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        User user2 = new User(
//                                                String.valueOf(firestoreDocMaxId+1),
//                                                nameEt.getText().toString(),
//                                                emailEt.getText().toString()
//                                                );
//
//                                        //Set to Cloud Firestore
//                                        FirebaseFirestore.getInstance()
//                                                .collection("Users")
//                                                .document(String.valueOf(firestoreDocMaxId+1))
//                                                .set(user2)
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        Toast.makeText(mContext,"User added successfully",Toast.LENGTH_SHORT).show();
//                                                    }
//                                                });
//                                    }
//                                });

                    }
                });

                builder.show();
            }
        });
    }

    private void getUsers() {
        if(!isMaxData) // 1st false
        {
            Query query;

            if (TextUtils.isEmpty(last_node))
                query = FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .orderByKey()
                        .limitToFirst(ITEM_LOAD_COUNT);
            else
                query = FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .orderByKey()
                        .startAt(last_node)
                        .limitToFirst(ITEM_LOAD_COUNT);

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.hasChildren())
                    {

                        List<User> newUsers = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren())
                        {
                            newUsers.add(userSnapshot.getValue(User.class));
                        }

                        last_node =newUsers.get(newUsers.size()-1).getId();    //10  if it greater than the toatal items set to visible then fetch data from server

                        if(!last_node.equals(last_key))
                            newUsers.remove(newUsers.size()-1);    // 19,19 so to renove duplicate removeone value
                        else
                            last_node="end";

                        // Toast.makeText(getContext(), "last_node"+last_node, Toast.LENGTH_SHORT).show();

                        adapter.addAll(newUsers);
                        adapter.notifyDataSetChanged();


                    }
                    else   //reach to end no further child avaialable to show
                    {
                        isMaxData=true;
                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {

                }
            });

        }

        else
        {
            progressBar.setVisibility(View.GONE); //if data end
        }
    }


    private void getLastKeyFromFirebase()
    {
        Query getLastKey= FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .orderByKey()
                .limitToLast(1);

        getLastKey.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot lastkey : snapshot.getChildren())
                    last_key=lastkey.getKey();
                //   Toast.makeText(getContext(), "last_key"+last_key, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(getBaseContext(), "can not get last key", Toast.LENGTH_SHORT).show();
            }
        });


    }



}
