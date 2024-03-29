package com.example.shara.courseproject.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shara.courseproject.R;
import com.example.shara.courseproject.core.chat.ChatContract;
import com.example.shara.courseproject.core.chat.ChatPresenter;
import com.example.shara.courseproject.models.Chat;
import com.example.shara.courseproject.models.User;
import com.example.shara.courseproject.ui.adapters.ChatRecyclerAdapter;
import com.example.shara.courseproject.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatFragment extends Fragment implements ChatContract.View, TextView.OnEditorActionListener {
    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;
    private Button mSend;


    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private ChatPresenter mChatPresenter;

    String currentUser,Useruid;

    public static ChatFragment newInstance(String receiver,
                                           String receiverUid,
                                           String firstname) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_FIRSTNAME, firstname);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        bindViews(fragmentView);
        getActivity().setTitle(getArguments().getString(Constants.ARG_FIRSTNAME));
        mSend = (Button) fragmentView.findViewById(R.id.button2);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mETxtMessage.getText().toString().equals(""))
                    sendMessage();
            }
        });
        return fragmentView;
    }

    private void bindViews(View view) {
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) view.findViewById(R.id.edit_text_message);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        initCurrentUser();
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mETxtMessage.setOnEditorActionListener(this);

        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                getArguments().getString(Constants.ARG_RECEIVER_UID));
    }

    public void initCurrentUser()
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS)
                .orderByChild("uid")
                .equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    currentUser = user.firstname;
                    Useruid = user.uid;
                }
                else
                    Toast.makeText(getActivity(),"User does not exist",Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void sendMessage() {
        String message = mETxtMessage.getText().toString();
        String receiver = getArguments().getString(Constants.ARG_FIRSTNAME);
        String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
        String sender = currentUser;
        String senderUid = Useruid;
        String receiverFirebaseToken = null;
        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis());
        mChatPresenter.sendMessage(getActivity().getApplicationContext(),
                chat, receiverFirebaseToken);
    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText("");
        Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
