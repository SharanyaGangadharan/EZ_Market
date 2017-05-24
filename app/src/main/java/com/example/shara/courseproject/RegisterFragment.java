package com.example.shara.courseproject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.shara.courseproject.utils.Constants.ARG_BOUGHT;
import static java.security.AccessController.getContext;

public class RegisterFragment extends FragmentActivity implements DatePickerDialog.OnDateSetListener{

    public RegisterFragment() {
    }

    private static final int PLACE_PICKER_REQUEST = 1;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    EditText mEmail, mPwd, mFirstName, mLastName, mMobileNumber;
    RadioGroup mGender;
    RadioButton mFemale;
    TextView mLocation,mBirthdate;
    Button mLoc, mDOB;
    String uid;

    String gender=null, address[]=null;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_])(?=\\S+$).{8,}$";

    boolean valid = true, isAccountCreated = true;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);
        mAuth = FirebaseAuth.getInstance();

        mEmail = (EditText) findViewById(R.id.email);
        mPwd = (EditText) findViewById(R.id.password);
        mFirstName = (EditText) findViewById(R.id.first_name);
        mLastName = (EditText) findViewById(R.id.last_name);
        mMobileNumber = (EditText) findViewById(R.id.mobile_number);

        mDOB = (Button) findViewById(R.id.select_dob);
        mBirthdate = (TextView) findViewById(R.id.birthday);

        mGender = (RadioGroup) findViewById(R.id.gender);
        mFemale = (RadioButton) findViewById(R.id.radio_female);

        mLocation = (TextView) findViewById(R.id.location);
        mLoc = (Button) findViewById(R.id.select_location);
    }

    public void datepicker(View view)
    {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(),"date");
    }

    private void setDate(final Calendar calendar)
    {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        mBirthdate.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = new GregorianCalendar(year,month,dayOfMonth);
        setDate(cal);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radio_male:
                if (checked)
                    gender = "Male";
                    break;
            case R.id.radio_female:
                if (checked)
                    gender = "Female";
                    break;
        }
    }

    public static class DatePickerFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getContext(),
                    (DatePickerDialog.OnDateSetListener) getActivity(),year,month,day);
        }
    }

    public void setLocation(View view)
    {
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
            Intent intent = intentBuilder.build(RegisterFragment.this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            final Place place = PlacePicker.getPlace(this, data);
            final CharSequence name = place.getName();
            address = place.getAddress().toString().split(", ");
            mLocation.setText(address[0]+", "+address[1]+", "+address[2]);
            setResult(RESULT_OK, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void addUserToFirebase(FirebaseUser firebaseuser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String email = mEmail.getText().toString().trim();
        String fname = mFirstName.getText().toString().trim();
        String lname = mLastName.getText().toString().trim();
        String mobile = mMobileNumber.getText().toString().trim();
        String birthdate = mBirthdate.getText().toString().trim();
        String location = mLocation.getText().toString().trim();
        uid = firebaseuser.getUid();

        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());
        User user = new User(firebaseuser.getUid(),email,fname,lname,mobile,birthdate,gender,location,format);
        database.child(Constants.ARG_USERS)
                .child(uid)
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // mOnUserDatabaseListener.onSuccess(context.getString(R.string.user_successfully_added));
                        Toast.makeText(getApplicationContext(), "Added to Firebase", Toast.LENGTH_LONG).show();
                            addToBought();
                        } else {
                            // mOnUserDatabaseListener.onFailure(context.getString(R.string.user_unable_to_add));
                            Toast.makeText(getApplicationContext(), "Submit Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void addToBought()
    {
        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final String product = uid+"-default";
        User user = new User(product);
        database.child(ARG_BOUGHT)
                .child(uid)
                .child(format)
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // mOnUserDatabaseListener.onSuccess(context.getString(R.string.user_successfully_added));
                        } else {
                            // mOnUserDatabaseListener.onFailure(context.getString(R.string.user_unable_to_add));
                            Toast.makeText(getApplicationContext(), "Submit Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean createAccount()
    {
        String email = mEmail.getText().toString().toLowerCase().trim();
        String password = mPwd.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("rew", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(RegisterFragment.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            isAccountCreated = false;
                        }
                        else {
                            addUserToFirebase(task.getResult().getUser());
                            call();
                        }
                    }
                });
        return isAccountCreated;
    }

    public void submit(View view) {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(mEmail.getText().toString().trim());
        Pattern pattern1 = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher1 = pattern1.matcher(mPwd.getText().toString().trim());

        //email validation
        if (mEmail.getText().toString().trim().equals("")) {
            mEmail.setError("Email required");
            valid=false;}
        else if(!matcher.matches()){
            mEmail.setError("Invalid Email");
            valid=false;}
        else if(matcher.matches()){
            mAuth.fetchProvidersForEmail(mEmail.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if (task.isSuccessful()) {
                                //getProviders() will return size 1. if email ID is available.
                                if(task.getResult().getProviders().size()==1){
                                valid = false;
                                mEmail.setError("Email already exists");}
                                else
                                    valid =true;
                            }
                        }
                    });
        }
        //else valid = true;

        //password validation
        if(valid){
            if (mPwd.getText().toString().trim().equals("")) {
                mPwd.setError("Password required");
                valid=false;
            }
            else
                valid = true;
        }

        if(valid){
                    if(!matcher1.matches())
                    {
                        mPwd.setError("Invalid Password");
                        Toast.makeText(getApplicationContext(),"Password must contain the following:" + "\n" +
                                "A digit" + "\n" +
                                "A lowercase letter" + "\n" +
                                "An uppercase letter" + "\n" +
                                "A special character [@#$%^&+=_]" + "\n" +
                                "No whitespaces" + "\n" +
                                "Length : 8",Toast.LENGTH_LONG).show();
                        valid=false;
                    }
                    else
                        valid = true;
                }


                //firstname validation
        if(valid){
                    if (mFirstName.getText().toString().trim().equals("")) {
                        mFirstName.setError("First Name required");
                        valid=false;
                    }
                    else
                        valid = true;
                }

                //lastname validation
        if(valid){
                    if (mLastName.getText().toString().trim().equals("")) {
                        mLastName.setError("First Name required");
                        valid=false;
                    }
                    else
                        valid = true;
                }

        //lastname validation
        int len = mMobileNumber.getText().toString().trim().length();
        if(valid){
            if (mMobileNumber.getText().toString().trim().equals("")) {
                mMobileNumber.setError("Mobile number required");
                valid=false;
            }
            else if(len!=10){
                valid = false;
                mMobileNumber.setError("Invalid number");
            }
            else
                valid = true;
        }

        /*if(valid)
        {
            //final String userMob = mMobileNumber.getText().toString().trim();
            FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            if(snap.getValue(User.class).getMobile().equals(mMobileNumber.getText().toString().trim())){
                                mMobileNumber.setError("Mobile number already exists");
                                valid = false;}
                        }
                    }
                    *//*else
                        valid = true;*//*
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }*/

        //birthdate validation
        if(valid){
            if (mBirthdate.getText().toString().trim().equals("")) {
                mBirthdate.setError("Birthdate required");
                valid=false;
            }
            else{
                mBirthdate.setError(null);
                valid = true;}
        }

                //gender validation
        if(valid){
                    if (mGender.getCheckedRadioButtonId()==-1) {
                        mFemale.setError("Gender required");
                        valid=false;
                    }
                    else{
                        mFemale.setError(null);
                        valid = true;}
                }

                //address validation
        if(valid){
                    if(mLocation.getText().toString().trim().equals("")) {
                        mLocation.setError("Address Required!");
                        valid=false;
                    }
                    else
                        valid = true;
                }


                if(valid) {
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    createAccount();
                }
            }


    public void call()
    {
        Intent login = new Intent(this,LoginActivity.class);
        startActivity(login);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
