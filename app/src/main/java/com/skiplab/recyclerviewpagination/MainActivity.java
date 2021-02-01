package com.skiplab.recyclerviewpagination;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.skiplab.recyclerviewpagination.Adapter.MyAdapter;
import com.skiplab.recyclerviewpagination.Model.User;
import com.skiplab.recyclerviewpagination.Utils.Utils;
import com.skiplab.recyclerviewpagination.ViewHolder.UserViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.View.GONE;
import static com.skiplab.recyclerviewpagination.Utils.Utils.DataCache;


public class MainActivity extends AppCompatActivity {

    Context mContext = MainActivity.this;

    ArrayList<Long> list = new ArrayList<Long>();

    SwipeRefreshLayout swipeRefresh;

    private ImageView btnAdd;
    private long max;

    private ProgressBar progressBar;

    DatabaseReference usersDb;
    CollectionReference usersCollection;

    FirestorePagingAdapter fsAdapter;
    FirebaseRecyclerPagingAdapter fbAdapter;

    PagedList.Config pagedListConfig;

    List<User> userList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;    //for linear layout
    //private MyAdapter adapter;
    private int ITEMS_PER_PAGE = 11;
    private Boolean isScrolling = false;
    private int currentItems, totalItems, scrolledOutItems;
    private Boolean reachedTheEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progressBar);
        btnAdd = findViewById(R.id.addBtn);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        usersCollection = FirebaseFirestore.getInstance().collection("Users");

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        //adapter = new MyAdapter(this, userList);
        //recyclerView.setAdapter(adapter);
        //recyclerView.setLayoutManager(layoutManager);

        //Initialize PagedList Configuration
        pagedListConfig = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(10)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        //loadPaginated();

        /**
         * Firebase realtime database pagination
         */
        firebaseDatabasePagination();

        /**
         * Firestore Pagination
         */
        //firestorePagination();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fbAdapter);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add new User
                addNewUser();
            }
        });
    }

    private void firebaseDatabasePagination() {

        //Initialize FirebasePagingOptions
        DatabasePagingOptions<User> options = new DatabasePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(usersDb, pagedListConfig, User.class)
                .build();

        fbAdapter = new FirebaseRecyclerPagingAdapter<User, UserViewHolder>(options){

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout_item, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder viewHolder, int position, @NonNull User model) {
                viewHolder.txt_name.setText(model.getName());
                viewHolder.txt_email.setText(model.getEmail());
            }

            @Override
            protected void onLoadingStateChanged(@NonNull com.firebase.ui.database.paging.LoadingState state) {
                switch (state){
                    case LOADING_INITIAL:
                        Log.d("PAGING_LOG","Loading Initial Data");
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case LOADING_MORE:
                        // Do your loading animation
                        Log.d("PAGING_LOG","Loading Next Page");
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case FINISHED:
                        //Reached end of Data set
                        Log.d("PAGING_LOG","All Data Loaded");
                        progressBar.setVisibility(GONE);
                        break;
                    case ERROR:
                        retry();
                        Log.d("PAGING_LOG","Error Loading Data");
                        progressBar.setVisibility(GONE);
                        break;
                    case LOADED:
                        // Stop Animation
                        Log.d("PAGING_LOG","Total items loaded: " + getItemCount());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount()-1);
                            }
                        },1500);
                        progressBar.setVisibility(GONE);
                        break;
                }
            }

            @Override
            protected void onError(@NonNull DatabaseError databaseError) {
                super.onError(databaseError);
                progressBar.setVisibility(GONE);
                databaseError.toException().printStackTrace();
                retry();

            }
        };
    }

    private void firestorePagination() {
        //Query
        Query query = usersCollection.orderBy("id", Query.Direction.ASCENDING);

        //Recycler Options
        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this) //with this you don't need adapter.startLoading() & adapter.stopLoading().
                //It will automatically bing them to the lifecycle methods of this activity
                .setQuery(query, pagedListConfig, new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        User user = snapshot.toObject(User.class);
                        String itemId = user.getId();
                        user.setId(itemId);
                        return user;
                    }
                })
                .build();

        fsAdapter = new FirestorePagingAdapter<User, UserViewHolder>(options) {
            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_layout_item, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                holder.txt_name.setText(model.getName());
                holder.txt_email.setText(model.getEmail());
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                super.onLoadingStateChanged(state);
                switch (state){
                    case LOADING_INITIAL:
                        Log.d("PAGING_LOG","Loading Initial Data");
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case LOADING_MORE:
                        Log.d("PAGING_LOG","Loading Next Page");
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case FINISHED:
                        Log.d("PAGING_LOG","All Data Loaded");
                        progressBar.setVisibility(GONE);
                        break;
                    case ERROR:
                        Log.d("PAGING_LOG","Error Loading Data");
                        progressBar.setVisibility(GONE);
                        break;
                    case LOADED:
                        Log.d("PAGING_LOG","Total items loaded: " + getItemCount());
                        progressBar.setVisibility(GONE);
                        break;
                }
            }
        };
    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        adapter.stopListening();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        adapter.startListening();
