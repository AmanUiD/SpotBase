package com.example.project.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.CommentActivity;
import com.example.project.Model.NotificationModel;
import com.example.project.Model.UserData;
import com.example.project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder>{
    ArrayList<NotificationModel> list;
    Context context;

    public NotificationAdapter(ArrayList<NotificationModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_view,parent,false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        NotificationModel model = list.get(position);
        String type = model.getType();
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(model.getNotificationBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserData user = snapshot.getValue(UserData.class);
                        Picasso.get()
                                .load(user.getProfilePhoto())
                                .into(holder.profile);
                        if (type.equals("like")){
                            holder.notification.setText(Html.fromHtml("<b>"+user.getName() +"</b>"+ " liked your post"));
                        }else if (type.equals("comment")){
                            holder.notification.setText(Html.fromHtml("<b>"+user.getName() +"</b>"+ " comment on your post"));
                        }else {
                            holder.notification.setText(Html.fromHtml("<b>"+user.getName() +"</b>"+ " following you"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.openNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
                if (!type.equals("follow")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("notification")
                            .child(model.getPostedBy())
                            .child(model.getNotificationId())
                            .child("checkOpen")
                            .setValue(true);
                    holder.openNotification.setBackgroundColor(Color.parseColor("#ffffff"));
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("postId", model.getPostId());
                    intent.putExtra("postedBy", model.getPostedBy());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
        Boolean checkOpen = model.isCheckOpen();
        if (checkOpen == true){
            holder.openNotification.setBackgroundColor(Color.parseColor("#ffffff"));
        }else{

        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView notification, time;
        ConstraintLayout openNotification;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            openNotification = itemView.findViewById(R.id.openNotification);
            profile = itemView.findViewById(R.id.profilePic);
            notification = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
        }
    }
}
