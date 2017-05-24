package com.example.shara.courseproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shara.courseproject.models.User;
import com.example.shara.courseproject.ui.fragments.ChatFragment;
import com.example.shara.courseproject.utils.Constants;
import com.example.shara.courseproject.utils.ItemClickSupport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment implements ItemClickSupport.OnItemClickListener {

    List<User> usersList = new ArrayList<>();
    boolean check=false;

    private RecyclerView recyclerView;
    private UserListAdapter mAdapter;
    private ArrayList<String> uidList = new ArrayList<>();
    private ArrayList<String> parentList = new ArrayList<>();
    final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public ChatListFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new com.example.shara.courseproject.DividerItemDecoration(getActivity(), com.example.shara.courseproject.DividerItemDecoration.VERTICAL_LIST));

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(usersList==null || usersList.isEmpty()){
            display();
        }
        else
        {
            UserListAdapter mAdapter1 = new UserListAdapter(usersList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter1);
        }
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        User user = (((UserListAdapter)recyclerView.getAdapter()).getUser(position));
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ARG_FIRSTNAME,user.firstname);
        bundle.putString(Constants.ARG_RECEIVER_UID,user.uid);
        ChatFragment fragment = new ChatFragment();
        FragmentManager fm = getFragmentManager();
        fragment.setArguments(bundle);
        fm.beginTransaction().replace(R.id.content, fragment).addToBackStack("tag").commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_chat_list, container, false);
        getActivity().setTitle("Chat");
        return v;
    }

    public void display() {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if((!snap.getValue(User.class).getUid().equals(currentUser)))
                            usersList.add((User) snap.getValue(User.class));
                    }
                    mAdapter = new UserListAdapter(usersList);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(mAdapter);
                }
                else
                    Toast.makeText(getActivity(),"User does not exist",Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*public void getList() {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_BOUGHT).child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if(!snap.getValue(User.class).getProduct().equals("")) {
                            String[] item = snap.getValue(User.class).getProduct().split("-");
                            uidList.add(item[0]);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getSellList(String parent) {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_BOUGHT).child(parent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if(!snap.getValue(User.class).getProduct().equals("")) {
                            String[] item = snap.getValue(User.class).getProduct().split("-");
                            if(!item[0].equals(currentUser)){
                                uidList.add(snap.getKey());
                            }
                        }
                       // parentList.add(dataSnapshot.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

}