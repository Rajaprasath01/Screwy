package com.rajaprasath.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.rajaprasath.chatapp.R;
import com.rajaprasath.chatapp.model.Category;
import com.rajaprasath.chatapp.ui.stranger.CategoryUsers;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {

    Context context;
    List<Category> categories;

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.category_item,parent,false);
        return new CategoryAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.Viewholder holder, int position) {
         final  Category category = categories.get(position);
         holder.category_name.setText( category.getName());
         holder.category_image.setImageDrawable(category.getIcon());
         holder.category_card.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(context, CategoryUsers.class);
                 intent.putExtra("category_name",category.getName());
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 context.startActivity(intent);
             }
         });



    }

    @Override
    public int getItemCount() {
        return categories.size();
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        private CardView category_card;
        private TextView category_name;
        private ImageView category_image;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            category_name=itemView.findViewById(R.id.category_name);
            category_card=itemView.findViewById(R.id.category_card);
            category_image=itemView.findViewById(R.id.category_image);
        }
    }
}
