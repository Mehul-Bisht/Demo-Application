package com.example.demoapplication.Callbacks;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.demoapplication.User;

import java.util.List;

/**
 * Created by Mehul Bisht on 18-02-2021
 */

public class MyDiffCallback extends DiffUtil.Callback{

    List<User> oldUsers;
    List<User> newUsers;

    public MyDiffCallback(List<User> newUsers, List<User> oldUsers) {
        this.newUsers = newUsers;
        this.oldUsers = oldUsers;
    }

    @Override
    public int getOldListSize() {
        return oldUsers.size();
    }

    @Override
    public int getNewListSize() {
        return newUsers.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldUsers.get(oldItemPosition).getTimestamp().equals(newUsers.get(newItemPosition).getTimestamp());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldUsers.get(oldItemPosition).equals(newUsers.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}