package com.example.shara.courseproject;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by shara on 4/26/2017.
 */


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.PlanetViewHolder> {

    ArrayList<String> categoryList;
    ArrayList<Integer> array_image = new ArrayList<Integer>();

    public CategoryAdapter(ArrayList<Integer> array_image,ArrayList<String> categoryList, Context context) {
        this.array_image = array_image;
        this.categoryList = categoryList;
    }

    @Override
    public CategoryAdapter.PlanetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row_list,parent,false);
        PlanetViewHolder viewHolder=new PlanetViewHolder(v);
        return viewHolder;
    }

    int selectedPosition=-1;
    @Override
    public void onBindViewHolder(CategoryAdapter.PlanetViewHolder holder, final int position) {
        holder.image.setImageResource(array_image.get(position));
        holder.text.setText(categoryList.get(position));
       // holder.itemView.setBackgroundColor(Color.WHITE);

        if(selectedPosition==position)
            holder.itemView.setBackgroundColor(Color.parseColor("#000000"));
        else
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class PlanetViewHolder extends RecyclerView.ViewHolder{

        protected ImageView image;
        protected TextView text;

        public PlanetViewHolder(View itemView) {
            super(itemView);
            image= (ImageView) itemView.findViewById(R.id.image_id);
            text= (TextView) itemView.findViewById(R.id.text_id);
        }
    }
}