package com.example.shara.courseproject;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class GeneralAdapter extends RecyclerView.Adapter<GeneralAdapter.PlanetViewHolder> {

    ArrayList<String> itemList;

    public GeneralAdapter(ArrayList<String> categoryList, Context context) {
        this.itemList = categoryList;
    }

    @Override
    public GeneralAdapter.PlanetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row_list,parent,false);
        PlanetViewHolder viewHolder=new PlanetViewHolder(v);
        return viewHolder;
    }

    int selectedPosition=-1;
    @Override
    public void onBindViewHolder(GeneralAdapter.PlanetViewHolder holder, final int position) {
        holder.text.setText(itemList.get(position));
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
        return itemList.size();
    }

    public static class PlanetViewHolder extends RecyclerView.ViewHolder{

        protected TextView text;

        public PlanetViewHolder(View itemView) {
            super(itemView);
            text= (TextView) itemView.findViewById(R.id.text_id);
        }
    }
}