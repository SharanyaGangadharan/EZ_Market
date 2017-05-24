package com.example.shara.courseproject.core.users.add;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.shara.courseproject.R;
import com.example.shara.courseproject.models.User;
import com.example.shara.courseproject.utils.Constants;
import com.example.shara.courseproject.utils.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class AddUserInteractor implements AddUserContract.Interactor {
    private AddUserContract.OnUserDatabaseListener mOnUserDatabaseListener;

    public AddUserInteractor(AddUserContract.OnUserDatabaseListener onUserDatabaseListener) {
        this.mOnUserDatabaseListener = onUserDatabaseListener;
    }

    @Override
    public void addUserToDatabase(final Context context, FirebaseUser firebaseUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        User user = new User(firebaseUser.getUid(),
                firebaseUser.getEmail(),
                new SharedPrefUtil(context).getString(Constants.ARG_FIRSTNAME),
                new SharedPrefUtil(context).getString(Constants.ARG_LASTNAME),
                        new SharedPrefUtil(context).getString(Constants.ARG_MOBILE),
                new SharedPrefUtil(context).getString(Constants.ARG_BIRTHDATE),
                new SharedPrefUtil(context).getString(Constants.ARG_GENDER),
                new SharedPrefUtil(context).getString(Constants.ARG_LOCATION));
        database.child(Constants.ARG_USERS)
                .child(firebaseUser.getUid())
                .setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mOnUserDatabaseListener.onSuccess(context.getString(R.string.user_successfully_added));
                        } else {
                            mOnUserDatabaseListener.onFailure(context.getString(R.string.user_unable_to_add));
                        }
                    }
                });
    }
}
