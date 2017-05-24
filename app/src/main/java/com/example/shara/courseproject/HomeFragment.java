package com.example.shara.courseproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shara.courseproject.models.User;
import com.example.shara.courseproject.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_LONG;
import static java.lang.Math.sqrt;

public class HomeFragment extends Fragment {

    TextView mLocation, mLocText, mAddFilter, mFilter;
    private static final int INTENT_RESULT_CODE = 123;
    StorageReference ref;
    int i = 1;
    int min = 0, max = 0;
    String cc;
    boolean isCostSet = false, isLocSet = false, isCatSet = false;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    final ArrayList<ImageItem> imageItems = new ArrayList<>();

    Double lat = 32.715736, lng = -117.161087;
    String loc;
    int miles;
    String uid;
    String filter_cat = null;
    String signedIn = null;
    final HashMap<String, String> detail = new HashMap<String, String>();
    final HashMap<String, HashMap<String, String>> userInfo = new HashMap<>();
    private static final int FRAGMENT_CODE = 0;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mLocation = (TextView) v.findViewById(R.id.change_location);
        mLocText = (TextView) v.findViewById(R.id.view_location);
        gridView = (GridView) v.findViewById(R.id.gridView);
        mAddFilter = (TextView) v.findViewById(R.id.add_filter);
        mFilter = (TextView) v.findViewById(R.id.filter);
        findLoc();

        getActivity().setTitle("Home");
        //getCurrency();

