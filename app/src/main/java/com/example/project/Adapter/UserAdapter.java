package com.example.project.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Model.Follow;
import com.example.project.Model.NotificationModel;
import com.example.project.Model.UserData;
import com.example.project.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder>{
    ArrayList<UserData> list;
    Context context;

    public UserAdapter(ArrayList<UserData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile2,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
     UserData user = list.get(position);
        Picasso.get()
                .load(user.getProfilePhoto())
                .placeholder(R.drawable.male)
                .into(holder.profilePic);
        holder.name.setText(user.getName());
        holder.profession.setText(user.getProfession());
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(user.getUserID())
                .child("followers")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    holder.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context , R.drawable.following_btn));
                    holder.followBtn.setText("Following");
                    holder.followBtn.setTextColor(context.getResources().getColor(R.color.darker_grey));
                    holder.followBtn.setEnabled(false);
                }else {
                    holder.followBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           list.clear();
                            Follow follow = new Follow();

                            follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                            follow.setFollowedAt(new Date().getTime());
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(user.getUserID())
                                    .child("followers")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    list.clear();
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Users")
                                            .child(user.getUserID())
                                            .child("followerCount")
                                            .setValue(user.getFollowerCount() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            holder.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context , R.drawable.following_btn));
                                            holder.followBtn.setText("Following");
                                            holder.followBtn.setTextColor(context.getResources().getColor(R.color.darker_grey));
                                            holder.followBtn.setEnabled(false);
                                            Toast.makeText(context, "You Followed " +user.getName(), Toast.LENGTH_SHORT).show();
                                            NotificationModel notification = new NotificationModel();
                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                            notification.setNotificationAt(new Date().getTime());
                                            notification.setType("follow");
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("notification")
                                                    .child(user.getUserID())
                                                    .push()
                                                    .setValue(notification);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        TextView name, profession;
        Button followBtn;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            profession = itemView.findViewById(R.id.profession);
            name = itemView.findViewById(R.id.name);
            followBtn = itemView.findViewById(R.id.followBtn);
        }
    }
}
