package com.example.shara.courseproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.shara.courseproject.ui.fragments.ChatFragment;
import com.example.shara.courseproject.utils.Constants;

public class ChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    public static void startActivity(Context context,
                                     String receiver,
                                     String receiverUid,
                                     String firstname) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiver);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(Constants.ARG_FIRSTNAME, firstname);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bindViews();
        init();
    }

    private void bindViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void init() {
        // set the toolbar
        setSupportActionBar(mToolbar);

        // set toolbar title
        mToolbar.setTitle(getIntent().getExtras().getString(Constants.ARG_RECEIVER));

        // set the register screen fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_content_chat,
                ChatFragment.newInstance(getIntent().getExtras().getString(Constants.ARG_RECEIVER),
                        getIntent().getExtras().getString(Constants.ARG_RECEIVER_UID),
                        getIntent().getExtras().getString(Constants.ARG_FIRSTNAME)),
                ChatFragment.class.getSimpleName());
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseChatMainApp.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseChatMainApp.setChatActivityOpen(false);
    }
}