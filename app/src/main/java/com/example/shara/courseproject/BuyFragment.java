package com.example.shara.courseproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shara.courseproject.models.User;
import com.example.shara.courseproject.ui.fragments.ChatFragment;
import com.example.shara.courseproject.utils.Constants;
import com.example.shara.courseproject.utils.ItemClickSupport;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_LONG;
import static com.example.shara.courseproject.R.id.buy;

public class BuyFragment extends Fragment{

    TextView mRemove,mNo_Items;
    ListView listView ;
    ArrayAdapter<String> adapter;
    private ArrayList<String> full_item = new ArrayList<>();
    private ArrayList<String> itemList = new ArrayList<>();
    String currentUser;

    public BuyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_buy, container, false);
        getActivity().setTitle("Your Cart");

        mRemove = (TextView) v.findViewById(R.id.remove);
        listView = (ListView) v.findViewById(R.id.list);
        mNo_Items = (TextView) v.findViewById(R.id.no_items);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {
                arg1.setBackgroundResource(R.color.grey_300);

                mRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String toRemove = adapter.getItem(position);
                        adapter.remove(toRemove);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query applesQuery = ref.child(Constants.ARG_BOUGHT).child(currentUser).orderByChild("product").equalTo(full_item.get(position));

                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                    listView.setAdapter(null);
                                    listView.invalidateViews();
                                    itemList.clear();
                                    adapter.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                    }
                });
            }
        });
        return v;
    }

    public void getList() {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_BOUGHT).child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    int i=0;
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if(!snap.getValue(User.class).getProduct().equals("")) {
                            full_item.add(snap.getValue(User.class).getProduct());
                            String[] item = snap.getValue(User.class).getProduct().split("-");
                            if(!(item[1].toLowerCase()).equals("default")){
                            itemList.add(item[1]);
                            adapter.add(item[1].toUpperCase());
                            listView.setAdapter(adapter);}
                        }
                    }
                }
                else
                    mNo_Items.setText("No items");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void callChat(String uid)
    {
        //final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    User user = dataSnapshot.getValue(User.class);
                    goToChatActivity(user);
                }
                else{
                    Toast.makeText(getActivity(),"User does not exist in Firebase", LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(),"FAILED!",LENGTH_LONG).show();
            }
        });
    }

    public void goToChatActivity(User user){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ARG_FIRSTNAME,user.firstname);
        bundle.putString(Constants.ARG_RECEIVER_UID,user.uid);
        ChatFragment fragment = new ChatFragment();
        FragmentManager fm = getChildFragmentManager();
        fragment.setArguments(bundle);
        fm.beginTransaction().replace(R.id.buy, fragment).addToBackStack("null").commit();
    }
}

