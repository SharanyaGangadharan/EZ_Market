package com.example.shara.courseproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shara.courseproject.models.User;
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

public class ItemFragment extends Fragment{

    TextView mSold, mRemove;
    ListView listView ;
    StorageReference ref;
    ArrayAdapter<String> adapter;
    private ArrayList<String> itemList = new ArrayList<>();
    int pos=0;

    public ItemFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_item, container, false);
        getActivity().setTitle("Your Cart");

        mSold = (TextView) v.findViewById(R.id.sold);
        mRemove = (TextView) v.findViewById(R.id.remove);
        listView = (ListView) v.findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);

        getList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {
                arg1.setBackgroundResource(R.color.grey_300);

                mSold.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String toRemove = adapter.getItem(position);
                        adapter.remove(toRemove);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query applesQuery = ref.child("photos").orderByChild("item_title").equalTo(itemList.get(position).toLowerCase());

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

                mRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String toRemove = adapter.getItem(position);
                        adapter.remove(toRemove);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query applesQuery = ref.child("photos").orderByChild("item_title").equalTo(itemList.get(position).toLowerCase());

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

    public void getList()
    {
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref1 = database.getReference().child("photos");
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (final DataSnapshot snap : dataSnapshot.getChildren()) {
                        try {
                            ref = storage.getReference().child(snap.getValue(User.class).getImage_path());
                            final File localFile = File.createTempFile("images", "jpg");
                            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = 8;
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath(), options);
                                    if (isAdded()) {
                                        String uid = snap.getValue(User.class).getUid();
                                        if(currentUser.equals(uid)) {
                                            itemList.add(snap.getValue(User.class).getItem_title().toUpperCase());
                                            adapter.add(snap.getValue(User.class).getItem_title().toUpperCase());
                                            listView.setAdapter(adapter);
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(getContext(), "FAILED", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (IOException e) {
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }
}

