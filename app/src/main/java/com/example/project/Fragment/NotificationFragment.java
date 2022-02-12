package com.example.project.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Adapter.NotificationAdapter;
import com.example.project.Model.NotificationModel;
import com.example.project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<NotificationModel> list;
FirebaseDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = view.findViewById(R.id.notificationRV);
        list = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));
//        list.add(new NotificationModel(R.drawable.x,"<b>Harry</b> mention you in comment: Good luck","just now"));

        NotificationAdapter adapter = new NotificationAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
        database.getReference()
                .child("notification")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren() )
                        {
                            NotificationModel notification = snapshot1.getValue(NotificationModel.class);
                            notification.setNotificationId(snapshot1.getKey());
                            list.add(notification);

                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        return view;

    }
}