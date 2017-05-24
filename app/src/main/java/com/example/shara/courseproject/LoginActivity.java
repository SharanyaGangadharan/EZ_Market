package com.example.shara.courseproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    EditText mPassword, mEmail;
    Button mSignIn;
    TextView mRegister, mErrorMessage;
    Boolean valid=false ;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    String email, password;
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mSignIn = (Button) findViewById(R.id.sign_in);
        mRegister = (TextView) findViewById(R.id.register);
        mErrorMessage = (TextView) findViewById(R.id.error);

        email = mEmail.getText().toString().trim();
        password = mPassword.getText().toString().trim();

        mSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                validate();
                if(valid){
                    signIn(mEmail.getText().toString().trim(),mPassword.getText().toString().trim());
                }
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent go1 = new Intent(LoginActivity.this, RegisterFragment.class);
                startActivity(go1);
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i("rew", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.i("rew", "onAuthStateChanged:signed_out");
                }
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void validate() {

        //email validation
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(mEmail.getText().toString().trim());
        if (mEmail.getText().toString().trim().equals("")) {
            mEmail.setError("Email required");
            valid=false;}
        else if(!matcher.matches()){
            mEmail.setError("Invalid Email");
            valid=false;}
        else
            valid = true;

        //password validation
        if (mPassword.getText().toString().trim().equals("")) {
            mPassword.setError("Password required");
            valid=false;
        }
        else if(mPassword.getText().toString().trim().length()<=3) {
            mPassword.setError("Invalid Password");
            valid = false;
        }
        else
            valid=true;
    }

    public void signIn(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("rew", "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.i("rew", "signInWithEmail:failed", task.getException());
                            //Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            mErrorMessage.setText("Invalid email or password");
                        }
                        else {
                            Intent go = new Intent(LoginActivity.this, BottomNavigation.class);
                            startActivity(go);
                            mErrorMessage.setText("");
                            //Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}


