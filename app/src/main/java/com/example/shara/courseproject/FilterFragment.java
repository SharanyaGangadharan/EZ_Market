package com.example.shara.courseproject;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shara.courseproject.utils.ItemClickSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;


public class FilterFragment extends Fragment implements ItemClickSupport.OnItemClickListener{

    String[] catList;
    String cc, selectedCategory;
    int max=1000;
    int startLimit=0, endLimit=0;
    SeekBar mSeekStart, mSeekEnd;
    TextView mStartLimit,mEndLimit, mReset;
    Button inc, dec, mCancel, mConfirm;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> categoryList=new ArrayList();
    private ArrayList<Integer> imageList=new ArrayList();
    int[] images = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    //String loc;
    private static final String FRAGMENT_KEY = "ABC";
    private static final int FRAGMENT_CODE = 0;

    public FilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_filter, container, false);

        mSeekStart = (SeekBar) v.findViewById(R.id.seekBar3);
        mSeekEnd = (SeekBar) v.findViewById(R.id.seekBar4);
        mStartLimit = (TextView) v.findViewById(R.id.start_limit);
        mEndLimit = (TextView) v.findViewById(R.id.end_limit);
        inc = (Button) v.findViewById(R.id.plus);
        dec = (Button) v.findViewById(R.id.minus);
        mCancel = (Button) v.findViewById(R.id.cancel);
        mConfirm = (Button) v.findViewById(R.id.confirm);
        mReset = (TextView) v.findViewById(R.id.reset);

        cc = getArguments().getString("currency");
        //loc = getArguments().getString("loc");

        mSeekStart.setMax(max);
        mSeekEnd.setMax(max);

        seek();
        displayList(v);
        setMax();

        mCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(startLimit>endLimit){
                    mEndLimit.setError("Invalid Range");
                }
                else {
                    HomeFragment fragment = new HomeFragment();
                    Bundle bundle = new Bundle();
                    //bundle.putString("location", loc);
                    bundle.putInt("min", startLimit);
                    bundle.putInt("max", endLimit);
                    bundle.putString("cat", selectedCategory);
                    fragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
                    //getFragmentManager().popBackStack();

                    /*Intent intent = new Intent();
                    intent.putExtra("min", startLimit);
                    intent.putExtra("max", endLimit);
                    intent.putExtra("cat", selectedCategory);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                    getFragmentManager().popBackStack();*/
                }
            }
        });

        return v;
    }

    public void seek()
    {
        mSeekStart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                startLimit = progressValue;
                mStartLimit.setText(cc+startLimit);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mStartLimit.setText(cc+startLimit);
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });

        mSeekEnd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                endLimit = progressValue;
                mEndLimit.setText(cc+endLimit);
                // Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mEndLimit.setText(cc+endLimit);
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });

        mReset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                max = 1000;
                mSeekStart.setMax(max);
                mSeekStart.setProgress(0);
                mSeekEnd.setMax(max);
                mSeekEnd.setProgress(0);
                seek();
            }
        });
    }

    public void setMax()
    {
        inc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                max = max + 4000;
                mSeekStart.setMax(max);
                mSeekStart.setProgress(startLimit);
                mSeekEnd.setMax(max);
                mSeekEnd.setProgress(endLimit);
                seek();
            }
        });
        dec.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                max = max - 1000;
                mSeekStart.setMax(max);
                mSeekStart.setProgress(startLimit);
                mSeekEnd.setMax(max);
                mSeekEnd.setProgress(endLimit);
                seek();
            }
        });
    }

    public void displayList(View view)
    {
        if(isAdded()){
            catList = getResources().getStringArray(R.array.category_list);
        }

        for(int i : images) {
            imageList.add(i);
        }

        for(String str : catList) {
            categoryList.add(str);
        }

        adapter=new CategoryAdapter(imageList,categoryList,getContext());
        recyclerView= (RecyclerView) view.findViewById(R.id.recycler_view);
        layoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(this);
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        selectedCategory = catList[position];
        for(int index=0; index<catList.length; index++) {
            if (index == position) {
                v.setBackgroundColor(Color.LTGRAY);
            }
        }
    }
}
