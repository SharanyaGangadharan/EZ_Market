package com.example.shara.courseproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shara.courseproject.models.User;
import com.example.shara.courseproject.ui.fragments.ChatFragment;
import com.example.shara.courseproject.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_LONG;
import static com.example.shara.courseproject.utils.Constants.ARG_BOUGHT;

public class DetailsActivity extends Fragment {

    TextView priceTextView;
    ImageView imageView;
    String title;
    Bitmap bitmap;
    Uri uri;
    String uid,firstname,lastname,location,prod_title,prod_desc;
    TextView mUser, mLoc, mTitle, mDesc;
    Button mChat,mOffer;
    StorageReference ref;
    final HashMap<String, HashMap<String, String>> userInfo = new HashMap<>();
    int check;
    final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_details, container, false);

        title = getArguments().getString("price");
        bitmap = (Bitmap) this.getArguments().getParcelable("Bitmap");
        uid = getArguments().getString("uid");

        imageView = (ImageView) v.findViewById(R.id.image);
        priceTextView = (TextView) v.findViewById(R.id.title);
        mUser = (TextView) v.findViewById(R.id.user_name);
        mLoc = (TextView) v.findViewById(R.id.user_loc);
        mTitle = (TextView) v.findViewById(R.id.prod_title);
        mDesc = (TextView) v.findViewById(R.id.prod_desc);
        mChat = (Button) v.findViewById(R.id.chat);
        mOffer = (Button) v.findViewById(R.id.make_offer);

        findLoc();
        getUserInfo(bitmap);

        mChat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!currentUser.equals(uid)){
                callChat();}
            }
        });
        mOffer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!currentUser.equals(uid)){
                    getList();}
                }
        });
        return v;
    }

    public void getList() {
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_BOUGHT).child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    int i = 0;
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if (snap.getValue(User.class).getProduct().equals(uid + "-" + prod_title)) {
                            i++;
                        }
                    }
                    if (i == 0) {
                        addDataToFirebase();
                    } else
                    {
                        CartFragment fragment = new CartFragment();
                        getFragmentManager().beginTransaction().replace(R.id.content, fragment).addToBackStack(null).commit();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void addDataToFirebase() {
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String product = uid+"-"+prod_title;
        Map<String, Object> data= new HashMap<String, Object>();
        data.put(product,data);
        User user = new User(product);
        database.child(ARG_BOUGHT)
                .child(currentUser)
                .child(format)
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Added to Firebase", Toast.LENGTH_LONG).show();
                            CartFragment fragment = new CartFragment();
                            getFragmentManager().beginTransaction().replace(R.id.content, fragment).addToBackStack(null).commit();

                        } else {
                            Toast.makeText(getContext(), "Submit Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean equalTo(Bitmap bitmap1, Bitmap bitmap2) {
        ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
        bitmap1.copyPixelsToBuffer(buffer1);

        ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
        bitmap2.copyPixelsToBuffer(buffer2);

        return Arrays.equals(buffer1.array(), buffer2.array());
    }

    public void getUserInfo(final Bitmap img)
    {
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
                                    options.inSampleSize = 10;
                                    Bitmap bitmap1 = BitmapFactory.decodeFile(localFile.getAbsolutePath(), options);
                                    try {
                                        if(equalTo(bitmap1,img)){
                                            uid = snap.getValue(User.class).getUid();
                                            firstname = userInfo.get(uid).get("firstname");
                                            lastname = userInfo.get(uid).get("lastname");
                                            location = userInfo.get(uid).get("location");
                                            prod_title = snap.getValue(User.class).getItem_title();
                                            prod_desc = snap.getValue(User.class).getItem_desc();

                                            call(img);
                                        }
                                    } catch (Exception e) {
                                        e.getMessage();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
//                                    Toast.makeText(getContext(), "FAILED", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (IOException e) {
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
               // out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void call(Bitmap bitmap)
    {
        priceTextView.setText(title);
        mUser.setText(firstname+" "+lastname);
        mLoc.setText(location);
        mTitle.setText(prod_title);
        mDesc.setText(prod_desc);
        //Bitmap resized = Bitmap.createScaledBitmap(bitmap,960,900,false);
        imageView.setImageBitmap(bitmap);
    }

    public void findLoc() {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        String uId = snap.getValue(User.class).getUid();
                        String firstname = snap.getValue(User.class).getFirstname();
                        String lastname = snap.getValue(User.class).getLastname();
                        String location = snap.getValue(User.class).getLocation();
                        HashMap<String, String> attr = new HashMap<String, String>();
                        attr.put("firstname", firstname);
                        attr.put("lastname", lastname);
                        attr.put("location", location);
                        userInfo.put(uId, attr);
                    }
                } else {
                    Toast.makeText(getActivity(), "User does not exist in Firebase", LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "FAILED!", LENGTH_LONG).show();
            }
        });
    }


    public void callChat()
    {
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.getUid().equals(currentUser)) {
                        if (TextUtils.equals(user.uid, uid)) {
                            goToChatActivity(user);
                        }
                    }
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
        FragmentManager fm = getFragmentManager();
        fragment.setArguments(bundle);
        fm.beginTransaction().replace(R.id.content, fragment).addToBackStack("tag").commit();
    }
}