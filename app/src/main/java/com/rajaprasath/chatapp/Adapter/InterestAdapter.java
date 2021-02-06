package com.rajaprasath.chatapp.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.controller.User;
import com.rajaprasath.chatapp.model.Category;

import java.util.ArrayList;
import java.util.List;

public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.ViewHolder> {
    Context context;
    List<Category> categories;
    Integer activity;
   static ArrayList<String> interests = new ArrayList<String>();

    public InterestAdapter(Context context, List<Category> categories,Integer activity) {
        this.context = context;
        this.categories = categories;
        this.activity=activity;

    }




    @NonNull
    @Override
    public InterestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.interest_item,parent,false);
        return new InterestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final InterestAdapter.ViewHolder holder, int position) {

        String name= categories.get(position).getName();
        Drawable icon = categories.get(position).getIcon();
        holder.item.setText(name);
        holder.icon.setImageDrawable(icon);
        if (activity==0){
            interests.clear();
        }

        if (User.getInstance().getInterest()!=null){

            for (int i=0;i<User.getInstance().getInterest().size();i++){

                if (User.getInstance().getInterest().get(i).equals(name)){

                    holder.item.toggle();
                    holder.item.setChecked(true);
                }
            }
        }


        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.item.toggle();
                if (holder.item.isChecked()){
                    
                    interests.add(holder.item.getText().toString().trim());
                }
                else {

                    for (int i=0;i<interests.size();i++){

                        if (interests.get(i)==holder.item.getText().toString().trim()){
                            interests.remove(i);
                        }
                    }
                }

                User.getInstance().setInterest(interests);



            }
        });


    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckedTextView item;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.interest_choice_id);
            icon=itemView.findViewById(R.id.interests_icon);

        }
    }
}
