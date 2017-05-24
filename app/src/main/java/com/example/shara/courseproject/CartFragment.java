package com.example.shara.courseproject;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CartFragment extends Fragment {

    private FragmentTabHost mTabHost;


    public CartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart, container, false);
        getActivity().setTitle("Your Cart");
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost = (FragmentTabHost)v.findViewById(R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        mTabHost.setCurrentTab(1);

        mTabHost.addTab(mTabHost.newTabSpec("buying").setIndicator("Buying"),
                BuyFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("selling").setIndicator("Selling"),
                ItemFragment.class, null);
        return v;
    }
}