//    }

    //    private void getUsers(String nodeId) {
//        progressBar.setVisibility(View.VISIBLE);
//
//        Query query;
//
//        if (nodeId == null) {
//            query = usersDb
//                    .orderByKey()
//                    .limitToFirst(ITEMS_PER_PAGE);
//        } else {
//            query = usersDb
//                    .orderByKey()
//                    .startAt(nodeId)
//                    .limitToFirst(ITEMS_PER_PAGE);
//        }
//
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<User> users = new ArrayList<>();
//                if (dataSnapshot != null && dataSnapshot.exists()) {
//                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                        if (ds.getChildrenCount() > 0) {
//                            User user = ds.getValue(User.class);
//                            user.setId(ds.getKey());
//                            if (Utils.userExists(ds.getKey())) {
//                                reachedTheEnd = true;
//                            } else {
//                                reachedTheEnd = false;
//                                DataCache.add(user);
//                                users.add(user);
//
//                                Handler handler = new Handler();
//                                handler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
//                                    }
//                                }, 1500);
//                            }
//                        } else {
//                            Utils.show(mContext, "DataSnapshot count is 0");
//                        }
//                    }
//
//
//                } else {
//                    Utils.show(mContext, "DataSnapshot Doesn't Exist or is Null");
//                }
//                if (!reachedTheEnd) {
//                    adapter.addAll(users);
//                } else {
//                    //..
//                }
//                progressBar.setVisibility(GONE);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                progressBar.setVisibility(View.GONE);
//                Utils.show(mContext, databaseError.getMessage());
//            }
//        });
//    }
//
//    private void loadPaginated() {
//        DataCache = new ArrayList<>();
//        recyclerView.setAdapter(adapter);
//
//        getUsers(null);
//
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//                //Check for Scroll State
//                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                    isScrolling = true;
//                }
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                currentItems = layoutManager.getChildCount();
//                totalItems = layoutManager.getItemCount();
//                scrolledOutItems = ((LinearLayoutManager) (
//                        recyclerView.getLayoutManager()))
//                        .findFirstVisibleItemPosition();
//
//                if (isScrolling && (currentItems + scrolledOutItems == totalItems)) {
//                    isScrolling = false;
//
//                    if (dy < 0) {
//                        //Scrollin Down
//                        if (!reachedTheEnd) {
//                            getUsers(adapter.getLastItemId());
//                            progressBar.setVisibility(View.VISIBLE);
//                        } else {
//                            Utils.show(mContext, "No Items founds");
//                        }
//                    } else {
//                        //Scrolling Up
//                    }
//                }
//            }
//        });
//    }


    private void addNewUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Add new user");

        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 20, 10, 10);

        final EditText emailEt = new EditText(mContext);
        final EditText nameEt = new EditText(mContext);

        emailEt.setHint("user's email");
        nameEt.setHint("user's name");

        linearLayout.addView(emailEt, 0);
        linearLayout.addView(nameEt, 1);

        builder.setView(linearLayout);

        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                usersDb.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            long data = Long.parseLong(ds.getValue(User.class).getId());
                            //list.add(data);
                            long arr[] = {data};
                            max = arr[0];
                            for (int i = 0; i < arr.length; i++) {
                                if (arr[i] > max) {
                                    max = arr[i];
                                }
                            }
                            // mUserIds.clear();

                        }
                        Log.d("MainActivity", "MAX: " + max + 1);

                        User user1 = new User();
                        user1.setId(String.valueOf(max + 1));
                        user1.setName(nameEt.getText().toString());
                        user1.setEmail(emailEt.getText().toString());

                        //Set to Firebase database
                        usersDb.child(String.valueOf(max + 1)).setValue(user1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(mContext, "User added successfully", Toast.LENGTH_SHORT).show();
//                                                                    User user2 = new User(
//                                                                            String.valueOf(firestoreDocMaxId+1),
//                                                                            nameEt.getText().toString(),
//                                                                            emailEt.getText().toString()
//                                                                    );
//
//                                                                    //Set to Cloud Firestore
//                                                                    FirebaseFirestore.getInstance()
//                                                                            .collection("Users")
//                                                                            .document(String.valueOf(firestoreDocMaxId+1))
//                                                                            .set(user2)
//                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                                @Override
//                                                                                public void onSuccess(Void aVoid) {
//                                                                                    Toast.makeText(mContext,"User added successfully",Toast.LENGTH_SHORT).show();
//                                                                                }
//                                                                            });
                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //..
                    }
                });

            }
        });

        builder.show();
    }

}