        signedIn = getArguments().getString("signedIn");
        try {
            getCurrency();
            filter_cat = getArguments().getString("cat");
            min = getArguments().getInt("min");
            max = getArguments().getInt("max");
            if (!filter_cat.trim().isEmpty() && max > 0) {
                SharedPreferences prefs = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                String restoredText = prefs.getString("text", "hello");
                if (restoredText != null) {
                    loc = prefs.getString("loc", "null");
                    miles = prefs.getInt("miles", 0);
                    if(loc.isEmpty()) isLocSet=false;
                    //isLocSet = true;
                }
                filter_cost();
                if (!isLocSet)
                    findLoc();
            } else if (!filter_cat.trim().isEmpty()) {
                catFilter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (signedIn.equals("SignedIn")) {
                if (getActivity() != null) {
                    home();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        signedIn = null;
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                //Toast.makeText(getContext(),item.getTitle(),Toast.LENGTH_LONG).show();
                //Bitmap convertedImage = getResizedBitmap(img, 400);
                //getUserInfo(item.getImage());
                final Bitmap img = item.getImage();
                DetailsActivity fragment = new DetailsActivity();
                Bundle bundle = new Bundle();
                bundle.putString("uid", uid);
                bundle.putString("price", item.getTitle());
               /* bundle.putString("firstname", firstname);
                bundle.putString("lastname", lastname);
                bundle.putString("location", location);
                bundle.putString("prod_title", prod_title);
                bundle.putString("prod_desc", prod_desc);
               */ bundle.putParcelable("Bitmap", img);
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.content, fragment).addToBackStack(null).commit();

                /*AsyncTaskRunner myTask = new AsyncTaskRunner();
                myTask.execute(item);*/
            }
        });

        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SetArea.class);
                startActivityForResult(intent, INTENT_RESULT_CODE);
            }
        });

        mAddFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment fragment = new FilterFragment();
                fragment.setTargetFragment(HomeFragment.this, FRAGMENT_CODE);
                Bundle bundle = new Bundle();
                bundle.putString("currency", cc);
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.content, fragment).addToBackStack(null).commit();
            }
        });
        return v;
    }

    public void getCurrency() {
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address obj = addresses.get(0);
        cc = Currency.getInstance(obj.getLocale()).getSymbol();
    }

    public void home() {
        imageItems.clear();
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
                                        uid = snap.getValue(User.class).getUid();
                                        //getUserInfo(bitmap);
                                        /*prod_title = snap.getValue(User.class).getItem_title();
                                        prod_desc = snap.getValue(User.class).getItem_desc();
                                        */imageItems.add(new ImageItem(bitmap, snap.getValue(User.class).getCurrency() + snap.getValue(User.class).getPrice()));
                                        gridAdapter = new GridViewAdapter(getContext(), R.layout.grid_item, imageItems);//imageItems);
                                        gridView.setAdapter(gridAdapter);
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

    public void catFilter() {
        isCatSet = true;
        imageItems.clear();
        getActivity().setTitle(filter_cat);
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
                                    try {
                                        if (filter_cat.equals(snap.getValue(User.class).getCategory())) {
                                            if (isAdded()) {
                                                uid = snap.getValue(User.class).getUid();
                                                //getUserInfo(bitmap);
                                                /*prod_title = snap.getValue(User.class).getItem_title();
                                                prod_desc = snap.getValue(User.class).getItem_desc();
                                                */imageItems.add(new ImageItem(bitmap, snap.getValue(User.class).getCurrency() + snap.getValue(User.class).getPrice()));
                                                gridAdapter = new GridViewAdapter(getContext(), R.layout.grid_item, imageItems);//imageItems);
                                                gridView.setAdapter(gridAdapter);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.getMessage();
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

    public void locFilter() {
        isLocSet = true;
        imageItems.clear();
        getActivity().setTitle(filter_cat);
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
                                    try {
                                        uid = snap.getValue(User.class).getUid();
                                        //getUserInfo(bitmap);
                                        if ((loc.toLowerCase()).equals(detail.get(uid).toLowerCase())) {
                                            if (isCostSet) {
                                                if (cc.equals(snap.getValue(User.class).getCurrency())
                                                        && min <= Integer.parseInt(snap.getValue(User.class).getPrice())
                                                        && max >= Integer.parseInt(snap.getValue(User.class).getPrice())
                                                        && filter_cat.equals(snap.getValue(User.class).getCategory())) {
                                                    if (isAdded()) {
                                                        /*prod_title = snap.getValue(User.class).getItem_title();
                                                        prod_desc = snap.getValue(User.class).getItem_desc();
                                                        */ImageItem item = new ImageItem(bitmap, snap.getValue(User.class).getCurrency() + snap.getValue(User.class).getPrice());
                                                        if (!contains(imageItems, bitmap)) {
                                                            imageItems.add(item);
                                                            gridAdapter = new GridViewAdapter(getContext(), R.layout.grid_item, imageItems);//imageItems);
                                                            gridView.setAdapter(gridAdapter);
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (isCatSet) {
                                                    if (filter_cat.equals(snap.getValue(User.class).getCategory())) {
                                                        if (isAdded()) {
                                                            /*prod_title = snap.getValue(User.class).getItem_title();
                                                            prod_desc = snap.getValue(User.class).getItem_desc();
                                                            */imageItems.add(new ImageItem(bitmap, snap.getValue(User.class).getCurrency() + snap.getValue(User.class).getPrice()));
                                                            gridAdapter = new GridViewAdapter(getContext(), R.layout.grid_item, imageItems);//imageItems);
                                                            gridView.setAdapter(gridAdapter);
                                                        }
                                                    }
                                                } else {
                                                    if (isAdded()) {
                                                        /*prod_title = snap.getValue(User.class).getItem_title();
                                                        prod_desc = snap.getValue(User.class).getItem_desc();
                                                        */ImageItem item = new ImageItem(bitmap, snap.getValue(User.class).getCurrency() + snap.getValue(User.class).getPrice());
                                                        if (!contains(imageItems, bitmap)) {
                                                            imageItems.add(item);
                                                            gridAdapter = new GridViewAdapter(getContext(), R.layout.grid_item, imageItems);//imageItems);
                                                            gridView.setAdapter(gridAdapter);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.getMessage();
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

    boolean contains(ArrayList<ImageItem> list, Bitmap bitmap) {
        for (ImageItem item : list) {
            if (equalTo(bitmap, item.getImage())) {
                return true;
            }
        }
        return false;
    }

    public boolean equalTo(Bitmap bitmap1, Bitmap bitmap2) {
        ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
        bitmap1.copyPixelsToBuffer(buffer1);

        ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
        bitmap2.copyPixelsToBuffer(buffer2);

        return Arrays.equals(buffer1.array(), buffer2.array());
    }

    public void filter_cost() {
        isCostSet = true;
        imageItems.clear();
        getActivity().setTitle(filter_cat);
        mFilter.setText(cc + min + " - " + cc + max);
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
                                    try {
                                        //getUserInfo(bitmap);
                                        if ((cc.equals(snap.getValue(User.class).getCurrency())
                                                && min <= Integer.parseInt(snap.getValue(User.class).getPrice())
                                                && max >= Integer.parseInt(snap.getValue(User.class).getPrice()))
                                                && filter_cat.equals(snap.getValue(User.class).getCategory())) {
                                            if (isLocSet) {
                                                mLocText.setText(loc + " . ");
                                                mLocText.append(miles + " miles");
                                                uid = snap.getValue(User.class).getUid();
                                                if ((loc.toLowerCase()).equals(detail.get(uid).toLowerCase())) {
                                                    if (isAdded()) {
                                                        /*prod_title = snap.getValue(User.class).getItem_title();
                                                        prod_desc = snap.getValue(User.class).getItem_desc();
                                                        */ImageItem item = new ImageItem(bitmap, snap.getValue(User.class).getCurrency() + snap.getValue(User.class).getPrice());
                                                        if (!contains(imageItems, bitmap)) {
                                                            imageItems.add(item);
                                                            gridAdapter = new GridViewAdapter(getContext(), R.layout.grid_item, imageItems);//imageItems);
                                                            gridView.setAdapter(gridAdapter);
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (isAdded()) {
                                                    /*prod_title = snap.getValue(User.class).getItem_title();
                                                    prod_desc = snap.getValue(User.class).getItem_desc();
                                                    */ImageItem item = new ImageItem(bitmap, snap.getValue(User.class).getCurrency() + snap.getValue(User.class).getPrice());
                                                    if (!contains(imageItems, bitmap)) {
                                                        imageItems.add(item);
                                                        gridAdapter = new GridViewAdapter(getContext(), R.layout.grid_item, imageItems);//imageItems);
                                                        gridView.setAdapter(gridAdapter);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.getMessage();
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

    public void findLoc() {
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if (currentUser.equals(snap.getValue(User.class).getUid())) {
                            String curUL = snap.getValue(User.class).getLocation();
                            String[] section = curUL.split(", ");
                            loc = section[0].toLowerCase();
                        }
                        String uId = snap.getValue(User.class).getUid();
                        String firstname = snap.getValue(User.class).getFirstname();
                        String lastname = snap.getValue(User.class).getLastname();
                        String location = snap.getValue(User.class).getLocation();
                        String lOc = snap.getValue(User.class).getLocation();
                        String[] part = lOc.split(", ");
                        detail.put(uId, part[0]);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_RESULT_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    lat = data.getDoubleExtra("lat", 0.0);
                    lng = data.getDoubleExtra("lng", 0.0);
                    loc = data.getStringExtra("loc").toLowerCase();
                    miles = data.getIntExtra("miles", 0);

                    mLocText.setText(loc + " . ");
                    mLocText.append(miles + " miles");
                    locFilter();
                    //findLoc();
                    SharedPreferences.Editor editor = getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("loc", loc);
                    editor.putInt("miles", miles);
                    editor.commit();
                    Log.d("rew", "Lat" + lat);
                    Log.d("rew", "Long" + lng);
                    break;
                case RESULT_CANCELED:
                    break;
            }
        }
    }
}


    /*public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }*/


