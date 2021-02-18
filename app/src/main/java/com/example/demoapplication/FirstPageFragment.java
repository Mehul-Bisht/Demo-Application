package com.example.demoapplication;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.demoapplication.Adapters.RecyclerAdapter;
import com.example.demoapplication.Callbacks.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class FirstPageFragment extends Fragment {

    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    ListenerRegistration mListenerRegistration;

    static class TimeComparator implements Comparator<User> {

        @Override
        public int compare(User user1, User user2) {
            return (int)(user2.getTimestamp() - user1.getTimestamp());
        }
    }

    public FirstPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<User> users = new ArrayList<>();

        CollectionReference docRef = FirebaseFirestore.getInstance().collection("Users");

        adapter = new RecyclerAdapter(requireContext(), users, new ClickListener() {
            @Override
            public void clickItem(int position) {
                deleteUser(users.get(position).getFirstName());
            }
        });

            mListenerRegistration = docRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("SnapshotListener ", "Listen failed.", e);
                    return;
                }

                if (value != null && !value.isEmpty()) {
                    Log.d("SnapshotListener ", "Current data: not null");
                    for(DocumentChange dc: value.getDocumentChanges()) {
                        if(dc.getType() == DocumentChange.Type.ADDED) {
                            User user = dc.getDocument().toObject(User.class);
                            Log.d("SnapshotListener ","added " + user.getFirstName());
                            users.add(user);
                            users.sort(new TimeComparator());
                            adapter.updateList(users);
                        } else if(dc.getType() == DocumentChange.Type.MODIFIED) {
                            // do nothing
                        } else if(dc.getType() == DocumentChange.Type.REMOVED) {
                            User user = dc.getDocument().toObject(User.class);
                            Log.d("SnapshotListener ","removed " + user.getFirstName());

                            Iterator<User> userItr = users.iterator();

                            if(!userItr.hasNext()) {
                                Log.d("iterator ","empty");
                            } else {
                                while(userItr.hasNext()) {
                                    if(userItr.next().getTimestamp().equals(user.getTimestamp())) {
                                        userItr.remove();
                                    }
                                }
                            }

                            adapter.updateList(users);
                        }
                    }

                    recyclerView = view.findViewById(R.id.recyclerview);
                    recyclerView.setAdapter(adapter);
                    ItemTouchHelper itemTouchHelper =
                            new ItemTouchHelper(new SwipeToDeleteCallback(adapter, new ClickListener() {
                                @Override
                                public void clickItem(int position) {
                                    deleteUser(users.get(position).getFirstName());
                                }
                            }));
                    itemTouchHelper.attachToRecyclerView(recyclerView);

                } else {
                    Log.d("SnapshotListener ", "Current data: null");
                }
            }
        });

    }

    private void deleteUser(String userName) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document("" + userName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting document", e);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListenerRegistration.remove();
    }
}