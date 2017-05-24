package com.example.shara.courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shara.courseproject.utils.ItemClickSupport;

import java.util.ArrayList;

public class CategoryFragment extends Fragment implements ItemClickSupport.OnItemClickListener{

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> categoryList=new ArrayList();
    private ArrayList<Integer> imageList=new ArrayList();
    String[] catList;
    int[] images = {R.drawable.ic_home_black_24dp,
            0,0,0,0, R.drawable.book,
            0,0,0,0,R.drawable.clothing,
            0,0,0,R.drawable.health,
            0,0,0,R.drawable.sport,
            0,0,0, R.drawable.auto,
            0,0,0, R.drawable.misc};

    public CategoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_category, container, false);
        getActivity().setTitle("Categories");
        displayList(v);
        return v;
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
        String cat = catList[position];
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("cat", cat);
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.content, fragment).addToBackStack(null).commit();
    }
}

