package com.example.shara.courseproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shara.courseproject.models.User;
import com.example.shara.courseproject.utils.Constants;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static android.app.Activity.RESULT_OK;

public class SellFragment extends Fragment{

    private ArrayList<String> categoryList=new ArrayList();
    String[] catList;
    Uri uri;

    ImageView mImageView;
    ImageButton mGalleryButton;
    TextView mCurrency, mSetPrice, mSetLoc, mViewLoc;
    EditText mProductTitle, mProductDesc;
    Spinner mCategory;
    Button mPost;

    String loc=null;
    String[] categories;
    String selectedCategory;
    final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String cc;

    private static final int GALLERY_REQUEST_CODE = 1;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    StorageReference galleryPath;

    private static final int PLACE_PICKER_REQUEST = 0;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    SimpleDateFormat s;
    String format;
    String curUserLoc;
    boolean isCamera = false;
    boolean valid=false, checkImage=false;

    public SellFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_sell, container, false);

        getActivity().setTitle("Sell");
        init(v);
        setClick();
        setLocation();
        getLocation();
        setCategoryList();

        return v;
    }

    public void init(View v)
    {
        mImageView = (ImageView) v.findViewById(R.id.product_image);
        mGalleryButton = (ImageButton) v.findViewById(R.id.mGalleryButton);
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(getContext());

        mSetPrice = (TextView) v.findViewById(R.id.price);
        mSetLoc = (TextView) v.findViewById(R.id.location);
        mViewLoc = (TextView) v.findViewById(R.id.locView);

        mProductTitle = (EditText) v.findViewById(R.id.product_title);
        mProductDesc = (EditText) v.findViewById(R.id.describe_product);

        mCategory = (Spinner) v.findViewById(R.id.category_spinner);
        mCurrency = (TextView) v.findViewById(R.id.currency);

        mPost = (Button) v.findViewById(R.id.post);
    }

    public void setClick()
    {
        mViewLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                    Intent intent = intentBuilder.build(getActivity());
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        mSetLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                    Intent intent = intentBuilder.build(getActivity());
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_PICK);
                intent1.setType("image/*");
                startActivityForResult(intent1,GALLERY_REQUEST_CODE);
            }
        });

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkImage)
                {
                    Toast.makeText(getContext(),"Image required",Toast.LENGTH_LONG).show();
                    valid=false;
                }
                else
                    valid = true;

                if(mSetPrice.getText().toString().trim().equals(""))
                {
                    mSetPrice.setError("Price required");valid = false;
                }
                else
                    valid = true;

                if(mProductTitle.getText().toString().trim().equals(""))
                {
                    mProductTitle.setError("Title required");
                    valid=false;
                }
                else
                    valid = true;

                if(mProductDesc.getText().toString().trim().equals(""))
                {
                    mProductDesc.setError("Description required");
                    valid=false;
                }
                else
                    valid = true;

                if(!selectedCategory.equals("Select Category")&&valid) {
                    s = new SimpleDateFormat("ddMMyyyyhhmmss");
                    format = s.format(new Date());
                    if (!isCamera) {
                        mProgress.setMessage("uploading");
                        mProgress.show();
                        galleryPath = mStorage.child("Photos").child(currentUser + "-" + mProductTitle.getText().toString().trim().toLowerCase());
                        galleryPath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mProgress.dismiss();
                                addUserToFirebase();
                                Toast.makeText(getActivity(), "Upload Complete!!!", Toast.LENGTH_LONG).show();
                                HomeFragment fragment = new HomeFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("signedIn", "SignedIn");
                                fragment.setArguments(bundle);
                                getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
                            }
                        });
                    }
                    }
                }
        });
    }

    public void getCurrency()
    {
        List<Address> address = null;
        try {
            Geocoder gc = new Geocoder(getContext());
            List<Address> addresses= gc.getFromLocationName(curUserLoc, 5);
            List<LatLng> ll = new ArrayList<LatLng>(addresses.size());
            for(Address a : addresses){
                if(a.hasLatitude() && a.hasLongitude()){
                    address = gc.getFromLocation(a.getLatitude(), a.getLongitude(), 1);
                }
            }
        } catch (IOException e) {
        }
        Address obj = address.get(0);
        cc = Currency.getInstance(obj.getLocale()).getSymbol();
        mCurrency.setText(cc);
    }

    public void getLocation()
    {
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if(snap.getValue(User.class).getUid().equals(currentUser)) {
                            curUserLoc = (snap.getValue(User.class).getLocation());
                            getCurrency();
                        }
                    }
                }
               /* else
                    mViewLoc.setError("Set Location");*/
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void addUserToFirebase() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currency = cc;
        String price = mSetPrice.getText().toString().trim();
        String title = mProductTitle.getText().toString().trim();
        String desc = mProductDesc.getText().toString().trim();
        String category = selectedCategory;
        String img_path = "Photos/"+currentUser+"-"+mProductTitle.getText().toString().trim();
        User user = new User(currentUser,currency,price,title,desc,category,img_path,format.trim());
        database.child(Constants.ARG_PHOTOS)
                .child(currentUser+"-"+mProductTitle.getText().toString().trim())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                          //  Toast.makeText(getActivity(), "Added to Firebase", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "Submit Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PLACE_PICKER_REQUEST:
                    final Place place = PlacePicker.getPlace(getActivity(), data);
                    String address = (String) place.getAddress();
                    if (address != null) {
                        mViewLoc.setText(address);
                    }
                    break;
                case GALLERY_REQUEST_CODE:
                    uri = data.getData();
                    mImageView.setImageURI(uri);
                    checkImage=true;

                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void setCategoryList(){
        if(isAdded()){
            catList = getResources().getStringArray(R.array.category_list);
        }
        categoryList.add("Select Category");
        for(String str : catList)
        {
            categoryList.add(str);
        }
        String category = null;
        while ((category) != null) {
            categoryList.add(category);
        }
        categories = new String[categoryList.size()];
        categories = categoryList.toArray(categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, categories);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        mCategory.setAdapter(adapter);
        mCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory = categories[i];
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    public void setLocation()
    {
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if(snap.getValue(User.class).getEmail().equals(currentUser)) {
                            loc = (snap.getValue(User.class).getLocation());
                            mViewLoc.setText(loc);
                        }
                    }
                }
                else
                    mViewLoc.setError("Location");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
