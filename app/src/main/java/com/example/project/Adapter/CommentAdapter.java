package com.example.project.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.Model.Comment;
import com.example.project.Model.UserData;
import com.example.project.R;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentAdapter extends  RecyclerView.Adapter<CommentAdapter.viewHolder>{
    Context context;
    ArrayList<Comment> list;

    public CommentAdapter(Context context, ArrayList<Comment> list) {

        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_rv, parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
      Comment comment = list.get(position);
//      String time = TimeAgo.using(comment.getCommentedAt());
//        String timeAgo = TimeAgo.using(comment.getCommentedAt());
//      holder.time.setText(timeAgo);
        holder.time.setText(comment.getCommentedAt()+"");

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(comment.getCommentedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserData user = snapshot.getValue(UserData.class);
                Picasso.get()
                        .load(user.getProfilePhoto())
                        .placeholder(R.drawable.male)
                        .into(holder.profilePic);
                holder.comment.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + "  " + comment.getCommentBody()));
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
        ImageView profilePic;
        TextView time, comment;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profilePic);
            time = itemView.findViewById(R.id.time);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}
