package com.example.project.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.CommentActivity;
import com.example.project.Model.NotificationModel;
import com.example.project.Model.PostModel;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {
    ArrayList<PostModel> list;
    Context context;

    public PostAdapter(ArrayList<PostModel> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashbord_rv,parent,false);
     return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        PostModel model = list.get(position);
        Picasso.get()
                .load(model.getPostImage())

                .into(holder.postImage);
        String postDescription = model.getPostDescription();
        holder.like.setText(model.getPostLike()+"");
//


        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(model.getPostedBy()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserData user = snapshot.getValue(UserData.class);
                Picasso.get()
                        .load(user.getProfilePhoto())

                        .into(holder.profilePic);

                holder.userName.setText(user.getName());

                if (postDescription.equals("")){

                    holder.postDescription.setVisibility(View.GONE);
                }else{
                    holder.postDescription.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + "  " + model.getPostDescription()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(model.getPostId())
                .child("likes")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart2, 0, 0, 0);
                }else{
                    holder.like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("posts")
                                    .child(model.getPostId())
                                    .child("likes")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(model.getPostId())
                                            .child("postLike")
                                            .setValue(model.getPostLike() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart2, 0, 0, 0);

                                            NotificationModel notification = new NotificationModel();
                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                            notification.setNotificationAt(new Date().getTime());
                                            notification.setPostId(model.getPostId());
                                            notification.setPostedBy(model.getPostedBy());
                                            notification.setType("like");
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("notification")
                                                    .child(model.getPostedBy())
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

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", model.getPostId());
                intent.putExtra("postedBy", model.getPostedBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
//
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class viewHolder extends RecyclerView.ViewHolder{
        ImageView  profilePic, more, postImage, comment,   save,share;
        TextView userName,postDescription,like,name;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            postDescription = itemView.findViewById(R.id.postDescription);
            profilePic = itemView.findViewById(R.id.profilePic);
            more = itemView.findViewById(R.id.more);
            postImage = itemView.findViewById(R.id.postImage);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.send);
            userName = itemView.findViewById(R.id.userName);

            save = itemView.findViewById(R.id.save);
            name = itemView.findViewById(R.id.name);



        }
    }
}
