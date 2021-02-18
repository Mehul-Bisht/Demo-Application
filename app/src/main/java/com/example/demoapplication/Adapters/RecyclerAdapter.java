package com.example.demoapplication.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.demoapplication.Callbacks.MyDiffCallback;
import com.example.demoapplication.ClickListener;
import com.example.demoapplication.Constants;
import com.example.demoapplication.R;
import com.example.demoapplication.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Created by Mehul Bisht on 17-02-2021
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.UsersViewHolder> {

    Context mContext;
    ArrayList<User> mUsers;
    private final ClickListener clickListener;

    public RecyclerAdapter(Context context, ArrayList<User> users, ClickListener clickListener) {
        this.mContext = context;
        this.mUsers = users;
        this.clickListener = clickListener;
    }

    public Context getContext() {
        return mContext;
    }

    public void updateList(ArrayList<User> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallback(this.mUsers, newList));
        diffResult.dispatchUpdatesTo(this);
    }

    static class UsersViewHolder extends RecyclerView.ViewHolder {

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        ShapeableImageView user_profile = itemView.findViewById(R.id.profile_image);
        TextView name = itemView.findViewById(R.id.profile_name);
        TextView bio = itemView.findViewById(R.id.profile_bio);
        ImageView deleteButton = itemView.findViewById(R.id.delete_button);
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerAdapter.UsersViewHolder holder = new UsersViewHolder(
                LayoutInflater.from(mContext)
                .inflate(R.layout.list_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

        User user = mUsers.get(position);
        String imageUrl = user.getImageUrl();
        if(imageUrl.isEmpty()) imageUrl = Constants.defaultImageUrl;

        float radius = mContext.getResources().getDimension(R.dimen.image_corner_radius);
        ShapeAppearanceModel shapeAppearanceModel = holder.user_profile.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED,radius)
                .build();

        holder.user_profile.setShapeAppearanceModel(shapeAppearanceModel);

        Glide.with(mContext)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.user_profile);

        holder.name.setText(user.getFirstName() + " " + user.getLastName());
        holder.bio.setText(user.getGender() + " | " + user.getDateOfBirth() + " | " + user.getCountry());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Confirm action")
                        .setMessage("Are you sure you want to delete this user?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                clickListener.clickItem(position);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog,
                                // do nothing
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
